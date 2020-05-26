package com.dwj.rpc.register;

import org.apache.zookeeper.KeeperException;

/**
 * Describe: 服务器端 服务注册
 *
 * @author Seven on 2020/5/25
 */
public interface ServiceRegistry {
    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress) throws KeeperException, InterruptedException;
}
