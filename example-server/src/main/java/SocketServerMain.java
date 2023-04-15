import org.vinci.HelloService;
import org.vinci.config.RpcServiceConfig;
import org.vinci.remoting.transport.socket.SocketRpcServer;
import org.vinci.serviceimpl.HelloServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
