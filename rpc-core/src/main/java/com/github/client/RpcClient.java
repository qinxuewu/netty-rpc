package com.github.client;

import com.github.bean.RpcRepsonse;
import com.github.bean.RpcRequest;
import com.github.codec.RpcDecoder;
import com.github.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC客户端
 *
 * @author qinxuewu
 * @create 20/6/6上午11:20
 * @since 1.0.0
 */


public class RpcClient extends SimpleChannelInboundHandler<RpcRepsonse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
    /*主机名*/
    private String host;
    /*端口*/
    private int port;
    /*RPC响应对象*/
    private RpcRepsonse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRepsonse response) throws Exception {
       // 收到服务端返回消息
        this.response = response;
        synchronized (obj){
            //收到响应，唤醒线程
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception...", cause);
        ctx.close();
    }

    public RpcRepsonse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建并初始化Netty客户端bootstrap对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    /*将RPC请求进行编码（发送请求）*/
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    /*将RPC响应进行解码（返回响应）*/
                                    .addLast(new RpcDecoder(RpcRepsonse.class))
                                    /*使用RpcClient发送RPC请求*/
                                    .addLast(RpcClient.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            //连接RPC服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //写入RPC请求数据
            future.channel().writeAndFlush(request).sync();

            synchronized (obj){
                //未收到响应，使线程继续等待
                obj.wait();
            }

            // 程序被唤醒 关闭rpc连接
            if(null != response){
                //关闭RPC请求连接
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
