package org.vinci.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.vinci.annotation.RpcReference;
import org.vinci.annotation.RpcService;
import org.vinci.config.RpcServiceConfig;
import org.vinci.extension.ExtensionLoader;
import org.vinci.factory.SingletonFactory;
import org.vinci.provider.ServiceProvider;
import org.vinci.provider.impl.ZkServiceProviderImpl;
import org.vinci.proxy.RpcClientProxy;
import org.vinci.remoting.transport.RpcRequestTransport;

import java.lang.reflect.Field;

@Slf4j
@Component // 声明为 Spring 组件
public class SpringBeanPostProcessor implements BeanPostProcessor {

    // 服务提供者
    private final ServiceProvider serviceProvider;
    // RPC 请求发送者
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        // 通过单例工厂获取服务提供者实例
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        // 通过 SPI 获取指定的 RPC 请求发送者实例
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    // 在 Bean 初始化之前进行处理
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 如果 Bean 上面有 @RpcService 注解，则将其注册到服务注册中心
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 构建 RPC 服务配置类
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            // 将 RPC 服务配置信息注册到服务注册中心
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    // 在 Bean 初始化之后进行处理
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取目标类的 Class 对象
        Class<?> targetClass = bean.getClass();
        // 获取目标类中所有声明的属性
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            // 如果属性上面有 @RpcReference 注解，则进行远程调用并设置属性值
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 构建 RPC 服务配置类
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                // 创建 RPC 代理对象
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                // 获取远程调用的代理对象
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    // 设置属性值
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
