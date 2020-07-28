package com.dwj.rpc.test;

import com.dwj.rpc.client.RpcProxy;
import com.dwj.rpc.test.client.HelloService;
import com.dwj.rpc.test.client.Person;
import com.dwj.rpc.test.client.PersonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

/**
 * Describe: 测试整个框架的有效性，
 *
 * @author Seven on 2020/5/25
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class RPCTest {
    @Autowired
    RpcProxy rpcProxy;

    /**
     * 测试返回string参数的代理
     */
    @Test
    public void HelloServiceTest() throws IOException {
        // 也可以通过版本进行控制。 但是二者要一样。
        HelloService helloService = rpcProxy.create(HelloService.class,"1.0");
        String result = helloService.sayHello("World");
        System.out.println(result);
        // System.in.read(); // 按任意键退出 让其监听到服务器节点上的信息。
    }

    /**
     * 测试对个返回参数的代理
     */
    @Test
    public void PersonServiceTest(){
        PersonService personService = rpcProxy.create(PersonService.class);
        List<Person> result2 = personService.GetTestPerson("小明",3);
        for (Person p : result2){
            System.out.println(p);
        }
    }
}
