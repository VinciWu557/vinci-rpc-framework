package org.vinci.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.vinci.exception.SerializeException;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化类，Kryo序列化效率很高，但只兼容Java语言
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * 因为 Kryo 不是线程安全的，所以使用 ThreadLocal 存储 Kryo 对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 注册需要序列化和反序列化的类
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // 使用Kryo对象将Java对象序列化为字节数组
            kryo.writeObject(output, obj);
            // 在序列化完成后，清除当前线程中的Kryo对象
            kryoThreadLocal.remove();
            // 将序列化结果输出为字节数组并返回
            return output.toBytes();
        } catch (Exception e) {
            // 抛出序列化异常
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // 使用Kryo对象将字节数组反序列化为Java对象
            Object o = kryo.readObject(input, clazz);
            // 在反序列化完成后，清除当前线程中的Kryo对象
            kryoThreadLocal.remove();
            // 将反序列化结果强制转换为指定的类并返回
            return clazz.cast(o);
        } catch (Exception e) {
            // 抛出反序列化异常
            throw new SerializeException("Deserialization failed");
        }
    }

}
