package org.vinci.remoting.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.vinci.enums.CompressTypeEnum;
import org.vinci.enums.RpcResponseCodeEnum;
import org.vinci.enums.SerializationTypeEnum;
import org.vinci.factory.SingletonFactory;
import org.vinci.remoting.constants.RpcConstants;
import org.vinci.remoting.dto.RpcMessage;
import org.vinci.remoting.dto.RpcRequest;
import org.vinci.remoting.dto.RpcResponse;
import org.vinci.remoting.handler.RpcRequestHandler;

@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    // 初始化 RpcRequestHandler
    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                // 设置序列化方式和压缩方式
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    // 处理心跳请求
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    // 处理 RPC 请求
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // 执行目标方法（客户端需要执行的方法）并返回方法结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        // 封装 RPC 响应并设置到 RpcMessage 的 data 字段中
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        // 如果 Channel 不可写，则封装一个失败的 RPC 响应
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                // 将 RpcMessage 写入 Channel 中，同时添加监听器以在操作失败时关闭 Channel
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            // 确保释放 ByteBuf，否则可能会导致内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    // 用户事件触发器，在超时时关闭 Channel
    /*
    Netty提供了一个IdleStateHandler类，可以用于在指定的时间间隔内检测空闲状态事件，并触发相应的操作。
    在这个方法中，我们检测到IdleStateEvent（空闲状态事件）时, 判断其类型是否为READER_IDLE（读取空闲状态）
    如果是，则表示长时间没有收到客户端的消息，此时我们会关闭该Channel连接
    如果事件类型不是IdleStateEvent，则将事件传递给父类进行处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    // 异常处理，打印异常信息并关闭 Channel
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
