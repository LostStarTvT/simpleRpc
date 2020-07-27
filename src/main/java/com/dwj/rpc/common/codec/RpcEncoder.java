package com.dwj.rpc.common.codec;

import com.dwj.rpc.common.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器  将object变成字节流。
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    // 获取到需要编码的数据。
    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        // object 从此处in，byteBuf从次数out，

        // 如果传递过来的是 genericClass类型的数据，则进行编码。
        if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}