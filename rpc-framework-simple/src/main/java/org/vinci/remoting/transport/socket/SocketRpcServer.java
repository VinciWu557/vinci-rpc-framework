package org.vinci.remoting.transport.socket;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.extern.slf4j.Slf4j;
import org.vinci.config.CustomShutdownHook;
import org.vinci.config.RpcServiceConfig;
import org.vinci.factory.SingletonFactory;
import org.vinci.provider.ServiceProvider;
import org.vinci.provider.impl.ZkServiceProviderImpl;
import org.vinci.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static org.vinci.remoting.transport.netty.server.NettyRpcServer.PORT;

@Slf4j
public class SocketRpcServer {

    // 线程池，用于处理客户端连接
    private final ExecutorService threadPool;

    // 服务提供者，用于注册和发布服务
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        // 创建线程池
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-poll");
        // 通过单例工厂获取服务提供者实例
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    // 注册服务
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    // 启动服务
    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            // 获取本机 IP 地址
            String host = InetAddress.getLocalHost().getHostAddress();
            // 绑定端口
            server.bind(new InetSocketAddress(host, PORT));
            // 添加 JVM 关闭钩子，用于停止服务
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept()) != null) {
                // 客户端连接成功，打印日志
                log.info("client connected [{}]", socket.getInetAddress());
                // 将客户端连接交给线程池处理
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
