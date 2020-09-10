package com.dwj.rpc.register.cluster.impl;

import com.dwj.rpc.register.cluster.ClusterStrategy;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * @Classname ClusterStrategy
 * @Description TODO
 * @Date 2020/8/3 16:01
 * @Created by Seven
 */
public class ClusterStrategyImpl implements ClusterStrategy {
    // 使用volatile
    private volatile static ClusterStrategyImpl uniqueInstance;
    private ClusterStrategyImpl() {
    }
    public static ClusterStrategy getUniqueInstance() {
        if (uniqueInstance == null) {
            synchronized (ClusterStrategyImpl.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new ClusterStrategyImpl();
                }
            }
        }
        return uniqueInstance;
    }


    @Override
    public synchronized String  select(List<String> providerIp) {
        if (providerIp.size() == 1){
            return providerIp.get(0);
        }else {
            int MAX_LEN = providerIp.size();
            int index = RandomUtils.nextInt(0, MAX_LEN - 1); // 随机选择一个数据进行。
            return providerIp.get(index);
        }

    }
}
