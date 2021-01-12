import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server {
    public  void run() throws Exception {
        EventLoopGroup poolConnect = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        AuthService.connection();

//        AuthService.setNewUsers(1, "login1", "pass1", "nik1");
//        AuthService.setNewUsers(2, "login2", "pass2", "nik2");
//        AuthService.setNewUsers(3, "login3", "pass2", "nik3");

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(poolConnect, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new AuthService()
                            );
                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            poolConnect.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().run();
    }
}
