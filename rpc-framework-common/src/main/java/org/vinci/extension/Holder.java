package org.vinci.extension;

/**
 * 这是一个泛型类 Holder<T> 的定义，用于存储一个对象的引用
 * 这个类有一个泛型参数 T，表示存储的对象的类型。它有两个公共方法：
 *      get()：用于获取存储的对象的引用
 *      set(T value)：用于设置存储的对象的引用。
 * 这个类还有一个私有成员变量 value，用于存储对象的引用
 * 这个成员变量被声明为 volatile，表示多个线程之间的可见性
 *
 * volatile 修饰符保证了该变量在多个线程之间的可见性，即一个线程修改了 value 的值，其他线程立即可以看到这个修改，从而避免了线程之间的数据不一致问题
 * 因此，在并发编程中，volatile 修饰符常用于保证变量的可见性
 *
 * 这个类通常被用作一个线程安全的容器，用于在多个线程之间共享一个对象的引用
 * 由于成员变量 value 被声明为 volatile，因此多个线程可以同时访问和修改这个变量，从而实现了多线程之间的数据共享和同步
 * @param <T>
 */
public class Holder <T>{

    private volatile T value;

    public T get(){
        return value;
    }

    public void set(T value){
        this.value = value;
    }
}
