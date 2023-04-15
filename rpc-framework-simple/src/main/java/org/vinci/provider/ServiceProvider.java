package org.vinci.provider;

import org.vinci.config.RpcServiceConfig;

/**
 * 服务提供者接口，定义了向服务提供者注册、获取和发布服务的方法
 */
public interface ServiceProvider {

    /**
     * 向服务提供者注册服务
     * @param rpcServiceConfig 服务相关属性
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取指定服务名称的服务对象
     * @param rpcServiceName 服务名称
     * @return 服务对象
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务到注册中心
     * @param rpcServiceConfig 服务相关属性
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
