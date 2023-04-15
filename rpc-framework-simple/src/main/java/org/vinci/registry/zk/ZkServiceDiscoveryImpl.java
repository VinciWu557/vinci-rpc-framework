package org.vinci.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.vinci.enums.RpcErrorMessageEnum;
import org.vinci.exception.RpcException;
import org.vinci.extension.ExtensionLoader;
import org.vinci.loadbalance.LoadBalance;
import org.vinci.registry.ServiceDiscovery;
import org.vinci.registry.zk.util.CuratorUtils;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.utils.CollectionUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现 (基于 Zookeeper 实现)
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        // 从扩展点中获取 LoadBalance 实现
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
    }

    /**
     * 根据 Rpc 请求找到对应的服务地址
     * @param rpcRequest Rpc 请求对象
     * @return InetSocketAddress 服务地址
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        // 获取 rpcServiceName
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 获取与 ZooKeeper 建立连接的 zkClient
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 根据 rpcServiceName 在 ZooKeeper 中查找服务地址
        // 获取指定服务名下的所有服务地址
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        // 如果找不到对应服务，抛出异常
        if (CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        // 通过负载均衡算法选择服务地址
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address: [{}]", targetServiceUrl);
        // 将地址字符串解析成主机名和端口
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
