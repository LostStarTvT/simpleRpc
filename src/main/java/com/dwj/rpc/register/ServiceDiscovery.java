package com.dwj.rpc.register;

import java.io.IOException;

/**
 * Describe: 客户端的zookeeper服务发现
 *
 * @author Seven on 2020/5/25
 */
public interface ServiceDiscovery {

    /**
     * 根据接口名找到对应服务器的ip地址。
     * @param interfaceName  传递过来接口全名。
     * @return 返回从Map中找到的 服务提供地址。
     * @throws IOException
     */
    public String discover(String interfaceName) throws IOException;
}
