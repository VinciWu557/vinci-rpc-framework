package org.vinci.remoting.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.vinci.compress.Compress;
import org.vinci.enums.CompressTypeEnum;
import org.vinci.enums.SerializationTypeEnum;
import org.vinci.extension.ExtensionLoader;
import org.vinci.remoting.constants.RpcConstants;
import org.vinci.remoting.dto.RpcMessage;
import org.vinci.serialize.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    // 原子整数类, 用于编号
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     * 对 RpcMessage 对象进行编码
     * @param ctx 操作Channel的上下文信息
     * @param rpcMessage 待编码的RpcMessage对象
     * @param out 用于存储编码后的字节数据
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            // 写入魔数（4字节）
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            // 写入协议版本号（1字节）
            out.writeByte(RpcConstants.VERSION);
            // 预留4字节，用于写入消息总长度
            out.writerIndex(out.writerIndex() + 4);
            // 写入消息类型（1字节）
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            // 写入序列化类型（1字节）
            out.writeByte(rpcMessage.getCodec());
            // 写入压缩类型（1字节）
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            // 写入消息编号，用于匹配请求和响应
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // build full length
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;

            // 如果消息类型不是心跳请求或响应，则需要进行序列化和压缩
            // fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // 获取序列化实例，根据序列化类型
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                // 序列化消息体
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // 获取压缩实例，根据压缩类型
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                // 压缩消息体
                bodyBytes = compress.compress(bodyBytes);
                // 计算总长度
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                // 写入压缩后的消息体
                out.writeBytes(bodyBytes);
            }
            // 记录当前写索引，用于更新消息总长度
            int writeIndex = out.writerIndex();
            // 回到预留的长度字段位置
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            // 写入消息总长度
            out.writeInt(fullLength);
            // 回到当前写索引位置
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }

    }
}
