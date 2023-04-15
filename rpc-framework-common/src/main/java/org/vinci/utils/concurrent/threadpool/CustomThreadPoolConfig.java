package org.vinci.utils.concurrent.threadpool;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池自定义配置类，可自行根据业务场景修改配置参数
 */
@Setter
@Getter
public class CustomThreadPoolConfig {
    /**
     * 线程池默认参数
     */
    // 线程池的默认核心线程数，为10
    private static final int DEFAULT_CORE_POOL_SIZE = 10;
    // 线程池的默认最大线程数，为100
    private static final int DEFAULT_MAXIMUM_POOL_SIZE_SIZE = 100;
    // 线程池默认的线程空闲超时时间，为1
    private static final int DEFAULT_KEEP_ALIVE_TIME = 1;
    // 线程空闲超时时间的默认单位，为分钟
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    // 线程池默认的阻塞队列容量，为100
    private static final int DEFAULT_BLOCKING_QUEUE_CAPACITY = 100;
    // 线程池阻塞队列的容量，同样为100
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    /**
     * 可配置参数
     */
    // 表示线程池中核心线程的数量，默认值为 10
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    // 表示线程池中最大线程数，默认值为 100
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE_SIZE;
    // 表示线程池的线程空闲超时时间，默认值为 1
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    private TimeUnit unit = DEFAULT_TIME_UNIT;
    // 使用有界队列, 缓存任务
    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
}
