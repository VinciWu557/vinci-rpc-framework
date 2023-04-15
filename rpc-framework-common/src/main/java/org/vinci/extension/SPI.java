package org.vinci.extension;

import java.lang.annotation.*;

// @Documented 表示该注解应该被 javadoc 工具记录，并包含在生成的文档中，方便开发者查看该注解的用法和说明
@Documented
// @Retention(RetentionPolicy.RUNTIME) 指定了 @SPI 的生命周期是运行时
// 它可以被保留到运行时，并能够通过反射机制获取到注解信息
@Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.TYPE) 指定了该注解只能用于类上
// 表示该注解只能用于标识一个类是某个扩展点的实现类
@Target(ElementType.TYPE)
// 注解 @SPI 用于指定一个扩展点接口或抽象类的默认实现类，该实现类的类名需要在扩展点配置文件中进行配置
// 比如，在 Dubbo 中，@SPI 注解用于标识各种扩展点的默认实现类，比如负载均衡算法、序列化方式等
// 这个注解的属性值为空，表示不指定默认实现类，需要在扩展点配置文件中显式地指定
// 这个注解的作用是告诉框架或工具，该接口或抽象类的实现类需要按照特定的加载策略进行加载，从而方便开发者对扩展点进行配置和管理
public @interface SPI {
}
