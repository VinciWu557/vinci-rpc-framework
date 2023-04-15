package org.vinci.exception;

/**
 * 自定义序列化异常
 */
public class SerializeException extends RuntimeException{

    /**
     * 构造函数，根据异常信息生成异常对象
     *
     * @param message 异常信息
     */
    public SerializeException(String message){
        super(message);
    }
}
