package org.vinci.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单例对象的工厂类
 */
public class SingletonFactory {

    /**
     * ConcurrentHashMap 是一个线程安全的哈希表
     * 它可以在并发访问的情况下，提供高效的、安全的访问
     * 它是 HashMap 的线程安全版本，并且比 Hashtable 更高效
     *
     * 与 Hashtable 不同，ConcurrentHashMap 不使用全局锁
     * 而是将哈希表分成了若干个段（Segment）
     * 每个段都有一个锁，这样不同的线程可以同时访问不同的段，从而提高了并发度
     *
     * ConcurrentHashMap 支持高效的并发访问操作
     * 比如 get()、put()、remove() 等操作
     * 同时，它也支持多种遍历方式
     * 包括迭代器、批量操作和 Stream 操作等，可以满足不同的需求
     *
     * ConcurrentHashMap 也支持并发地更新或者替换一个键值对
     * 当多个线程同时更新一个键时，只有一个线程能够成功，其他线程会自旋等待。
     *
     * 需要注意的是，ConcurrentHashMap 并不保证所有的操作都是原子的
     * 它只能保证操作的原子性是在单个桶内，而非整个哈希表
     * 如果需要实现一些复杂的操作
     * 比如“如果不存在，则插入”，还需要使用其他原子性操作来实现
     *
     * 在 Java 8 中，ConcurrentHashMap 进行了一些改进
     * 比如使用 CAS 操作来实现读取操作的非阻塞式访问，从而提高了并发度和性能
     */
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    public SingletonFactory() {
    }

    // 获取某个类的单例对象
    public static <T> T getInstance(Class<T> c){
        if(c == null){
            throw new IllegalArgumentException();
        }

        String key = c.toString();

        if (OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        } else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    // 在创建单例对象时，该方法使用了类 Class 的反射机制
                    // 通过调用 getDeclaredConstructor 方法获取该类的无参构造方法
                    // 并通过调用 newInstance 方法创建该类的实例
                    return c.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }

    }
}
