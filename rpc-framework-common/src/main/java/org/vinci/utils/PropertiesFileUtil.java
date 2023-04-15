package org.vinci.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public final class PropertiesFileUtil {
    private PropertiesFileUtil() {
    }

    /**
     * 读取指定文件名的属性文件，并将属性值存储在一个 Properties 对象中并返回该对象
     * @param fileName
     * @return
     */
    public static Properties readPropertiesFile(String fileName) {
        // 获取当前线程的上下文类加载器的根路径, 存储为 url
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            // 将文件名与 url 拼接成完整的文件路径
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        // 打开该文件，并读取其内容
        // 使用 StandardCharsets.UTF_8 字符集将文件的字节流转换为字符流
        // 并将其传递给 Properties 对象的 load 方法，以便将属性值读取到 Properties 对象中
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}
