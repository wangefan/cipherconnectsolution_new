package com.cipherlab.cipherconnect.sdk;

import android.graphics.Bitmap;

/**
 * Define the interface of CipherConnectControl.
 * <DT><B>code:</B><DT>
 * <DD>
 *    ICipherConnectControl _control =  CipherConnectControl.getCipherConnectControl();
 * </DD>
 * @author visual.chen
 * @version 1.0
 */
public interface ICipherConnectControl {
	/**
	 * Get the version of CipherConnectControl.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * String version = _control.getVersion();
	 * </DD>
	 * @return 1.0
	 */
	public String getVersion();
	
	/**
	 * Get the status of CipherConnectControl.
	 * <DT><B>code:</B><DT>
	 * <DD> 
	 * boolean bConnect = _control.isConnected();
	 * </DD>
	 * @return  true : CipherConnectControl is connected to the BT scanner device<br>
	 * 			false: CipherConnectControl is not connected to the BT scanner device
	 */
	public boolean isConnected();
	
	/**
	 * Get the all Bluetooth devices which have been identified with the OS.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * ICipherConnBTDevice[] sNames = _control.getBtDevices();
	 * </DD>
	 */
	public ICipherConnBTDevice[] getBtDevices();
	
	/**
	 * Connect to the BT scanner device by device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * try{<br>
	 * 		_control.connect(device);<br>
	 * }<br>
	 * catch(NullPointerException e){<br>
	 *      System.out.println(e);<br>
	 * }<br>
	 * </DD>
	 * @param device: BT scanner device and have been identified with this OS. 
	 * @throws NullPointerException : if device is null, CipherConnectControl throws a NullPointerException.
	 */
    public void connect(ICipherConnBTDevice device)throws NullPointerException;	
    
    /**
     * Disconnect from the BT scanner device.
     * <DT><B>code:</B><DT>
     * <DD>
     * _control.disconnect();
     * </DD>
     */
	public void disconnect();
	
	/**
	 * Define the interface of CipherConnectControl and monitor all events from CipherConnectControl.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * try{<br>
	 * 		_control.addCipherConnectControlListener(new ICipherConnectControlListener(){<br>
	 * 				public void onReceivingBarcode(String deviceName,final String barcode) {<br>
	 * 					System.out.println("onReceivingBarcode[deviceName="+deviceName+",barcode="+barcode+"]");<br>
	 * 				}<br>
	 * 				public void onCipherConnectControlError(String deviceName,int id,String message) {<br>
	 * 					System.out.println("onCipherConnectControlError(deviceName="+deviceName+",id="+id+",message="+message+")");<br>
	 * 				}<br>
	 * 				public void onConnecting(String deviceName) {<br>
	 * 					System.out.println("onConnecting[deviceName="+deviceName+"]");<br>
	 * 				}<br>
	 * 				public void onConnected(String deviceName) {<br>
	 * 					System.out.println("onConnected[deviceName="+deviceName+"]");<br>
	 * 				}<br>
	 * 				public void onDisconnected(String deviceName) {<br>
	 * 					System.out.println("onDisconnected[deviceName="+deviceName+"]");<br>
	 * 				}<br>
	 * 			});<br>
	 * }<br>
	 * catch(NullPointerException e){<br>
	 * 		System.out.println(e);<br>
	 * }<br>
	 * </DD>
	 * @param listener : the interface of CipherConnectControlListener.
	 * @throws NullPointerException : if listener is null,  CipherConnectControl throws a NullPointerException. 
	 */
	public void addCipherConnectControlListener(ICipherConnectControlListener listener)throws NullPointerException;
	
	/**
	 * Set auto Reconnect.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Set auto Reconnect.<br>
	 * try{<br>
	 * 		_control.setAuotReconnect(true, device);<br>
	 * }<br>
	 * catch(NullPointerException e){<br>
	 * 		System.out.println(e);<br>
	 * }<br>
	 * //Set autoReconnect stop. <br>
	 * try{<br>
	 * 		_control.setAuotReconnect(false,null);<br>
	 * }<br>
	 * catch(NullPointerException e){<br>
	 * 		System.out.println(e);<br>
	 * } <br>
	 * </DD>
	 * @param enable true | false 
	 * @param device BT scanner device and have been identified with this OS. 
	 * @throws NullPointerException If device is null, CipherConnectControl throws a NullPointerException. 
	 */
	public void setAutoReconnect(boolean enable, ICipherConnBTDevice device)throws NullPointerException;
	
	/**
	 * Get the status of AutoReconnect.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * boolean bAutoReconnect = _control.isAutoReconnect();
	 * </DD>
	 * @return 	true : CipherConnectControl is able to automatically re-connect to the BT scanner device.<br>
	 * 			false: CipherConnectControl is not able to automatically re-connect to the BT scanner device.
	 */
	public boolean isAutoReconnect();
	
	/**
	 * Get the supported status Low Energy mode.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * boolean bSupportBLE = _control.IsBLEModeSupported();
	 * </DD>
	 * @return 	true : CipherConnectControl support LE Mode.<br>
	 * 			false: CipherConnectControl does not support LE Mode..
	 */
	public boolean IsBLEModeSupported();
    
	/**
	 * Set LE mode.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Set Low energy mode.<br>
	 * try{<br>
	 * 		_control.SetBLEMode(true);<br>
	 * }<br>
	 * catch(UnsupportedOperationException e){<br>
	 * 		System.out.println(e);<br>
	 * }<br>
	 * //Set LE mode stop. <br>
	 * </DD>
	 * @param enable true | false  
	 * @throws UnsupportedOperationException If not support low energy mode. 
	 */
    public void SetBLEMode(boolean bEnable) throws UnsupportedOperationException;
    
	/**
	 * Start Scan LE Devices.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Start Scan LE Devices.<br>
	 * try{<br>
	 * 		_control.StartScanLEDevices();<br>
	 * }<br>
	 * catch(UnsupportedOperationException e){<br>
	 * 		System.out.println(e);<br>
	 * }<br>
	 * </DD>
	 * @return 	true : Start asynchronous Scan LE Devices.<br>
	 * 			false: Start asynchronous Scan LE Devices fail.
	 * @throws UnsupportedOperationException If not support low energy mode. 
	 */
    public boolean StartScanLEDevices() throws UnsupportedOperationException;
    
	/**
	 * Stop Scan LE Devices.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Stop Scan LE Devices.<br>
	 * try{<br>
	 * 		_control.StopScanLEDevices();<br>
	 * }<br>
	 * catch(UnsupportedOperationException e){<br>
	 * 		System.out.println(e);<br>
	 * }<br>
	 * </DD>
	 * @return 	true : Stop asynchronous Scan LE Devices.<br>
	 * 			false: Stop asynchronous Scan LE Devices fail.
	 * @throws UnsupportedOperationException If not support low energy mode. 
	 */
    public boolean StopScanLEDevices() throws UnsupportedOperationException;
    
    /**
	 * start to listen connection from remote device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Start listen devices to connect.<br>
	 * _control.StartListening();<br>
	 * </DD>
	 * @return 	true : Start asynchronous Listen<br>
	 * 			false: Asynchronous Listen fail. 
	 */
    public boolean StartListening();
    
    /**
	 * stop and clean all connections from remote device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Stop and clean all connections.<br>
	 * _control.Stop();<br>
	 * </DD>
	 */
    public void StopListening(); 
    
    /**
	 * Generate MAC address code128 for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.GetMacAddrBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 */
    public Bitmap GetMacAddrBarcodeImage(int nWidth, int nHeight);
    
    /**
	 * Generate Reset connection code128 command for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.GetResetConnBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 */
    public Bitmap GetResetConnBarcodeImage(int nWidth, int nHeight); 
    
    /**
	 * Generate setting connection code128 command for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.GetSettingConnBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 */
    public Bitmap GetSettingConnBarcodeImage(int nWidth, int nHeight); 
}
