package com.github.handler;

import com.github.bean.RpcRepsonse;
import com.github.bean.RpcRequest;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 处理Rpc请求 扩展 Netty 的SimpleChannelInboundHandler抽象类
 *
 * @author qinxuewu
 * @create 20/6/6上午10:22
 * @since 1.0.0
 */


public class RpcHandler  extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    /***
     * 通过构造方法传入 key=类的名称  Object=类的对象
     */
    private  final  Map<String,Object> handlerMap;
    public  RpcHandler(Map<String,Object> handerMap){
        this.handlerMap=handerMap;
    }

    /**
     * 当接收到消息时触发
     * @param ctx
     * @param request
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
        // 处理rpc请求 构造rpc响应返回
        RpcRepsonse repsonse=new RpcRepsonse();
        // 设置响应ID
        repsonse.setReponseId(request.getRequestId());
        try {
            // 处理rpc请求
            Object result=handle(request);
            repsonse.setResult(result);

        }catch (Exception e){
            // 设置异常信息
            repsonse.setError(e);
        }
        // 发送响应信息给请求方通道 并添加一个监听器，以检测是否所有数据包都被发送，然后关闭该通道
        ctx.writeAndFlush(repsonse).addListener(ChannelFutureListener.CLOSE);

    }


    private  Object handle(RpcRequest request) throws InvocationTargetException {
        // 获取rpc请求 调用class类名
        String className=request.getClassName();
        // 获取Class名 对应的对象
        Object serviceBean=handlerMap.get(className);

        //获取反射所需要的参数
        Class<?> serviceClass=serviceBean.getClass();
        // 方法名
        String methodName=request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        // 获取参数
        Object[] parameters = request.getParameters();

        /*cglib反射，可以改善java原生的反射性能*/
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    /**
     * 异常捕获
     * @param ctx 上下文
     * @param cause 异常对象
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception...", cause);
        ctx.close();
    }
}
