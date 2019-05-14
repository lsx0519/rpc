package com.lsx.rpc.register;

import lombok.Data;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class RpcDiscover {
    public static final Logger LOGGER= LoggerFactory.getLogger(RpcRegistry.class);

    private String registryAddress;

    private volatile List<String> dataList = new ArrayList<>();

    private ZooKeeper zooKeeper = null;

    public RpcDiscover(String registryAddress) throws IOException {
        this.registryAddress = registryAddress;
        this.zooKeeper = new ZooKeeper(registryAddress, Constant.SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    watchNode();
                }
            }
        });
        watchNode();
    }

    public String discover() {
        int size = dataList.size();
        if (size>0) {
            int index = new Random().nextInt(size);
            return dataList.get(index);
        }
        throw new RuntimeException("没有找到对应的服务器");
    }

    private void watchNode() {
        try {
            //zooKeeper.getChildren  获取指定 path 下的所有子目录节点
            List<String> nodeList = zooKeeper.getChildren(Constant.REGISTRY_PATH,true);
            List<String> dataList = new ArrayList<>();

            for (String node : nodeList) {
                byte[] bytes = zooKeeper.getData(Constant.REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            this.dataList = dataList;
        } catch (KeeperException e) {
            LOGGER.error("", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOGGER.error("", e);
            e.printStackTrace();
        }
    }

    //测试程序
    public static void main(String[] args) throws Exception {
        //打印获取到的连接地址信息
        System.out.println(new RpcDiscover("localhost:2181").discover());
        System.in.read();
    }


}
