package org.vinci.compress.gzip;

import org.vinci.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {
    // 缓冲区大小
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * 压缩字节数组
     * @param bytes 需要压缩的字节数组
     * @return 压缩后的字节数组
     */
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            // 如果字节数组为 null，则抛出空指针异常
            throw new NullPointerException("bytes is null");
        }
        // 建立字节输出流
        // 建立 GZIP 压缩输出流
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            // 将字节数组写入 GZIP 压缩输出流
            gzip.write(bytes);
            // 刷新缓冲区
            gzip.flush();
            // 结束压缩流，写入 GZIP 文件尾
            gzip.finish();
            // 返回压缩后的字节数组
            return out.toByteArray();
        } catch (IOException e) {
            // 如果出现 IO 异常，则抛出运行时异常
            throw new RuntimeException("gzip compress error", e);
        }
    }

    /**
     * 解压缩字节数组
     * @param bytes 需要解压缩的字节数组
     * @return 解压缩后的字节数组
     */
    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            // 如果字节数组为 null，则抛出空指针异常
            throw new NullPointerException("bytes is null");
        }

        // 建立输出流
        // 建立 GZIP 解压输入流
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            // 建立缓冲区
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            // 持续读取 GZIP 解压输入流
            while ((n = gunzip.read(buffer)) > -1) {
                // 将解压后的数据写入输出流
                out.write(buffer, 0, n);
            }
            // 返回解压后的字节数组
            return out.toByteArray();
        } catch (IOException e) {
            // 如果出现 IO 异常，则抛出运行时异常
            throw new RuntimeException("gzip decompress error", e);
        }
    }
}
