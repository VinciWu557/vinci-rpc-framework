package org.vinci.registry.zk.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.vinci.enums.RpcConfigEnum;
import org.vinci.utils.PropertiesFileUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtils {

    // 每次重试之前的等待时间基数，单位为毫秒
    private static final int BASE_SLEEP_TIME = 1000;

    // 最大重试次数
    private static final int MAX_RETRIES = 3;

    // 服务注册在 ZooKeeper 中的根路径
    public static final String ZK_REGISTER_ROOT_PATH = "/vinci-rpc";

    // 存储注册服务信息, 服务名称到服务地址列表的映射, 使用 ConcurrentHashMap 实现
    public static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    // 已经注册的服务路径集合，使用 ConcurrentHashMap 的新的 keySet() 方法实现
    public static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    // ZooKeeper 客户端
    private static CuratorFramework zkClient;

    // 默认的 ZooKeeper 地址
    public static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    // 私有构造器
    private CuratorUtils(){}

    /**
     * 创建持久节点
     * @param zkClient 执行 ZooKeeper 操作
     * @param path 节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try{
            // 判断节点路径是否已经创建
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node already exists. The node is: [{}]", path);
            }else {
                // 创建节点
                // eg: /vinci-rpc/org.vinci.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
        } catch (Exception e){
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 获取一个节点的子节点
     * @param zkClient 执行 ZooKeeper 操作
     * @param rpcServiceName RPC 服务名称, 如 org.vinci.HelloServicetest2version1
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        // 如果已经获取过该服务的地址列表，则直接返回
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try{
            // 获取该服务节点下的所有子节点
            result = zkClient.getChildren().forPath(servicePath);
            // 将获取到的子节点添加到地址列表中
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            // 为该服务节点注册监听器
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e){
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 删除注册数据
     * @param zkClient 执行 ZooKeeper 操作
     * @param inetSocketAddress 服务地址
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        // 遍历已注册的路径，采用并行流处理
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                // 如果路径以指定地址结尾，则删除该路径的数据
                if (p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e){
                log.error("clear registry for path [{}] fail", p);
            }
        });
    }

    /**
     * 获取与 ZooKeeper 建立连接的 ZkClient
     * @return CuratorFramework zkClient 执行 ZooKeeper 操作
     */
    public static CuratorFramework getZkClient(){
        // 检测用户是否自定义了 ZooKeeper 地址
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress =
                properties != null
                && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ?
                properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;

        // 如果 ZkClient 已经启动了, 直接返回 ZkClient
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        // 设置重试策略
        // 重试 3 次, 然后增加重试之间的睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // 需要连接的服务器或服务器列表
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // 阻塞 30 s 直到连接到 ZooKeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZooKeeper!");
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * 创建子节点监视器
     * @param rpcServiceName RPC 服务名称
     * @param zkClient ZooKeeper 客户端
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception{
        // 获取服务路径
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        // 创建路径子节点监听器
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheLisenter = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                // 当子节点有变化时，重新获取服务地址列表并存入 SERVICE_ADDRESS_MAP 中
                List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
            }
        };
        // 注册监听器并启动监听
        pathChildrenCache.getListenable().addListener(pathChildrenCacheLisenter);
        pathChildrenCache.start();
    }




}
