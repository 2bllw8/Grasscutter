package emu.grasscutter.info;

import emu.grasscutter.Grasscutter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class InfoServer {
    public static final int HTTP_PORT = 8081;

    private InfoServer() {
    }

    public static void run() {
        // Multithreaded event loops
        final EventLoopGroup parentGroup = new NioEventLoopGroup();
        final EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new InfoServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start the channel
            final ChannelFuture channelFuture = bootstrap.bind(HTTP_PORT).sync();

            Grasscutter.getLogger().info("Started info web server at http://localhost:" +
                    HTTP_PORT);
            // Wait for server socket termination
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Grasscutter.getLogger().error("Failed to start info server", e);
        } finally {
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();
        }
    }
}
