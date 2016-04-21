package main.java.hintcommit.driver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;

import javax.net.ssl.SSLException;


public class NettyClient extends Thread {
    static final boolean SSL = System.getProperty("ssl") != null;
    static int           port;
    static String        host;

    public ChannelPromise chPromise = null;

    public HintCommit            hcInstance = new HintCommit();
    public Channel               serverChannel;
    public Lock                  clock         = new ReentrantLock();
    public Condition             commited      = clock.newCondition();
    public Lock                  hlock         = new ReentrantLock();
    public Condition             hinted        = hlock.newCondition();
    public CyclicBarrier         updateBarrier = new CyclicBarrier(2);
    public ChannelHandlerContext ctx;
    private CountDownLatch       startupLatch;
    long startTime;
    long endTime;

    public NettyClient(CountDownLatch l, int port, String host)
    {
        startupLatch = l;
        this.port    = port;
        this.host    = host;
    }

    public void run()
    {
        // Configure SSL.git
        final SslContext sslCtx;

        if (SSL)
        {
            try {
                sslCtx = SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            }
            catch (SSLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            sslCtx = null;
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
               .channel(NioSocketChannel.class)
               .option(ChannelOption.TCP_NODELAY, true)
               .handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();

                    /*
                     * if (sslCtx != null) {
                     *  p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                     * }
                     */
                    //p.addLast(new LoggingHandler(LogLevel.INFO));
                    p.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    p.addLast("decoder", new StringDecoder());
                    p.addLast("encoder", new StringEncoder());
                    p.addLast(new NettyClientHandler(NettyClient.this));
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            serverChannel = f.channel();

            System.out.println("Netty started");
            startupLatch.countDown();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    /** Bull** */
    // public void flushUpdateCommand(String command)
    // {
    //     this.chPromise = this.serverChannel.newPromise();
    //
    //     this.serverChannel.writeAndFlush(command, chPromise);
    //     try {
    //         chPromise.await();
    //     } catch (Exception e)
    //     {
    //         e.printStackTrace();
    //     }
    // }
}
