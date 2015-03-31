package com.cipherlab.cipherconnect.sdk;

public interface ICipherConnCtrl2EZMetListener extends ICipherConnectControl2Listener 
{
	/**
	 * The user program will be notified
	 *  while CipherConnectControl listen service
	 * is online.
	 */
	public void onListenServerOnline();
	
	/**
	 * The user program will be notified
	 *  while CipherConnectControl listen service
	 * is offline.
	 */
	public void onListenServerOffline();
}
