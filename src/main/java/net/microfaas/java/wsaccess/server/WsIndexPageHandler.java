/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author christophe
 */
public class WsIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final String websocketPath;
	private static final Logger logger = LogManager.getLogger(WsIndexPageHandler.class);

    public WsIndexPageHandler(String websocketPath) {
        this.websocketPath = websocketPath;
}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		logger.debug("request: {}",msg);
	}
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		logger.debug("evt: {}",evt.getClass().getName());
		if (evt instanceof SslHandshakeCompletionEvent) {
			fetchCertificate(ctx);
		}
	}
	private void fetchCertificate(ChannelHandlerContext ctx) {
		SslHandler sslhandler = (SslHandler) ctx.channel().pipeline().get("ssl");
		logger.debug("sslhandler.applicationProtocol {}" ,sslhandler.applicationProtocol());
		logger.debug("sslhandler.getEnabledProtocols {}" ,String.join(",",sslhandler.engine().getEnabledProtocols()));
		logger.debug("sslhandler.getLocalCertificates {}" ,sslhandler.engine().getSession().getLocalCertificates()[0].getPublicKey());
		try {
			sslhandler.engine().getSession().getPeerCertificateChain()[0].getSubjectDN();
		} catch (SSLPeerUnverifiedException ex) {
			logger.error(ex);
		}
	}
	
}
