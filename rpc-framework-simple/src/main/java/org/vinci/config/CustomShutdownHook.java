package org.vinci.config;

import lombok.extern.slf4j.Slf4j;
import org.vinci.registry.zk.util.CuratorUtils;
import org.vinci.remoting.transport.netty.server.NettyRpcServer;
import org.vinci.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 服务器关闭时，执行一些操作，例如注销所有服务
 */
@Slf4j
public class CustomShutdownHook {

    // 定义了一个私有静态常量 CUSTOM_SHUTDOWN_HOOK 作为自身实例
    // 使用单例模式，以保证全局只有一个 CustomShutdownHook 实例
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    // 获取 CustomShutdownHook 实例
    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    /**
     *  添加 JVM 关闭钩子，用于在程序退出前执行清理工作
     */
    public void clearAll() {
        // 在日志中记录 JVM 关闭钩子已添加的信息
        log.info("addShutdownHook for clearAll");
        // 添加 JVM 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 获取本机 IP 地址和 NettyRpcServer 的端口号
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                // 在 ZooKeeper 上清除当前服务注册的信息
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            // 关闭所有线程池，释放资源
            ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }
}

