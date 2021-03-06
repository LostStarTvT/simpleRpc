package com.dwj.rpc.test.server;

import com.dwj.rpc.server.RpcService;
import com.dwj.rpc.test.client.HelloService;

/**
 * Describe:
 *
 * @author Seven on 2020/5/25
 */
@RpcService(value = HelloService.class,version = "1.0")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
//        int i = 1/0; //抛出异常。
        return "Hello " + name;
    }
}
