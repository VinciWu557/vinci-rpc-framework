package org.vinci.registry.zk;

import org.apache.curator.framework.CuratorFramework;
import org.vinci.registry.ServiceRegistry;
import org.vinci.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * 服务注册 (基于 ZooKeeper 实现)
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    /**
     * 向注册中心注册服务
     * @param rpcServiceName    完整的服务名称 (class name + group + version)
     * @param inetSocketAddress 远程服务地址
     */
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // 构造服务在 ZooKeeper 中的路径
        String servicePath =
                CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        // 获取 ZooKeeper 客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 在 ZooKeeper 中创建持久化节点，将服务地址信息存储在节点中
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
