package org.vinci.remoting.transport;

import org.vinci.extension.SPI;
import org.vinci.remoting.dto.RpcRequest;

// 定义一个发送 RPC 请求的顶层接口
// 分别通过 Socket 和 Netty 两种方式进行实现
// SPI (Service Provider Interface)
// 调用方来指定接口规范, 提供给外部来实现
// 调用方在调用时, 选择自己需要的外部实现
@SPI
public interface RpcRequestTransport {
    /**
     * 用于发送 RPC 请求给服务端并获取结果
     * @param rpcRequest
     * @return
     */
    Object sendRpcRequest(RpcRequest rpcRequest);

}
