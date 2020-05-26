package com.dwj.rpc.register.zookeeper;

import com.dwj.rpc.common.util.CollectionUtil;
import com.dwj.rpc.register.Constant;
import com.dwj.rpc.register.ServiceDiscovery;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Describe:
 *
 * @author Seven on 2020/5/25
 */
public class ZooKeeperServiceDiscovery implements ServiceDiscovery, Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private String zkAddress;
    private CountDownLatch latch = new CountDownLatch(1);

    private final ZooKeeper zooKeeper;
    private volatile List<String> dataList = new ArrayList<>(); //保存获取到的列表。

    public ZooKeeperServiceDiscovery(String zkAddress) throws IOException {
        // 创建 ZooKeeper 客户端
        zooKeeper = new ZooKeeper(zkAddress, Constant.ZK_CONNECTION_TIMEOUT, this);

        try {

            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //看着结点。
        watchNode(zooKeeper);
    }

    // 现阶段的发现仅仅是发现服务器地址，和服务名称是没有关系的。
    @Override
    public String discover() throws IOException {
        //dataList 里面两个地址都是一样的，因为都是相同的东西。。
        int size = dataList.size();
        if (size == 1){
            return dataList.get(0);
        }else {
            return dataList.get(ThreadLocalRandom.current().nextInt(size));
        }

    }

    private void watchNode(final ZooKeeper zk) {
        try {
            // 将register下所有的节点进行遍历。
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            // 然后遍历节点下的值。
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;

            LOGGER.debug("Service discovery triggered updating connected server node.");

        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
            latch.countDown();
        }
    }
}
