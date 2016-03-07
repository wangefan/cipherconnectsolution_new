package com.cipherlab.cipherconnect.sdk;

/**
 * Define the interface of CipherConnectControlListener.
 * @author visual.chen
 * @version 1.0
 * 
 */
public interface ICipherConnectControlListener {
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
	
	/**
	 * The user program will be notified
	 *  while CipherConnectControl is beginning
	 *   to connect to BT scanner device.
	 * 
	 * @param deviceName: BT scanner device name
	 */
	public void onBeginConnecting(String deviceName);
	/**
	 * The user program will be notified When BT scanner device sends the data to CipherConnectControl
	 * 
	 * @param deviceName : Active BT scanner device name
	 * @param barcode : A set of barcode data.
	 */
	public void onReceivingBarcode(String deviceName,String barcode);
	
	/**
	 * The user program will be notified
	 *  when the exception or error occurs
	 *   in CipherConnectControl while CipherConnectControl
	 *    is doing operations.
	 * 
	 * @param deviceName: BT scanner device name
	 * @param id,message : exception message list is follows:<br>
	 * 	               <UL>	
	 * 					  <li>0,Exception message.
	 * 					  <li>101,Please turn on Bluetooth
	 *					  <li>102,Cannot find any Bluetooth device
	 *					  <li>103,Cannot find [device-name]
	 *					  <li>104,Cannot turn off Bluetooth
	 *					  <li>105,Bluetooth connection error
	 *					  <li>106,The device is not the Cipherlab product
	 *					  <li>107,Cannot find any listener
	 *					  <li>108,Cannot find any services (for low energy mode)
	 *					<UL>
	 */
	public void onCipherConnectControlError(String deviceName,int id, String message);
	
	/**
	 * The user program will be notified
	 *  while CipherConnectControl is trying
	 *   to connect to BT scanner device.
	 * 
	 * @param deviceName: BT scanner device name
	 */
	public void onConnecting(String deviceName);
	
	/**
	 * The user program will be notified when CipherConnectControl is connected to the BT scanner device.
	 * 
	 * @param deviceName: BT scanner device name
	 */
	public void onConnected(String deviceName);
	
	/**
	 * The user program will be notified when CipherConnectControl is disconnected from the BT scanner device. 
	 * 
	 * @param deviceName: BT scanner device name
	 */
	public void onDisconnected(String deviceName);
	
	/**
	 * The user program will be notified when CipherConnectControl in scan LE mode and discover devices. 
	 * 
	 * @param ICipherConnBTDevice: bluetooth device.
	 */
	public void onGetLEDevice(final ICipherConnBTDevice device);
}
