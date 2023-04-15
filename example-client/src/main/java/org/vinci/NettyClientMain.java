package org.vinci;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.vinci.annotation.RpcScan;

// 声明扫描的 RPC 接口所在的基础包路径
@RpcScan(basePackage = {"org.vinci"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        // 创建基于注解配置的 Spring 应用上下文对象
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        // 从应用上下文中获取名为 "helloController" 的 Spring 组件
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        // 调用 helloController 对象的 test 方法
        helloController.test();
    }
}
