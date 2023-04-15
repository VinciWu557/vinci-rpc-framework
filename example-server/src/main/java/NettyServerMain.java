import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.vinci.HelloService;
import org.vinci.annotation.RpcScan;
import org.vinci.config.RpcServiceConfig;
import org.vinci.remoting.transport.netty.server.NettyRpcServer;
import org.vinci.serviceimpl.HelloServiceImpl2;

@RpcScan(basePackage = {"org.vinci"})
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // Register service manually
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
