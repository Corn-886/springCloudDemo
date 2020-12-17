package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;

/**
 * netty 客户端配置
 */
public class clientServer {
    private final int port;
    private String host;

    public clientServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @SneakyThrows
    public void start() {
        //线程组，用于存放
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            //启动器
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)//指定NIO通信模式
                    .remoteAddress(new InetSocketAddress(host, port))//指定服务器地址
                    .handler(new ChannelInitializer<SocketChannel>() {
                        //实现继承方法，实现handler 方法
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new clientHandle());
                        }
                    });
            //异步链接到服务器，sync()会阻塞到完成
            ChannelFuture future = bootstrap.connect().sync();
            //阻塞当前线程，直到客户端的channel被关闭
            future.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        new clientServer(9999,"127.0.0.1").start();
    }
}
