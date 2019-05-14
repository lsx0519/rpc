package com.lsx.rpc.register;

import lombok.Data;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Data
public class RpcRegistry {
    public static final Logger LOGGER= LoggerFactory.getLogger(RpcRegistry.class);

    private String registryAddress;

    private ZooKeeper zooKeeper;

    public void createNode(String data) throws IOException {
        zooKeeper = new ZooKeeper(registryAddress, Constant.SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {

            }
        });

        if (zooKeeper != null) {
            try {
                Stat stat = zooKeeper.exists(Constant.REGISTRY_PATH,false);
                if (stat == null) {
                    //创建一个持久的节点目录
                    zooKeeper.create(Constant.REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }

                /*
                * CreateMode.PERSISTENT：持久化目录节点
                * CreateMode.EPHEMERAL_SEQUENTIAL: 临时自动编号节点
                * */
                zooKeeper.create(Constant.DATA_PATH,data.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);


            } catch (KeeperException e) {
                LOGGER.error("", e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                LOGGER.error("", e);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RpcRegistry rpcRegistry = new RpcRegistry();
        rpcRegistry.setRegistryAddress("localhost:2181");
        rpcRegistry.createNode("lsx");
        //让程序等待输入,程序一直处于运行状态
        System.in.read();
    }
}
