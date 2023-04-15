package org.vinci.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.vinci.exception.SerializeException;
import org.vinci.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * HessianSerializer 是一个基于二进制的序列化器，用于将对象转换为二进制数据流
 * 一种动态类型、二进制序列化和 Web 服务协议, 专为面向对象的传输而设计
 */
public class HessianSerializer implements Serializer {

    /**
     * 将对象序列化为字节数组
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializeException 序列化异常
     */
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            // 创建输出流
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            // 将对象序列化到输出流中
            hessianOutput.writeObject(obj);
            // 返回序列化后的字节数组
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }

    }

    /**
     * 将字节数组反序列化为指定类型的对象
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @return 反序列化后的对象
     * @throws SerializeException 反序列化异常
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            // 创建输入流
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            // 从输入流中反序列化对象
            Object o = hessianInput.readObject();
            // 将反序列化得到的对象强制转换为指定类型
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
