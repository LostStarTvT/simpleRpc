package com.dwj.rpc.common.codec;


import com.dwj.rpc.common.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC 解码器  将字节流变成对象。  需要将定义好的
 */
public class RpcDecoder extends ByteToMessageDecoder {

    // 具体的类传递进来。 带有？ 表示可以接收到任何的类， 即使是带有泛型的也可以进行接受。
    private Class<?> genericClass;

    // 将需要序列化的对象传递过来，直接传递过来一个类，
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 在这里进行操作ByteBuf这个类， 然后将其转换成对象出去。

        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        out.add(SerializationUtil.deserialize(data, genericClass));
    }
}
