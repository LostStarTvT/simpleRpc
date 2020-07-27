package com.dwj.rpc.register;

/**
 *  对于zookeeper数据来说，通过/表示一个节点，
 *   zookeeper服务器的配置信息，和节点的设置信息。
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 1000;

    String ZK_REGISTRY_PATH = "/registry";
}
