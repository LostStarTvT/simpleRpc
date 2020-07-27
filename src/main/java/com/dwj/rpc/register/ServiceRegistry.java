package com.dwj.rpc.register;

import org.apache.zookeeper.KeeperException;

import java.util.List;

/**
 * Describe: 服务器端 服务注册
 *
 * @author Seven on 2020/5/25
 */
public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     * @param serviceAddress 服务地址
     * @param interfaceName  注册的接口全限定类名
     * @throws KeeperException
     * @throws InterruptedException
     */
    void register(String serviceAddress, List<String> interfaceName) throws KeeperException, InterruptedException;
}
