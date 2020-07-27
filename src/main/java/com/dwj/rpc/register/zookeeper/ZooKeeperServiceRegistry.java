package com.dwj.rpc.register.zookeeper;


import com.dwj.rpc.register.Constant;
import com.dwj.rpc.register.ServiceRegistry;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Describe: 在zookeeper服务器上注册服务。 Server端使用
 *
 * @author Seven on 2020/5/25
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry, Watcher  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);
    private CountDownLatch latch = new CountDownLatch(1);

    private final ZooKeeper zooKeeper;

    // 还要将服务器传递过来。
    public ZooKeeperServiceRegistry(String zkAddress) throws IOException {

        // 初始化的时候就进行连接到zookeeper服务器。
        this.zooKeeper = new ZooKeeper(zkAddress, Constant.ZK_CONNECTION_TIMEOUT, this);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //  注册持久性registry节点 其中只需要serviceAddress 就行，而 service 无序注册。
    // 还需要传递 interfaceName。
    @Override
    public void register(String serviceAddress, List<String> interfaceName) throws KeeperException, InterruptedException {

        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;
        LOGGER.debug("create registry node: {}", registryPath);
        // 当没有注册的时候才会进行注册。
        if (null==zooKeeper.exists(registryPath, false)) {
            //  new byte[0] 表示该节点只是一个父节点，没有值。
            zooKeeper.create(registryPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        //创建Server节点  这是个临时节点。
        createNode(zooKeeper, serviceAddress, interfaceName);
    }

    //将服务器的地址保存进去， 在上面创建的/registry 节点下。
    private void createNode(ZooKeeper zk, String serviceAddress, List<String> interfaceName) {
        try {
            //
            byte[] AddressBytes = serviceAddress.getBytes();
            for (String name :interfaceName){
                //采用的连接模式为 断开连接的时候会自动的删除。 并且索引值为自动增加。
                // 创建节点并且将地址写进去。
//                String path = zk.create(Constant.ZK_DATA_PATH, AddressBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                String path = zk.create( Constant.ZK_REGISTRY_PATH + "/" + name, AddressBytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                LOGGER.debug("create zookeeper node ({} => {})", path, serviceAddress);
            }

//            byte[] bytes = serviceAddress.getBytes();
            //采用的连接模式为 断开连接的时候会自动的删除。 并且索引值为自动增加。
            // 创建节点并且将地址写进去。
//            String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
//            LOGGER.debug("create zookeeper node ({} => {})", path, serviceAddress);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }

    // 创建zookeeper同步所需要的同步锁。
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected){
            latch.countDown();
        }
    }
}
