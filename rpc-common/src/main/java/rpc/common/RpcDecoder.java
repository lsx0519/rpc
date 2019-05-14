package rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private Class genericClass;

    public RpcDecoder(Class genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        int size = in.readableBytes();

        if (size < 4) {
            return;
        }

        byte[] data = new byte[size];

        in.readBytes(data);

        // 反序列化为对象(RPCRequest/RPCResponse对象)
        Object deserialize = SerializationUtil.deserialize(data, genericClass);

        list.add(deserialize);

        channelHandlerContext.flush();
    }
}
