package org.vinci.serialize.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.vinci.serialize.Serializer;

public class ProtostuffSerializer implements Serializer {

    /**
     * 避免每次序列化时重新申请缓冲区空间
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /*
    在Protostuff中，Schema是一个接口，用于描述Java对象的字段结构和序列化信息。
    每个Java类都需要一个对应的Schema对象来描述它的序列化信息

    Schema包含了Java对象的所有字段信息，包括字段名、字段类型、以及序列化和反序列化时的操作信息等
    它可以将Java对象转换为字节数组，也可以将字节数组转换为Java对象

    在Protostuff中，通过使用RuntimeSchema类和ProtobufSchema类来生成Schema。
    其中，RuntimeSchema类是通过反射生成Schema，而ProtobufSchema类是通过.proto文件生成Schema。
    通过Schema，可以更加灵活地实现Java对象的序列化和反序列化
     */
    @Override
    public byte[] serialize(Object obj) {
        // 获取对象的Class对象
        Class<?> clazz = obj.getClass();
        // 获取Class对象的Schema对象
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes;
        try {
            // 使用ProtostuffIOUtil工具类将Java对象序列化为字节数组
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            // 清空缓冲区
            BUFFER.clear();
        }
        // 返回序列化结果
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // 获取Class对象的Schema对象
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        // 创建新的对象实例
        T obj = schema.newMessage();
        // 使用ProtostuffIOUtil工具类将字节数组反序列化为Java对象
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        // 返回反序列化结果
        return obj;
    }
}
