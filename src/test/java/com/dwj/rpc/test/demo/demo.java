package com.dwj.rpc.test.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Describe: 测试zookeeper客户端基本操作逻辑。
 *
 * @author Seven on 2020/5/25
 */
public class demo implements Watcher {

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
        ZooKeeper z = demo.connect("192.168.60.130:2181",2000);
        System.out.println("OK"+z);
        demo.createNode(z,"/javacc","hello");
//        demo.getData(z,"/javacc");
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
            latch.countDown();
        }
    }

    //创建一个节点。
    public void createNode(ZooKeeper zooKeeper,String path,String content) throws KeeperException, InterruptedException {
        zooKeeper.create(path,content.getBytes(), ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    //查询一个节点。
    public void getData(ZooKeeper zooKeeper,String path) throws KeeperException, InterruptedException {
        System.out.println(new String(zooKeeper.getData(path,false,new Stat())));
    }
}

