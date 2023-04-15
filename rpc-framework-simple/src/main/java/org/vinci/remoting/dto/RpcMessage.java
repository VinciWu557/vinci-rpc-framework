package org.vinci.remoting.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
/**
 * RpcMessage类包含了一个RPC消息所需的字段信息。
 */
public class RpcMessage {

    // RPC消息类型
    private byte messageType;

    // 序列化类型
    private byte codec;

    // 压缩类型
    private byte compress;

    // 请求ID
    private int requestId;

    // 请求数据
    private Object data;

}
