package com.lsx.rpc.service;

import com.lsx.rpc.register.RpcRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.lsx.rpc.common.RpcDecoder;
import com.lsx.rpc.common.RpcEncoder;
import com.lsx.rpc.common.RpcRequest;
import com.lsx.rpc.common.RpcResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private Map<String, Object> serviceBeanMap =  new HashMap<>();

    private RpcRegistry rpcRegistry;

    private String serverAddress;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取到所有拥有特定注解的Beans Map
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object object : serviceBeanMap.values()) {
                //获取到类的路径名称
                String serviceName = object.getClass().getAnnotation(RpcService.class).value().getName();

                this.serviceBeanMap.put(serviceName, object);
            }
            System.out.println("服务器: "+serverAddress +" 提供的服务列表: "+ serviceBeanMap );
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline()
                            .addLast(new RpcDecoder(RpcRequest.class))
                            .addLast(new RpcEncoder(RpcResponse.class))
                            .addLast(new ServerHandler(serviceBeanMap));
                }
            }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            String host=serverAddress.split(":")[0] ;//获取到主机地址
            int port=Integer.valueOf(serverAddress.split(":")[1]);//端口

            ChannelFuture future = serverBootstrap.bind(host, port).sync();//开启异步通信服务

            System.out.println("服务器启动成功:"+future.channel().localAddress());

            rpcRegistry.createNode(serverAddress);

            System.out.println("向zkServer注册服务地址信息");

            future.channel().closeFuture().sync();//等待通信完成

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
