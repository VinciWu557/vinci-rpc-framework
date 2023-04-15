package org.vinci.loadbalance;

import org.vinci.extension.SPI;
import org.vinci.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 通过 SPI 机制，为 RPC 框架提供不同的负载均衡算法
 */
@SPI
public interface LoadBalance {
    /**
     * 在现有的服务地址列表中选择一个
     *
     * @param serviceUrlList 服务地址列表
     * @param rpcRequest RPC 请求
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
