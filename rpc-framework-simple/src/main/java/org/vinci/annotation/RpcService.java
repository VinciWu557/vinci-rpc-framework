package org.vinci.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
/**
 * RPC 服务注册
 */
public @interface RpcService {

    /**
     * 服务版本，默认为空串
     */
    String version() default "";

    /**
     * 服务组，默认为空串
     */
    String group() default "";

}
