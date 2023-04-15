package org.vinci.remoting.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
// RPC 请求实体类, 包含要调用的目标方法, 类的名称, 参数等数据
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    // 请求ID
    private String requestId;
    // 要调用的接口名称
    private String interfaceName;
    // 要调用的方法名称
    private String methodName;
    // 方法的参数列表
    private Object[] parameters;
    // 方法的参数类型列表
    private Class<?>[] paramTypes;

    // version 为后续不兼容升级提供可能
    private String version;
    // group 处理一个接口有多个类实现的情况
    private String group;
    /**
     * 获取RPC服务名称
     */
    public String getRpcServiceName(){
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }

}
