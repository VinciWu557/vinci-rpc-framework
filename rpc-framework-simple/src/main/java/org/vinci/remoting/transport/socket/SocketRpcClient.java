package org.vinci.remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.vinci.exception.RpcException;
import org.vinci.extension.ExtensionLoader;
import org.vinci.registry.ServiceDiscovery;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.transport.RpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 Socket 传输 RpcRequest
 */
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    // ServiceDiscovery 接口的实现类，用于查找远程服务的地址
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        // 使用 ServiceDiscovery 实现类：zk
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class)
                                                .getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 通过 ServiceDiscovery 查找远程服务的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try (Socket socket = new Socket()) {
            // 通过 socket 连接到远程服务地址
            socket.connect(inetSocketAddress);
            // 创建 ObjectOutputStream 对象，用于将 RpcRequest 对象发送给远程服务
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 将 RpcRequest 对象写入输出流
            objectOutputStream.writeObject(rpcRequest);
            // 创建 ObjectInputStream 对象，用于从输入流中读取远程服务返回的 RpcResponse 对象
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 从输入流中读取 RpcResponse 对象，并返回给调用方
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 发生异常时，抛出 RpcException
            throw new RpcException("调用服务失败:", e);
        }
    }
}
