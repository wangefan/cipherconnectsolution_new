package com.cipherlab.cipherconnect.sdk2;

/**
 * Define the interface of CipherConnectControlListener.
 * @author visual.chen
 * @version 1.0
 * 
 */
public interface ICipherConnectControl2Listener 
{
	/**
	 * The user program will be notified
	 *  while CipherConnectControl is beginning
	 *   to connect to BT scanner device.
	 * 
	 * @param device: BT scanner device 
	 */
	public void onBeginConnecting(ICipherConnBTDevice device);
	
	/**
	 * The user program will be notified When BT scanner device sends the barcode data to CipherConnectControl
	 * 
	 * @param device: Active BT scanner device 
	 * @param barcode : A set of barecode data.
	 */
	public void onReceivingBarcode(ICipherConnBTDevice device, String barcode);
	
	/**
	 * The user program will be notified When BT scanner device sends minimize keyboard command.
	 *
	 */
	public void onMinimizeCmd();
	
	/**
	 * The user program will be notified
	 *  when the exception or error occurs
	 *   in CipherConnectControl while CipherConnectControl
	 *    is doing operations.
	 * 
	 * @param device: BT scanner device
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
	public void onCipherConnectControlError(ICipherConnBTDevice device,int id, String message);
	
	/**
	 * The user program will be notified
	 *  while CipherConnectControl is trying
	 *   to connect to BT scanner device.
	 * 
	 * @param device: BT scanner device
	 */
	public void onConnecting(ICipherConnBTDevice device);
	
	/**
	 * The user program will be notified when CipherConnectControl is connected to the BT scanner device.
	 * 
	 * @param device: BT scanner device 
	 */
	public void onConnected(ICipherConnBTDevice device);
	
	/**
	 * The user program will be notified when CipherConnectControl is disconnected from the BT scanner device. 
	 * 
	 * @param device: BT scanner device 
	 */
	public void onDisconnected(ICipherConnBTDevice device);
	
	/**
	 * The user program will be notified when CipherConnectControl in scan LE mode and discover devices. 
	 * 
	 * @param ICipherConnBTDevice: bluetooth device.
	 */
	public void onGetLEDevice(final ICipherConnBTDevice device);
}
