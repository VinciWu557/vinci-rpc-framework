package org.vinci.remoting.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.vinci.enums.CompressTypeEnum;
import org.vinci.enums.SerializationTypeEnum;
import org.vinci.factory.SingletonFactory;
import org.vinci.remoting.constants.RpcConstants;
import org.vinci.remoting.dto.RpcMessage;
import org.vinci.remoting.dto.RpcResponse;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClilentHandler extends ChannelInboundHandlerAdapter {
    // 未处理的请求
    private final UnprocessedRequests unprocessedRequests;

    // NettyRpc客户端
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClilentHandler() {
        // 获取未处理的请求的单例实例
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        // 获取NettyRpc客户端的单例实例
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }


    /**
     * 读取服务器发送的消息
     * @param ctx 操作 Channel 和触发事件
     * @param msg 消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 打印客户端接收到的消息
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcResponse){
                // 将消息的类型转换为RpcResponse
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) msg;
                // 标记请求已处理
                unprocessedRequests.complete(rpcResponse);
            }
        } finally {
            // 释放消息
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 触发用户事件
     * @param ctx 操作 Channel 和触发事件
     * @param evt 事件
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果是空闲状态事件
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 如果是写空闲状态
            if (state == IdleState.WRITER_IDLE) {
                // 输出日志，说明发生了写空闲事件
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                // 获取与远程地址相关联的通道
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                // 构造一个心跳请求消息
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                // 发送心跳请求消息，并在失败时关闭通道
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            // 如果不是空闲状态事件，交由父类处理
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 捕捉异常信息并进行处理
     * @param ctx 操作 Channel 和触发事件
     * @param cause 异常信息
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 打印异常信息的日志
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        // 关闭当前 ChannelHandlerContext 对象
        ctx.close();
    }
}
