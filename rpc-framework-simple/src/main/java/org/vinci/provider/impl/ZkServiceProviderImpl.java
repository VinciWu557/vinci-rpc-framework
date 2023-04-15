package org.vinci.provider.impl;

import lombok.extern.slf4j.Slf4j;
import org.vinci.config.RpcServiceConfig;
import org.vinci.enums.RpcErrorMessageEnum;
import org.vinci.exception.RpcException;
import org.vinci.extension.ExtensionLoader;
import org.vinci.provider.ServiceProvider;
import org.vinci.registry.ServiceRegistry;
import org.vinci.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc 服务名称（接口名 + 版本号 + 组名）
     * value: 服务对象
     */
    private final Map<String, Object> serviceMap;
    // 存储已经注册过的服务名
    private final Set<String> registeredService;
    // 服务注册中心
    private final ServiceRegistry serviceRegistry;

    // 构造函数，初始化服务实现对象Map、已注册服务名称Set、服务注册中心
    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    /**
     * 将服务添加到服务映射中
     * @param rpcServiceConfig rpc 服务相关属性
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        // 如果已经注册过该服务，则不再添加
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        // 添加服务实现对象到 Map 中
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        // 输出日志，记录服务名和服务实现的接口名
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 获取指定服务名称的服务对象
     *
     * @param rpcServiceName rpc 服务名称
     * @return 服务对象
     * @throws RpcException 当服务对象未找到时抛出 RpcException 异常
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        // 如果服务实现对象不存在，则抛出异常
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 发布服务，将服务添加到服务映射中，并注册到服务注册中心
     *
     * @param rpcServiceConfig rpc 服务相关属性
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            // 获取本机IP地址
            String host = InetAddress.getLocalHost().getHostAddress();
            // 添加服务实现对象到服务实现对象Map中
            this.addService(rpcServiceConfig);
            // 在注册中心注册该服务
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
