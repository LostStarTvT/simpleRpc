package com.dwj.rpc.register.zookeeper;


import com.dwj.rpc.register.Constant;
import com.dwj.rpc.register.ServiceRegistry;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

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
        if (null == zooKeeper.exists(registryPath, false)) {
            //  new byte[0] 表示该节点只是一个父节点，没有值。
            zooKeeper.create(registryPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        // 注册服务节点。
        for (String name:interfaceName) {
            // 需要将所有的接口服务发布出去。
            String InterfacePath = registryPath + "/" + name ;
            // InterfacePath = registry/com.dwj.rpc.test.client.HelloService-1.0
            if (null == zooKeeper.exists(InterfacePath,false)){ // 如果没有该节点就进行注册。
                zooKeeper.create(InterfacePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); //创建持久节点。
            }
            // 建立Provider节点，
            String providerPath = InterfacePath + Constant.ZK_PROViDE_PATH;
            // providerPath = registry/com.dwj.rpc.test.client.HelloService-1.0/provider
            if (null == zooKeeper.exists(providerPath,false)){
                zooKeeper.create(providerPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); //创建持久节点。
            }

            // 此时在进行注册服务器地址，这时候如果有新的服务器提供者运行，那么也会直接的增加新的数据。
            String addressPath = providerPath + "/" + serviceAddress;
            // addressPath = registry/com.dwj.rpc.test.client.HelloService-1.0/provider/127.0.0.1:8000;
            if (null == zooKeeper.exists(addressPath,false)){
                zooKeeper.create(addressPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL); //创建临时节点。
            }
            LOGGER.debug("create zookeeper node ({} => {})", addressPath, serviceAddress);
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
