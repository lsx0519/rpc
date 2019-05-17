package com.lsx.rpc.client;

import com.lsx.rpc.common.RpcDecoder;
import com.lsx.rpc.common.RpcEncoder;
import com.lsx.rpc.common.RpcRequest;
import com.lsx.rpc.common.RpcResponse;
import com.lsx.rpc.register.RpcDiscover;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;


@Data
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private RpcRequest rpcRequest;

    private RpcResponse rpcResponse;

    private Object object = new Object();

    private RpcDiscover rpcDiscover;


    public RpcClient(RpcRequest rpcRequest, RpcDiscover rpcDiscover) {
        this.rpcRequest = rpcRequest;

        this.rpcDiscover = rpcDiscover;
    }

    public RpcResponse send() {
        Bootstrap client = new Bootstrap();

        NioEventLoopGroup loopGroup = new NioEventLoopGroup();

        client.group(loopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder(RpcRequest.class))
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(RpcClient.this);//发送请求对象
                    }
                }).option(ChannelOption.SO_KEEPALIVE,true);


        return null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.rpcResponse=msg;//响应消息
        synchronized (object){
            ctx.flush();//刷新缓存
            object.notifyAll();//唤醒等待
        }
    }
}
