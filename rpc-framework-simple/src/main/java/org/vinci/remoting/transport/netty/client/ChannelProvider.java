package org.vinci.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于存放 channel (channel 用于在服务端和客户端之间传输数据)
 */
@Slf4j
public class ChannelProvider {

    // 存放 channel 的 map
    private final Map<String, Channel> channelMap;

    // 初始化 channelMap
    public ChannelProvider(){
        channelMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取指定地址的 channel
     * @param inetSocketAddress 要获取的 channel 的地址
     * @return 如果存在指定地址的 channel 且 channel 是有效的，则返回该 channel；否则返回 null。
     */
    public Channel get(InetSocketAddress inetSocketAddress){
        // 获取 InetSocketAddress 的字符串表示形式 -> 地址
        String key = inetSocketAddress.toString();
        // 判断与相应地址是否存在连接
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            // 如果存在连接, 判断连接是否有效
            if (channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    /**
     * 存放指定地址的 channel
     * @param inetSocketAddress 要存放的 channel 的地址
     * @param channel 要存放的 channel
     */
    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        // 将地址转为字符串作为 key，存放 channel
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    /**
     * 移除指定地址的 channel
     * @param inetSocketAddress 要移除的 channel 的地址
     */
    public void remove(InetSocketAddress inetSocketAddress){
        // 将地址转为字符串作为 key，从 channelMap 中移除 channel
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        // 打印当前 channelMap 的大小
        log.info("Channel Map Size: [{}]", channelMap.size());
    }

}
