package org.vinci.exception;

import org.vinci.enums.RpcErrorMessageEnum;

/**
 * RPC 自定义异常
 */
public class RpcException extends RuntimeException{

    /**
     * 构造函数，根据 RpcErrorMessageEnum 和 detail 生成异常信息
     *
     * @param rpcErrorMessageEnum Rpc 错误信息枚举
     * @param detail              错误详细信息
     */
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail){
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    /**
     * 构造函数，根据异常信息和原始异常生成异常对象
     *
     * @param message 异常信息
     * @param cause   原始异常
     */
    public RpcException(String message, Throwable cause){
        super(message, cause);
    }

    /**
     * 构造函数，根据 RpcErrorMessageEnum 生成异常信息
     *
     * @param rpcErrorMessageEnum Rpc 错误信息枚举
     */
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());
    }


}
