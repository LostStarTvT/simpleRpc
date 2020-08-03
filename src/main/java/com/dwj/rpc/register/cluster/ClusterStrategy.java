package com.dwj.rpc.register.cluster;

import java.util.List;

/**
 * @Classname ClusterStrategy
 * @Description TODO 负载均衡策略的实现方法。
 * @Date 2020/8/3 16:00
 * @Created by Seven
 */
public interface ClusterStrategy {

    /**
     *  从获取到的ip列表中找出一个Ip，然后进行负载均衡。
     * @param providerIp 服务提供者提供的Ip地址集合。
     * @return
     */
    public String select(List<String> providerIp);
}
