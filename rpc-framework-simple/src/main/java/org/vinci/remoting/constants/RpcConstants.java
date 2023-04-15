package org.vinci.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * RpcConstants类包含用于远程过程调用（RPC）的常量。
 */
public class RpcConstants {


    /**
     * 魔数. 用于验证 RpcMessage
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    /**
     * 默认字符集编码
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 版本信息
    public static final byte VERSION = 1;
    /**
     * 数据总长度（头部 + 消息体）
     */
    public static final byte TOTAL_LENGTH = 16;
    /**
     * 请求消息类型
     */
    public static final byte REQUEST_TYPE = 1;
    /**
     * 响应消息类型
     */
    public static final byte RESPONSE_TYPE = 2;
    /**
     * 心跳请求消息类型
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    /**
     * 心跳响应消息类型
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    /**
     * 头部长度
     */
    public static final int HEAD_LENGTH = 16;
    /**
     * 心跳ping消息
     */
    public static final String PING = "ping";
    /**
     * 心跳pong消息
     */
    public static final String PONG = "pong";
    /**
     * 最大帧长度
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
