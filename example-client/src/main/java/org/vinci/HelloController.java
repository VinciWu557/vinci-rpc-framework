package org.vinci;

import org.springframework.stereotype.Component;
import org.vinci.annotation.RpcReference;

@Component
public class HelloController {

    // 声明该属性为 RPC 引用，指定版本号和分组名称

    @RpcReference(version = "version1", group = "test1")
    // 引用的 RPC 服务接口
    private HelloService helloService;

    public void test() throws InterruptedException {
        // 调用 RPC 服务接口的 hello 方法，并传入参数 Hello("111", "222")，返回结果为字符串类型
        String hello = this.helloService.hello(new Hello("111", "222"));
        // 如需使用 assert 断言，需要在 VM options 添加参数：-ea
        // 使用断言判断返回结果是否符合预期
        assert "Hello description is 222".equals(hello);
        // 线程休眠 12 秒钟
        Thread.sleep(12000);
        // 循环调用 RPC 服务接口的 hello 方法，并传入相同的参数，输出返回结果
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
