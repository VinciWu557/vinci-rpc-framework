package org.vinci.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用于表示数据的压缩类型的枚举类
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    // 包含了一个枚举值 GZIP，该值表示使用 GZIP 压缩算法进行压缩
    // code -> name
    GZIP((byte) 0x01, "gzip");

    // 压缩类型的字节码
    private final byte code;
    // 压缩类型的字符串表示
    private final String name;

    // 根据传入的字节码获取压缩类型的字符串表示
    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
