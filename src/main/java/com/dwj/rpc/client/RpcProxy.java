package com.dwj.rpc.client;

import com.dwj.rpc.common.bean.RpcRequest;
import com.dwj.rpc.common.bean.RpcResponse;
import com.dwj.rpc.common.util.StringUtil;
import com.dwj.rpc.register.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * Describe: rpc 的对应代理类。
 *
 * @author Seven on 2020/5/25
 */
public class RpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    // 通过放射获取。
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());

                        //获取到的接口名称 com.dwj.rpc.test.client.HelloService
                        request.setInterfaceName(method.getDeclaringClass().getName());

                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        // 获取 RPC 服务地址
                        // 可以做个缓存。 即使用 HashMap也可以。
                        // 通过interface的全限定类名进行回去服务器的名称， 需要加上这个version字段
                        if (serviceDiscovery != null) {
                            String interFaceName = method.getDeclaringClass().getName();
                            // 需要判断 serviceVersion 是否为空。
                            if (StringUtil.isNotEmpty(serviceVersion)) {
                                interFaceName += "-" + serviceVersion;
                            }
                            serviceAddress = serviceDiscovery.discover(interFaceName);
                        }
                        //如果不使用zookeeper则需要将这个不进行注解。
                        //serviceAddress = "127.0.0.1:8000";
                        // 如果地址为空，则直接的丢出来错误。
                        if (StringUtil.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty");
                        }
                        // 从 RPC 服务地址中解析主机名与端口号
                        String[] array = StringUtil.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        // 创建 RPC 客户端对象并发送 RPC 请求
                        // 使用地址和端口进行数据连接，
                        RpcClient client = new RpcClient(host, port);

                        long time = System.currentTimeMillis();
                        //但是这个获取到的response总是为空
                        RpcResponse response = client.initNetty(request);
                        LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);

                        // 这里面如果有异常的话，应该重试，比如说重新选择一个服务器进行连接。
                        // 这里出现返回为null， 则需要进行重试。如果充实了5次比如说，那么就需要进行抛出异常。
                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        // 返回RPC响应结果
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }

}
