/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

/**
 *
 * @author christophe
 */
public class WsServerException extends Exception {

	public WsServerException() {
	}

	public WsServerException(String message) {
		super(message);
	}

	public WsServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public WsServerException(Throwable cause) {
		super(cause);
	}

}
