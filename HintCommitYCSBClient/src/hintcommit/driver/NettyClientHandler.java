/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package hintcommit.driver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	/*
	 * @Override public void messageReceived(ChannelHandlerContext c, String s)
	 * throws Exception{
	 * 
	 * }
	 */
	NettyClient nc;
	
	
	/**
	 * Creates a client-side handler.
	 */
	public NettyClientHandler(NettyClient nc) {
		this.nc = nc;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (((String) msg).substring(0, 1).equals("h")) {
			nc.hcInstance.hint = ((String) msg).substring(1);
			nc.hlock.lock();
			nc.hinted.signal();
			nc.hlock.unlock();
			//nc.startTime = System.nanoTime();
		} else {
			if (((String) msg).substring(0, 1).equals("c")) {
				nc.hcInstance.commit = ((String) msg).substring(1);
				//nc.endTime = System.nanoTime();
				nc.clock.lock();
				nc.commited.signal();
				nc.clock.unlock();
			} else {
				System.out.println("Invalid data received");
			}
		}

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}