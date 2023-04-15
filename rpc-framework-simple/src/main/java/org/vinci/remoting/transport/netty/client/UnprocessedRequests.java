package org.vinci.remoting.transport.netty.client;

import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存放未被服务端处理的请求
 */
public class UnprocessedRequests {
    // 存储未处理的请求，使用 ConcurrentHashMap 来实现并发安全
    private static final
        Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES
            = new ConcurrentHashMap<>();

    // 存储一个请求对应的未来结果
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    // 将请求对应的结果标记为完成状态
    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future){
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
