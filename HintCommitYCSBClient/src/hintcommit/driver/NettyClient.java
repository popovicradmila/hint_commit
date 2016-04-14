package hintcommit.driver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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

import javax.net.ssl.SSLException;


public class NettyClient extends Thread{

	static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static int PORT;
     
    public HintCommit hcInstance = new HintCommit();
    public Channel serverChannel;
    public Lock clock = new ReentrantLock();
	public Condition commited = clock.newCondition();
    public Lock hlock = new ReentrantLock();
	public Condition hinted = hlock.newCondition();
	public ChannelHandlerContext ctx;
	private CountDownLatch startupLatch;
	long startTime;
	long endTime;
	
	public NettyClient(CountDownLatch l, int port){
		startupLatch = l;
		this.PORT = port;
	}
     
    public void run() {
       
        // Configure SSL.git
        final SslContext sslCtx;
        if (SSL) {
            try {
				sslCtx = SslContextBuilder.forClient()
				    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            sslCtx = null;
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     /*
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
                     }
                     */
                     //p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                     p.addLast("decoder", new StringDecoder());
                     p.addLast("encoder", new StringEncoder());
                     p.addLast(new NettyClientHandler(NettyClient.this));
                 }
             });

        // Start the client.
        ChannelFuture f = b.connect(HOST, PORT).sync();
        serverChannel = f.channel();
        
        System.out.println("Netty started");
        startupLatch.countDown(); 
        
        // Wait until the connection is closed.
        f.channel().closeFuture().sync();
    } catch(Exception e)
    {
    	e.printStackTrace();
    }
        finally {
        // Shut down the event loop to terminate all threads.
        group.shutdownGracefully();
    }	
    }

}
