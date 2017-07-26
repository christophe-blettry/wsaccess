/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author christophe
 */
public class WsServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final String WEBSOCKET_PATH = "/websocket";

	private static final Logger logger = LogManager.getLogger(WsServerInitializer.class);
	private final SslContext sslCtx;
	private final WatchJsFile watchJsFile;

	public WsServerInitializer(SslContext sslCtx, WatchJsFile watchJsFile) {
		this.sslCtx = sslCtx;
		this.watchJsFile = watchJsFile;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			/*SSLEngine engine = sslCtx.newEngine(ch.alloc());
			engine.setUseClientMode(false);
			engine.setNeedClientAuth(true);
			pipeline.addLast("ssl",new SslHandler(engine));
			*/
			pipeline.addLast("ssl",sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new WebSocketServerCompressionHandler());
		pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
		pipeline.addLast(new WsIndexPageHandler(WEBSOCKET_PATH));
		pipeline.addLast(new WsFrameHandler(this.watchJsFile));
	}
}
