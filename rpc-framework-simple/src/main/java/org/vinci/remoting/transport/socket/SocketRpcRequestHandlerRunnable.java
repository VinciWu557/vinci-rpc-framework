package org.vinci.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import org.vinci.factory.SingletonFactory;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.remoting.handler.RpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{

    private final Socket socket;

    // Rpc 请求处理
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        // 使用工厂方法 SingletonFactory.getInstance() 获取 RpcRequestHandler 实例
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        // 输出当前线程的名称，以便于调试和追踪
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        // 创建 ObjectInputStream 对象，用于从输入流中读取客户端发送的 RpcRequest 对象
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 创建 ObjectOutputStream 对象，用于将 RpcResponse 对象写入输出流
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            // 从输入流中读取客户端发送的 RpcRequest 对象
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 使用 RpcRequestHandler 处理 RpcRequest，并获取处理结果
            Object result = rpcRequestHandler.handle(rpcRequest);
            // 将 RpcResponse 对象写入输出流
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            // 刷新输出流，确保 RpcResponse 对象被立即发送给客户端
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            // 输出异常信息
            log.error("occur exception:", e);
        }
    }
}
