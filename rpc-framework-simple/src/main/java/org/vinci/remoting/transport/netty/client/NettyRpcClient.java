package org.vinci.remoting.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.vinci.enums.CompressTypeEnum;
import org.vinci.enums.SerializationTypeEnum;
import org.vinci.extension.ExtensionLoader;
import org.vinci.factory.SingletonFactory;
import org.vinci.registry.ServiceDiscovery;
import org.vinci.remoting.constants.RpcConstants;
import org.vinci.remoting.dto.RpcMessage;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.remoting.transport.RpcRequestTransport;
import org.vinci.remoting.transport.netty.codec.RpcMessageDecoder;
import org.vinci.remoting.transport.netty.codec.RpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    // 服务发现接口
    private final ServiceDiscovery serviceDiscovery;
    // 未处理请求
    private final UnprocessedRequests unprocessedRequests;
    // 连接提供者
    private final ChannelProvider channelProvider;
    // 启动类
    private final Bootstrap bootstrap;
    // 事件循环组
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(){
        // 资源初始化
        // 创建事件循环组
        eventLoopGroup = new NioEventLoopGroup();
        // 创建启动类
        bootstrap = new Bootstrap();
        // 配置事件循环组
        bootstrap.group(eventLoopGroup)
                // 设置通道为 NIO 服务器通道
                .channel(NioSocketChannel.class)
                // 添加日志处理器
                .handler(new LoggingHandler(LogLevel.INFO))
                // 超时时间周期
                // 当连接超时或连接无法建立, 连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 配置初始化器
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 获取通道管道
                        ChannelPipeline p = ch.pipeline();
                        // 如果在 15 秒内没有数据传输, 发送心跳请求
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        // 添加消息编码器
                        p.addLast(new RpcMessageEncoder());
                        // 添加消息解码器
                        p.addLast(new RpcMessageDecoder());
                        // 添加 Netty RPC 客户端处理器
                        p.addLast(new NettyRpcClilentHandler());
                    }
                });
        // 获取服务发现接口的扩展实现
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        // 获取未处理请求的单例工厂实例
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        // 获取连接提供者的单例工厂实例
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 连接服务器并获取通道，以便可以将rpc消息发送到服务器
     *
     * @param inetSocketAddress 服务器地址
     * @return 通道
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // 创建CompletableFuture对象, 它代表一个异步操作的结果, 在此处指channel对象
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 尝试连接指定的服务器地址, 连接结果会通过ChannelFutureListener返回
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            // 连接成功, 将channel对象通过CompletableFuture.complete方法返回
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                // 连接失败, 抛出异常
                throw new IllegalStateException();
            }
        });
        // 等待异步操作完成并返回结果
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 创建CompletableFuture对象, 它代表一个异步操作的结果, 在此处指rpc调用的返回结果
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // 通过服务发现组件获取rpc服务提供者地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // 获取与rpc服务提供者地址关联的channel对象
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // 将请求放入未处理请求map中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            // 构建rpc消息
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            // 发送rpc消息到rpc服务提供者地址对应的channel上
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    // rpc调用失败, 关闭channel
                    // 将异常结果通过CompletableFuture.completeExceptionally方法返回
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            // channel对象未激活, 抛出异常
            throw new IllegalStateException();
        }
        // 返回CompletableFuture对象, 该对象代表异步操作的结果, 在此处指rpc调用的返回结果
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        // 从channelProvider中获取与inetSocketAddress关联的channel对象
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            // channelProvider中不存在与inetSocketAddress关联的channel对象
            // 创建一个新的channel对象并连接指定地址
            channel = doConnect(inetSocketAddress);
            // 将新建的channel对象存入channelProvider中
            channelProvider.set(inetSocketAddress, channel);
        }
        // 返回channel对象
        return channel;
    }

    public void close() {
        // 优雅关闭eventLoopGroup
        eventLoopGroup.shutdownGracefully();
    }

}
