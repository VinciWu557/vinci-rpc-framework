package org.vinci.loadbalance.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.vinci.loadbalance.AbstractLoadBalance;
import org.vinci.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConsistentHashSelector 实现 LoadBalance接口，使用一致性哈希算法进行负载均衡
 * https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 *
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    // 使用 ConcurrentHashMap 存储 ConsistentHashSelector 对象，确保线程安全
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    /**
     *
     * @param serviceAddresses 服务地址列表
     * @param rpcRequest RPC请求
     * @return 适合当前RPC请求的服务地址
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 计算服务地址列表的标识哈希值
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // 通过RPC请求构建RPC服务名称
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 获取与RPC服务名称对应的一致性哈希选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // 如果选择器为空或其标识哈希值不等于服务地址列表的标识哈希值，则创建新的一致性哈希选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        // 通过RPC服务名称和参数列表选择一个服务地址
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    /**
     * 实现一致性哈希算法的选择器
     */
    static class ConsistentHashSelector {
        // 用于存储虚拟节点和实际服务地址之间的映射关系
        private final TreeMap<Long, String> virtualInvokers;

        // 标识哈希码
        private final int identityHashCode;

        /**
         * 构造函数
         * @param invokers 服务地址列表
         * @param replicaNumber 副本数
         * @param identityHashCode 标识哈希码
         */
        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            // TreeMap 用于存储虚拟节点和实际服务地址之间的映射关系
            this.virtualInvokers = new TreeMap<>();
            // 读入标识哈希码
            this.identityHashCode = identityHashCode;

            // 遍历实际节点，为每个节点创建多个虚拟节点
            for (String invoker : invokers) {
                // 每个实际节点创建的虚拟节点个数
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 对每个虚拟节点计算 hash 值
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        // 将虚拟节点加入 TreeMap 中
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        /**
         * 对输入的字符串进行 MD5 加密，返回加密后的字节数组
         * @param key
         * @return
         */
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        /**
         * 将字节数组 digest 的第 idx 个 4 字节块转换成 long 类型的哈希值，返回该哈希值
         * @param digest
         * @param idx
         * @return
         */
        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        /**
         * 根据输入的服务名 rpcServiceKey，将其转化为字节数组并进行哈希
         * 然后调用 selectForKey 方法来选择服务地址
         * @param rpcServiceKey
         * @return 服务地址
         */
        public String select(String rpcServiceKey) {
            // 将输入的服务名 rpcServiceKey 转化为字节数组
            byte[] digest = md5(rpcServiceKey);
            // 对字节数组进行哈希, 然后调用 selectForKey 方法来选择服务地址
            return selectForKey(hash(digest, 0));
        }

        /**
         * 根据输入的哈希值 hashCode，返回服务地址
         * @param hashCode
         * @return 服务地址
         */
        public String selectForKey(long hashCode) {
            // 在虚拟节点列表中选择最小的大于等于输入的哈希值的节点
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            // 如果没有这样的节点，则选择列表中的第一个节点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            // 返回选择的节点的值，即服务地址
            return entry.getValue();
        }
    }
}
