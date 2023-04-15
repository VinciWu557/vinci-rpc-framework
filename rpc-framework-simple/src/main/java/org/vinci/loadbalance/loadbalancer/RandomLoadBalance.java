package org.vinci.loadbalance.loadbalancer;

import org.vinci.loadbalance.AbstractLoadBalance;
import org.vinci.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡器的实现
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 随机负载均衡算法
     * @param serviceAddresses 服务地址列表
     * @param rpcRequest RPC请求
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 使用 Random 类来生成一个随机数
        Random random = new Random();
        // 该随机数作为索引，从服务地址列表中选取一个地址返回
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}