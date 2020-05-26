package com.dwj.rpc.test.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Describe: rpc框架的服务器启动程序。 在进行测试之前需要先
 *
 * @author Seven on 2020/5/25
 */
public class RpcBootStrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcBootStrap.class);

    public static void main(String[] args) {
        LOGGER.debug("start server");
        new ClassPathXmlApplicationContext("server-spring.xml");
        System.out.println("cccc");
    }
}
