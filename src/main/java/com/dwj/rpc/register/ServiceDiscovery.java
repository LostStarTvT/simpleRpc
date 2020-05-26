package com.dwj.rpc.register;

import java.io.IOException;

/**
 * Describe: 客户端的zookeeper服务发现
 *
 * @author Seven on 2020/5/25
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址  但是现阶段并没有实现根据服务名称查询。
     *

     * @return 服务地址
     */
    public String discover() throws IOException;
}
