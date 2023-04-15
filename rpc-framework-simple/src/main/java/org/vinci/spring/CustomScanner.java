package org.vinci.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * 这个类是 Spring 框架中的一个扫描器，用于扫描指定包下面所有被指定注解标注的类，并将它们注册为 Spring 的 Bean。
 * 通过这个扫描器，可以轻松地将符合条件的类扫描到 Spring 容器中进行管理和维护，实现依赖注入和控制反转等功能
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    // 构造函数，接收 BeanDefinitionRegistry 和注解类型作为参数
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        // 调用父类构造函数
        super(registry);
        // 添加一个过滤器，扫描指定注解类型的类
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    /**
     * 重写 scan 方法，扫描指定的包路径下的类，并注册到 BeanDefinitionRegistry 中
     * @param basePackages
     * @return
     */
    @Override
    public int scan(String... basePackages) {
        // 调用父类 scan 方法进行扫描
        return super.scan(basePackages);
    }
}
