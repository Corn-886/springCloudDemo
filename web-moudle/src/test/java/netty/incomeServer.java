package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;

public class incomeServer {
    private final int port;

    public incomeServer(int port) {
        this.port = port;
    }

    @SneakyThrows
    private void start(){
        final incomeHandler incomeHandler = new incomeHandler();
        /*线程组*/
        EventLoopGroup group  = new NioEventLoopGroup();
        try {
            /*服务端启动必备*/
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)/*指定使用NIO的通信模式*/
                    .localAddress(new InetSocketAddress(port))/*指定监听端口*/
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(incomeHandler);
                        }
                    });
            ChannelFuture f = b.bind().sync();/*异步绑定到服务器，sync()会阻塞到完成*/
            f.channel().closeFuture().sync();/*阻塞当前线程，直到服务器的ServerChannel被关闭*/
        } finally {
            group.shutdownGracefully().sync();

        }
    }
    public static void main(String[] args) throws InterruptedException {
        int port = 9999;
        incomeServer echoServer = new incomeServer(port);
        System.out.println("服务器即将启动");
        echoServer.start();
        System.out.println("服务器关闭");
    }
}
