/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess;

import java.io.File;
import java.io.IOException;
import net.microfaas.java.wsaccess.server.WsServer;
import net.microfaas.java.wsaccess.server.WsServerException;

/**
 *
 * @author christophe
 */
public class NewMain {

	/**
	 * @param args the command line arguments
	 * @throws java.io.IOException
	 * @throws java.lang.InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException, WsServerException {
		WsServer wsServer = new WsServer(new File("script.js"));
		wsServer.ready().start();
	}
	
}
