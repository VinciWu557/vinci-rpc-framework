package org.vinci.remoting.dto;

import lombok.*;
import org.vinci.enums.RpcResponseCodeEnum;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
// 创建泛型类
/**
* RpcResponse类包含了一个RPC请求的响应信息。
* 它包括了请求ID，响应状态码，响应消息和响应数据等信息。
*/
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    // 请求ID
    private String requestId;
    // 响应码
    private Integer code;
    // 响应消息
    private String message;
    // 响应数据
    private T data;

    /**
     * 构造成功的响应对象
     */
    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = new RpcResponse<T>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data){
            response.setData(data);
        }
        return response;
    }

    /**
     * 构造失败的响应对象
     */
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = new RpcResponse<T>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }



}
