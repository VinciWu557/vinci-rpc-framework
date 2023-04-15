package org.vinci.annotation;

import org.springframework.context.annotation.Import;
import org.vinci.spring.CustomScannerRegister;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegister.class)
@Documented
/**
 * 用于扫描自定义注释
 */
public @interface RpcScan {

    String[] basePackage();

}
