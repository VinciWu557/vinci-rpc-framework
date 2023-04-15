package org.vinci.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
/**
 * RPC服务的配置类，用于存储RPC服务的相关信息，例如服务的版本、分组、服务实现对象等
 */
public class RpcServiceConfig {
    /**
     * 服务版本号
     */
    private String version = "";
    /**
     * 当接口有多个实现类时，通过group进行区分
     */
    private String group = "";

    /**
     * 目标服务
     */
    private Object service;

    /**
     * 获取完整的RPC服务名称，包括group和version
     * @return RPC服务名称
     */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * 获取服务接口名称
     * @return 服务接口名称
     */
    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
