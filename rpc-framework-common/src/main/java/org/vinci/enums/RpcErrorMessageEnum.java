package org.vinci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Rpc 错误枚举类
 */
@AllArgsConstructor // 自动生成带参构造函数
@Getter // 自动生成 getter 方法
@ToString // 自动生成 toString 方法
public enum RpcErrorMessageEnum {
    // 客户端连接服务器失败的错误信息
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务器失败"),
    // 服务调用失败的错误信息
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    // 未找到服务的错误信息
    SERVICE_CAN_NOT_BE_FOUND("服务调用失败"),
    // 注册的服务没有实现任何接口的错误信息
    SERIVCE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口"),
    // 请求和返回的响应不匹配的错误信息
    REQUEST_NOT_MATCH_RESPONSE("返回结果错误! 请求和返回的响应不匹配");
    // 错误信息
    private final String message;
}
