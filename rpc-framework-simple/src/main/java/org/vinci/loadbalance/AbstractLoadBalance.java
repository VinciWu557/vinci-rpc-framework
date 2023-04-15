package org.vinci.loadbalance;

import org.vinci.remoting.dto.RpcRequest;
import org.vinci.utils.CollectionUtil;

import java.util.List;

/**
 * 抽象负载均衡类，实现了 LoadBalance 接口中的 selectServiceAddress 方法
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    /**
     * 选择一个服务地址，如果服务地址列表只有一个，直接返回
     * @param serviceAddresses 服务地址列表
     * @param rpcRequest RPC 请求
     * @return 被选中的服务地址
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        // 选择服务地址的具体实现由子类完成
        return doSelect(serviceAddresses, rpcRequest);
    }

    /**
     * 选择服务地址的具体实现由子类完成
     * @param serviceAddresses 服务地址列表
     * @param rpcRequest RPC请求
     * @return 被选中的服务地址
     */
    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
