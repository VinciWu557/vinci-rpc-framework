package org.vinci.utils;

public class RuntimeUtil {
    /**
     * 获取当前计算机的CPU的核心数
     * 该方法并不总是返回真实的处理器数量，而是返回当前可用的逻辑处理器数量
     * 以便于编写多线程程序或者其他需要知道 CPU 核心数量的程序
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
