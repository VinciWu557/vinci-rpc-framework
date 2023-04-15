package org.vinci.utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;


/**
 * 创建 ThreadPool(线程池) 的工具类
 */
@Slf4j
public class ThreadPoolFactoryUtil {
    /**
     * 通过 threadNamePrefix 来区分不同线程池
     * （我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {

    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        // 创建默认的线程池配置
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        // 调用下面的重载方法
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        // 调用下面的重载方法
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 如果不存在名为 threadNamePrefix 的线程池，则创建一个自定义的线程池
     * 如果已经存在名为 threadNamePrefix 的线程池，则返回已有线程池
     * 如果已有线程池已经被关闭或终止，那么将重新创建一个线程池并返回
     * @param customThreadPoolConfig 自定义线程池配置
     * @param threadNamePrefix 线程名前缀
     * @param daemon 是否为守护线程
     * @return 名为 threadNamePrefix 的线程池
     */
    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        // 根据 threadNamePrefix 从 THREAD_POOLS 中获取线程池
        // 如果不存在则创建一个新的线程池
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            // 先将原来的线程池从 THREAD_POOLS 中移除
            THREAD_POOLS.remove(threadNamePrefix);
            // 创建新的线程池
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            // 将新的线程池放入 THREAD_POOLS 中
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        // 遍历 THREAD_POOLS
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                // 在规定时间内等待所有线程池任务执行完毕
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // 如果等待过程中被中断，则立即关闭线程池
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 根据自定义的线程池配置参数和线程名前缀，创建一个线程池
     * @param customThreadPoolConfig 自定义的线程池配置参数
     * @param threadNamePrefix 线程名前缀
     * @param daemon 是否为守护线程
     * @return 创建好的线程池
     */
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        // 创建一个线程工厂，用于创建线程
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        // 创建一个线程池，并根据参数设置其各项属性
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory
     * 如果threadNamePrefix不为空则使用自建ThreadFactory
     * 否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
