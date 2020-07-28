package com.dwj.rpc.register.zookeeper;


import com.dwj.rpc.register.Constant;
import com.dwj.rpc.register.ServiceDiscovery;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Describe: 发现zookeeper上的服务。 Client端使用。
 *
 * @author Seven on 2020/5/25
 */
public class ZooKeeperServiceDiscovery implements ServiceDiscovery, Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private final CountDownLatch latch = new CountDownLatch(1);

    private final ZooKeeper zooKeeper;

    // 记录缓存，同步HashMap进行存储数据。
    private final Map<String, String> serviceMap = new ConcurrentHashMap<>();

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

    // 根据接口的名称发现对应服务器的ip地址和端口。 每次获取都是从HashMap中获取，避免多次连接。
    @Override
    public String discover(String interfaceName) throws IOException {
        // 直接从数据中进行获取。
        return serviceMap.get(interfaceName);
    }

    // 每次开始的时候会直接的进行连接。获取到所有的数据。
    // 实现监听ZK_REGISTRY_PATH的child节点，当节点进行变化的时候便会回调这个方法，然后进行重新更新HashMap。
    private void watchNode(final ZooKeeper zk) {
        serviceMap.clear(); //每次在进行检测子节点的变化的时候，需要先将Map 更新，然后在进行重新赋值。 所以要使用 ConcurrentHashMap
        // 进行数据的同步
        try {
            // 将register下所有的节点进行遍历。
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, event -> {

                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    watchNode(zk); // 这时候会重新的调用方法，然后进行更新状态。
                    LOGGER.debug("Watcher:", "子节点的值发生了变化");
                }
            });

            // 然后遍历registry节点下所有的节点。 并且使用HashMap缓存。
            for (String node : nodeList) {
                // 在获取节点的时候需要使用监听器。
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, true, null);
                String value = new String(bytes);
                // 存到HashMap中。
                serviceMap.put(node,value);
                // Map :{com.dwj.rpc.test.client.PersonService : 127.0.0.1:8000}
            }
            LOGGER.debug("serviceMap data: {}", serviceMap);
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
