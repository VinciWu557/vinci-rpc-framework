package org.vinci.registry;

import org.vinci.extension.SPI;
import org.vinci.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */

@SPI
public interface ServiceDiscovery {
    /**
     * 根据 rpcServiceName 获取远程服务地址
     * @param rpcRequest
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
