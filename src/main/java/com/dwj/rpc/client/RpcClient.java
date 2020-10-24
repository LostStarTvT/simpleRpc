package com.dwj.rpc.client;

import com.dwj.rpc.common.bean.RpcRequest;
import com.dwj.rpc.common.bean.RpcResponse;
import com.dwj.rpc.common.codec.RpcDecoder;
import com.dwj.rpc.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describe: 自定义RPC客户端，并且将自己定义的请求体和响应体绑定。
 *
 * @author Seven on 2020/5/25
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private final String host;
    private final int port;

    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private ChannelHandlerContext ctx = null;
    // send 以后就是使用这个东西进行获取。
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        // 在这里面获取chx可以服用发送数据
        // channelHandlerContext.writeAndFlush(new RpcRequest());// 发送新的请求。
        this.response = rpcResponse;
        this.ctx = channelHandlerContext;
        // 其实可以通过在这里面进行 下一个接口回调，然后将用户需要的任务注册进来，然后外面进行回调。
    }
    // 体现人的水平和性能的一个比较好的做法就是异常的处理方式。

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.close();
        // 然后在这里面也可以获取到异常，
    }

    public RpcResponse callMethod(RpcRequest request){

        ChannelFuture channelFuture = ctx.writeAndFlush(request);

        return  response;
    }
    /**
     *  发送数据到服务器。 竟然是每次都重新建立一个netty连接。。按理说应该是复用这个通道，然后就可能出现异步的结果？
     * @param request 发送请求
     * @return 返回响应数据
     * @throws Exception
     */
    public RpcResponse initNetty(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建并初始化 Netty 客户端 Bootstrap 对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求
                    pipeline.addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应
                    pipeline.addLast(RpcClient.this); // 处理 RPC 响应
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // 连接 RPC 服务器  TCP连接 返回一个连接成功的future。
            ChannelFuture future = bootstrap.connect(host, port).sync();

            // 可以通过添加 Listen的方式进行实现rpc的异步回调。 即使用当前的IO线程， 或者就是使用线程池的方法进行异步的回调。
            // 写入 RPC 请求数据并关闭连接
            Channel channel = future.channel();

            // 将数据发送过去。
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            // 返回 RPC 响应对象
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
