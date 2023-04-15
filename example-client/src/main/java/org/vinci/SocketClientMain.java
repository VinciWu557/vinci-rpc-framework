package org.vinci;

import org.vinci.config.RpcServiceConfig;
import org.vinci.proxy.RpcClientProxy;
import org.vinci.remoting.transport.RpcRequestTransport;
import org.vinci.remoting.transport.socket.SocketRpcClient;

public class SocketClientMain {
    public static void main(String[] args) {
        // 创建 Socket 传输方式的 RPC 请求传输对象
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        // 创建 RPC 服务配置对象
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        // 创建 RPC 客户端代理对象
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        // 获取 HelloService 接口的代理对象
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        // 调用代理对象的 hello 方法，并传入参数 Hello("111", "222")，返回结果为字符串类型
        String hello = helloService.hello(new Hello("111", "222"));
        // 输出 hello 方法的返回结果
        System.out.println(hello);
    }
}
