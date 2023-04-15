package org.vinci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表示数据的序列化类型的枚举类
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    // 包含了三个枚举值，分别表示使用 Kryo、Protostuff、Hessian 三种序列化框架进行序列化
    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0X03, "hessian");

    // 分别表示序列化类型的字节码和字符串表示
    private final byte code;
    private final String name;

    /**
     * 根据字节码获取对应的序列化类型的名称
     *
     * @param code 序列化类型的字节码
     * @return 序列化类型的名称
     */
    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
