package com.github.codec;

import com.github.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 *  解码器 基础扩展Netty的ByteToMessageDecoder抽象类，并且实现其decode方法
 *
 * @author qinxuewu
 * @create 20/6/6上午10:12
 * @since 1.0.0
 */

public class RpcDecoder extends ByteToMessageDecoder {
    private  Class<?> genericClass;

    public  RpcDecoder(Class<?> genericClass){
        this.genericClass=genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if(in.readableBytes() <4 ){
            return;
        }

        in.markReaderIndex();
        int dataLength = in.readInt();
        if(dataLength < 0){
            ctx.close();
        }

        if(in.readableBytes() < dataLength){
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = SerializationUtil.deSerialize(data, genericClass);
        out.add(obj);

    }
}
