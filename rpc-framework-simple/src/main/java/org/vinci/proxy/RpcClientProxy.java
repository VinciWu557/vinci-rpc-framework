package org.vinci.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.vinci.config.RpcServiceConfig;
import org.vinci.enums.RpcErrorMessageEnum;
import org.vinci.enums.RpcResponseCodeEnum;
import org.vinci.exception.RpcException;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.remoting.transport.RpcRequestTransport;
import org.vinci.remoting.transport.netty.client.NettyRpcClient;
import org.vinci.remoting.transport.socket.SocketRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类
 * 当一个动态代理对象调用一个方法时，它实际上调用了下面的 invoke 方法
 * 正是因为有了动态代理，客户端调用远程方法就像调用本地方法一样（屏蔽了中间过程）
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    // INTERFACE_NAME 是一个常量，表示服务接口的名称
    private static final String INTERFACE_NAME = "interfaceName";

    // 用于将请求发送到服务器。有两种实现：socket和netty
    private final RpcRequestTransport rpcRequestTransport;

    // 表示rpc服务的配置信息
    private final RpcServiceConfig rpcServiceConfig;

    /**
     * 构造函数，初始化RpcClientProxy对象。
     * @param rpcRequestTransport 用于发送请求的RpcRequestTransport对象
     * @param rpcServiceConfig rpc服务的配置信息
     */
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    /**
     * 构造函数，初始化RpcClientProxy对象。
     * @param rpcRequestTransport 用于发送请求的RpcRequestTransport对象
     */
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * 返回一个实现了指定接口的代理对象
     * @param clazz 指定的接口
     * @param <T> 接口类型
     * @return 实现了指定接口的代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 当代理对象的方法被调用时，实际上是调用了这个invoke方法
     * @param proxy 代理对象
     * @param method 被调用的方法
     * @param args 方法的参数
     * @return 方法的返回值
     * @throws Exception 如果调用方法失败
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        // 构造 RPC 请求对象
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        // 构造 RPC 响应对象
        RpcResponse<Object> rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient) {
            // 如果使用 Netty 实现，则发送异步请求，并等待响应
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        if (rpcRequestTransport instanceof SocketRpcClient) {
            // 如果使用 Socket 实现，则发送同步请求，并等待响应
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        // 检查响应
        this.check(rpcResponse, rpcRequest);
        // 返回方法的返回值
        return rpcResponse.getData();
    }

    /**
     * 检查请求和响应是否有效
     * @param rpcResponse
     * @param rpcRequest
     */
    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
        // 调用服务失败
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 请求和响应不匹配
        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
        // 服务调用失败
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
