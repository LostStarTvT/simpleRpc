package com.dwj.rpc.test.demo;

import com.dwj.rpc.register.Constant;
import com.dwj.rpc.register.zookeeper.ZooKeeperServiceDiscovery;
import com.dwj.rpc.test.client.HelloService;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Describe: 测试zookeeper客户端基本操作逻辑。
 *
 * @author Seven on 2020/5/25
 */
public class demo implements Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(demo.class);
    private CountDownLatch latch = new CountDownLatch(1);

    //连接zookeeper服务器。
    public ZooKeeper connect(String connectStr, int timeout) throws IOException {

        ZooKeeper zooKeeper = new ZooKeeper(connectStr, timeout, this);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    public static void main(String[] args) throws Exception {
        demo demo = new demo();
        ZooKeeper z = demo.connect("192.168.249.128:2181",2000);
        System.out.println("OK"+z);
        System.out.println(HelloService.class.getName());
//        demo.createNode(z,"/javacc", HelloService.class.getName());
        demo.getData(z, Constant.ZK_REGISTRY_PATH + "/" + HelloService.class.getName());
        System.in.read();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
            latch.countDown();
            LOGGER.debug("node data: {}", "子节点变化了");
        }
    }

    //创建一个节点。
    public void createNode(ZooKeeper zooKeeper,String path,String content) throws KeeperException, InterruptedException {
        zooKeeper.create(path,content.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    //查询一个节点。
    public void getData(ZooKeeper zooKeeper,String path) throws KeeperException, InterruptedException {
        System.out.println(new String(zooKeeper.getData(path,true,new Stat())));
    }
}

