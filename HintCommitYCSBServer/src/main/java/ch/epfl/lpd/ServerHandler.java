
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
package main.java.ch.epfl.lpd;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


  /**
   * Handler implementation for the echo server.
   */
  @Sharable
  public class ServerHandler extends ChannelInboundHandlerAdapter {

      public static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

      @Override
      public void channelActive(ChannelHandlerContext ctx) {
      }

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	  try {
    		  App.client.run((String)msg, ctx);
    	    } finally {
    	    	ReferenceCountUtil.release(msg);
    	    }
      }

      @Override
      public void channelReadComplete(ChannelHandlerContext ctx) {
          //ctx.flush();
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
          // Close the connection when an exception is raised.
          logger.error("Got exception", cause);
          ctx.close();
      }
  }
