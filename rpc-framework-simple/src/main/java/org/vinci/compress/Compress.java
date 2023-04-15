package org.vinci.compress;

import org.vinci.extension.SPI;

/**
 * 压缩算法接口
 */
@SPI
public interface Compress {

    /**
     * 压缩字节数组
     * @param bytes 需要压缩的字节数组
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩字节数组
     * @param bytes 需要解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] bytes);
}
