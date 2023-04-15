package org.vinci.remoting.transport.netty.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.vinci.compress.Compress;
import org.vinci.enums.CompressTypeEnum;
import org.vinci.enums.SerializationTypeEnum;
import org.vinci.extension.ExtensionLoader;
import org.vinci.remoting.constants.RpcConstants;
import org.vinci.remoting.dto.RpcMessage;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.serialize.Serializer;

import java.util.Arrays;

/**
 * custom protocol decoder
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
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 * </p>
 *
 * @author wangtao
 * @createTime on 2020/10/2
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */

/**
 * 自定义解码器
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{

    /**
     * @param maxFrameLength      最大帧长度, 决定了可以接收的最大数据长度
     *                            如果超过，数据将被丢弃
     * @param lengthFieldOffset   长度字段偏移量, 长度字段是跳过指定字节长度的字段
     * @param lengthFieldLength   长度字段中的字节数
     * @param lengthAdjustment    添加到长度字段值的补偿值
     * @param initialBytesToStrip 跳过的字节数
     *                            如果需要接收所有的 header + body 数据，这个值为0
     *                            如果只想接收正文数据，则需要跳过 header 消耗的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public RpcMessageDecoder(){
        // lengthFieldOffset: magic code为4B，version为1B，然后全长, 所以值为 5
        // lengthFieldLength：全长为4B, 所以值为 4
        // lengthAdjustment: full length 包括所有数据，读取前9个字节，所以左边的长度是 (fullLength-9), 所以值为-9
        // initialBytesToStrip：我们将手动检查魔术代码和版本，所以不要剥离任何字节, 所以值为 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * 对数据进行解码
     * @param ctx 提供操作Channel的方法和属性
     * @param in 接收到的数据
     * @return 解码后的对象
     * @throws Exception
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 调用父类的decode方法对接收到的数据进行解码，得到解码后的对象
        Object decoded = super.decode(ctx, in);
        // 如果解码后得到的是ByteBuf类型
        // 在Netty中，数据通过ByteBuf来传输和操作
        if (decoded instanceof ByteBuf){
            // 将解码后得到的对象转换为ByteBuf类型
            ByteBuf frame = (ByteBuf) decoded;
            // 如果ByteBuf中的数据长度大于等于 16
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try{
                    // 对ByteBuf进行解码，得到解码后的对象
                    return decodeFrame(frame);
                } catch (Exception e) {
                    // 如果解码过程中出现异常，记录日志并抛出异常
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    // 释放ByteBuf
                    frame.release();
                }
            }
        }
        // 返回解码后的对象
        return decoded;
    }

    /**
     * 帧解码
     * @param in 解码后的ByteBuf
     * @return 解析后的RpcMessage对象
     */
    private Object decodeFrame(ByteBuf in) {
        // 校验魔数
        checkMagicNumber(in);
        // 校验版本号
        checkVersion(in);
        // 读取消息总长度
        int fullLength = in.readInt();
        // 读取消息类型、编解码方式、压缩方式、请求ID
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        // 根据读取到的信息构建RpcMessage对象
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        // 若是心跳请求消息，则设置数据为PING并返回RpcMessage对象
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        // 若是心跳响应消息，则设置数据为PONG并返回RpcMessage对象
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        // 计算消息体长度
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        // 若消息体长度大于0，则读取消息体
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // 解压缩
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            // 反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        // 返回RpcMessage对象
        return rpcMessage;
    }

    /**
     * 检查协议版本
     * @param in 接收到的ByteBuf
     */
    private void checkVersion(ByteBuf in) {
        // 读取协议版本号
        byte version = in.readByte();
        // 判断协议版本号是否和常量中定义的版本号一致
        if (version != RpcConstants.VERSION) {
            // 抛出异常，提示协议版本不兼容
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    /**
     * 检查魔数
     * @param in 接收到的数据流
     */
    private void checkMagicNumber(ByteBuf in) {
        // 读取前4位，即魔数，并进行比较
        // 魔数的长度
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        // 从ByteBuf中读取魔数
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            // 如果读取的魔数与预定义的不一致，则抛出异常
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
