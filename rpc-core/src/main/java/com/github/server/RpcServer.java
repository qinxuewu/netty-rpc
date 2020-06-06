package com.github.server;

import com.github.bean.RpcRepsonse;
import com.github.bean.RpcRequest;
import com.github.codec.RpcDecoder;
import com.github.codec.RpcEncoder;
import com.github.handler.RpcHandler;
import com.github.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 *  rpc服务器
 *
 * @author qinxuewu
 * @create 20/6/6上午10:53
 * @since 1.0.0
 */

public class RpcServer  implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    /**
     *  服务地址
     */
    private String serverAddress;

    /**
     * 服务注册中心
     */
    private ServiceRegistry serviceRegistry;


    /**
     * 存放接口名与服务对象之间的映射关系
     */
    private Map<String, Object> handlerMap = new HashMap<>();

    /**
     * 构造器注入
     * @param serverAddress
     * @param serviceRegistry
     */
    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 将在所有的属性被初始化后调用。但是会在init前调用
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求

        // 启动netty服务
        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            // 创建并初始化 Netty 服务端 Bootstrap 对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //将RPC请求进行解码（为了处理请求）
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    //将RPC请求进行编码（为了返回响应）
                                    .addLast(new RpcEncoder(RpcRepsonse.class))
                                    //处理RPC请求
                                    .addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);


            //解析IP地址和端口信息
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            //启动RPC服务端
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            LOGGER.debug("启动RPC服务端  port: {}", port);

            if(null != serviceRegistry){
                //注册服务地址
                serviceRegistry.register(serverAddress);
                LOGGER.debug("注册 service:{}", serverAddress);
            }

            //关闭RPC服务器
            channelFuture.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 优雅关闭
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有带带@RpcService注解的Spring Bean
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(null !=serviceBeanMap && serviceBeanMap.size() >0){
            for (Object serviceBean : serviceBeanMap.values()){
                // 获取rpc服务实现类的名称
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);

            }
        }
        LOGGER.debug(" 获取所有带@RpcService注解的Spring Bean{}",handlerMap);
    }
}
