package org.vinci.extension;

import lombok.extern.slf4j.Slf4j;
import org.vinci.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ExtensionLoader<T> {
    // SERVICE_DIRECTORY 定义配置文件的目录名
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    // 缓存 ExtensionLoader 实例
    // 将扩展点类型 Class 对象映射到对应的 ExtensionLoader 实例
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    // 缓存已经实例化的扩展实现类对象
    // 将扩展点类型 Class 对象映射到对应的实例化对象
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();
    // 扩展点类型，每个 ExtensionLoader 实例对应一个扩展点类型
    private final Class<?> type;
    // 缓存扩展实现类对象
    // 将扩展实现类的名称映射到对应的 Holder 对象
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    // 缓存扩展实现类的 Class 对象
    // 将扩展实现类的名称映射到对应的 Class 对象
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // ExtensionLoader 类构造函数
    // 接受一个 Class<?> 类型的参数 type，并将其赋值给实例对应的扩展点类型
    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    /**
     * 获取指定接口类型的扩展点加载器
     * @param type 接口类型
     * @return 扩展点加载器
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        // 检查传入的参数是否为空
        if (type == null){
            throw new IllegalArgumentException("Extension type should not be null.");
        }

        // 检查传入的参数是否是一个接口类型
        if (!type.isInterface()){
            throw new IllegalArgumentException("Extension type must be an interface.");
        }

        // 检查传入的参数是否被 @SPI 注解所修饰
        if (type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }

        // 获取该接口的 ExtensionLoader 对象
        // 用于加载和管理所有扩展点的实现，提供了一些方法来获取接口的所有实现以及获取默认实现等功能
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null){
            // 如果 ExtensionLoader 对象不存在
            // 则创建一个新的 ExtensionLoader 对象并将其加入到 EXTENSION_LOADERS 中
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            // 获取该接口的 ExtensionLoader 对象
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 根据扩展点的名称获取扩展点的实现
     * @param name 扩展点的名称
     * @return 扩展点的实现
     */
    public T getExtension(String name) {
        // 检查传入的扩展点名称是否为空
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // 首先从缓存中获取扩展点实现类对应的容器
        Holder<Object> holder = cachedInstances.get(name);
        // 如果未获取到缓存，则创建一个 Holder 容器
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果该容器中没有扩展点的实现，则创建一个并将其设置为单例
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        // 返回扩展点的实现
        return (T) instance;
    }

    /**
     * 根据扩展点名称创建扩展点实现
     *
     * @param name 扩展点名称
     * @return 创建的扩展点实现
     */
    private T createExtension(String name) {
        // 从文件中加载类型为 T 的所有扩展类，然后根据名称获取指定的扩展类
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("No such extension of name " + name);
        }

        // 如果还没有该类型的扩展点实现，则创建一个并将其设置为单例
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        // 返回创建的扩展点实现
        return instance;
    }

    /**
     * 获取已加载的扩展类
     *
     * @return 已加载的扩展类
     */
    private Map<String, Class<?>> getExtensionClasses() {
        // 从缓存中获取已加载的扩展类
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双重检查锁定，确保并发安全
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从扩展点目录加载所有扩展类
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 加载指定接口的所有扩展实现类
     * @param extensionClasses
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        // 拼接扩展点配置文件的名称
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            // 获取当前 ExtensionLoader 类的 ClassLoader
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            // 获取配置文件路径下的所有 URL 资源
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                // 遍历所有 URL 资源，并加载对应的配置文件中的扩展实现类
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    // 加载配置文件中的扩展点实现类
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            // 记录日志
            log.error(e.getMessage());
        }
    }

    /**
     * 从资源文件中加载扩展类，并将类名与扩展名对应，保存在 extensionClasses Map 中
     *
     * @param extensionClasses 存储扩展类的 Map 对象
     * @param classLoader 加载类的 ClassLoader
     * @param resourceUrl 资源文件 URL
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 逐行读取文件
            while ((line = reader.readLine()) != null) {
                // 查找注释符号的位置
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // # 后的字符串是注释，所以忽略它
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        // 获取等号索引
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // 我们的 SPI 使用键值对，因此两者都不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
