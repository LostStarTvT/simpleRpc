package com.dwj.rpc.test.server;

import com.dwj.rpc.server.RpcService;
import com.dwj.rpc.test.client.HelloService;

/**
 * Describe:
 *
 * @author Seven on 2020/5/25
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
