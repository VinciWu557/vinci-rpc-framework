package org.vinci.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import org.vinci.exception.RpcException;
import org.vinci.factory.SingletonFactory;
import org.vinci.provider.ServiceProvider;
import org.vinci.provider.impl.ZkServiceProviderImpl;
import org.vinci.remoting.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理RPC请求的核心类，主要实现了根据请求信息调用对应的服务方法并返回结果
 */
@Slf4j
public class RpcRequestHandler {

    // 服务提供者
    // 定义了向服务提供者注册、获取和发布服务的方法
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        // 通过 ZkServiceProviderImpl 获取 ServiceProvider 实例
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 处理 RpcRequest，调用对应的方法并返回方法执行结果
     *
     * @param rpcRequest RpcRequest 对象
     * @return 调用方法的执行结果
     */
    public Object handle(RpcRequest rpcRequest) {
        // 获取请求的 service 实例对象
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        // 调用目标方法并返回结果
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 调用目标方法并返回执行结果
     *
     * @param rpcRequest 客户端请求
     * @param service    服务实例对象
     * @return 目标方法执行结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            // 通过反射获取方法并执行
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            // 记录日志
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}