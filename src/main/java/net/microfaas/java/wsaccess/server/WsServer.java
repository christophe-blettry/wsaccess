/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author christophe
 */
public class WsServer extends Thread {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
	private SslContext sslCtx = null;
	private final WatchJsFile watchJsFile;
	private static final Logger logger = LogManager.getLogger(WsFrameHandler.class);

	public WsServer(File jsFile) throws WsServerException {
		try {
			if (SSL) {
				try {
					final KeyStore serverKeyStore = KeyStore.getInstance("PKCS12");
					File file = new File("microfaas_net.p12");
					InputStream ksIs = new FileInputStream(file);
					serverKeyStore.load(ksIs, "microfaas.net".toCharArray());
					//serverKeyStore.load(getClass().getResourceAsStream("kestore.jks"), "microfaas.net".toCharArray());
					final KeyManagerFactory serverKeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					serverKeyManagerFactory.init(serverKeyStore, "microfaas.net".toCharArray());
					//SelfSignedCertificate ssc = new SelfSignedCertificate();
					//sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
					sslCtx = SslContextBuilder.forServer(serverKeyManagerFactory)
							.sslProvider(SslProvider.JDK)
							.trustManager(new File("keystore.pem"))
							.clientAuth(ClientAuth.OPTIONAL)
							.ciphers(null, IdentityCipherSuiteFilter.INSTANCE)
							.build();
				} catch (CertificateException | SSLException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
					throw new WsServerException(ex);
				}
			} else {
				sslCtx = null;
			}
			watchJsFile = new WatchJsFile(jsFile);
		} catch (IOException ex) {
			throw new WsServerException(ex);
		}
	}

	public WsServer ready() throws InterruptedException {
		this.watchJsFile.start();
		this.watchJsFile.getCountDownLatch().await();
		return this;
	}

	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup(10);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new WsServerInitializer(sslCtx, watchJsFile));

			Channel ch = b.bind(PORT).sync().channel();

			System.out.println("Open your web browser and navigate to "
					+ (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

			ch.closeFuture().sync();
		} catch (InterruptedException ex) {
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
