package org.vinci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Rpc 配置枚举类
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    // 枚举类型的成员变量，表示rpc配置文件路径
    RPC_CONFIG_PATH("rpc.properties"),
    // 枚举类型的成员变量，表示ZooKeeper地址
    ZK_ADDRESS("rpc.zookeeper.address");
    // 枚举类型的成员变量，表示枚举项对应的属性值
    private final String propertyValue;
}
