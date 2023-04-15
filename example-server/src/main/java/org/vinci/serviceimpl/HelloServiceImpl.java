package org.vinci.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.vinci.Hello;
import org.vinci.HelloService;
import org.vinci.annotation.RpcService;

@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}