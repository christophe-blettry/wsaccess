/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Locale;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author christophe
 */
public class WsFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final Logger logger = LogManager.getLogger(WsFrameHandler.class);
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	private Invocable invocable;
	private final WatchJsFile watchJsFile;
	private int fileVersion;

	public WsFrameHandler(WatchJsFile watchJsFile) throws FileNotFoundException, ScriptException {
		this.watchJsFile = watchJsFile;
		reloadJs();
	}

	private void reloadJs() throws FileNotFoundException, ScriptException {
		this.engine.eval(new FileReader(this.watchJsFile.getFile()));
		this.invocable = (Invocable) engine;
		this.fileVersion = this.watchJsFile.getFileVersion();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		if (this.fileVersion != this.watchJsFile.getFileVersion()) {
			reloadJs();
		}
		if (frame instanceof TextWebSocketFrame) {
			// Send the uppercase string back.
			String request = ((TextWebSocketFrame) frame).text();
			Object result = invocable.invokeFunction("isConnectFrame", request);
			if (result instanceof Boolean && ((Boolean) result)) {
				logger.debug("result type of {}, value {}", result.getClass().getName(), result);
				Object vid = invocable.invokeFunction("getVid", request);
			}
			ctx.channel().writeAndFlush(new TextWebSocketFrame(result.toString().toUpperCase(Locale.US)));
		} else {

		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof SslHandshakeCompletionEvent) {
			fetchCertificate(ctx);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx); //To change body of generated methods, choose Tools | Templates.
	}

	private void fetchCertificate(ChannelHandlerContext ctx) {
		SslHandler sslhandler = (SslHandler) ctx.channel().pipeline().get("ssl");
		try {
			sslhandler.engine().getSession().getPeerCertificateChain()[0].getSubjectDN();
		} catch (SSLPeerUnverifiedException ex) {
			logger.error(ex);
		}
	}

}
