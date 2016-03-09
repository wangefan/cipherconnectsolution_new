package com.cipherlab.cipherconnect.sdk2;

/**
 * Define the interface of CipherConnectControl2.
 * <DT><B>code:</B><DT>
 * <DD>
 *    ICipherConnectControl2 _control =  ICipherConnectControl2.createInst();
 * </DD>
 * @author yifan.wang
 * @version 1.0
 */
public interface ICipherConnectControl2 {
	/**
	 * Get the version of CipherConnectControl.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * String version = _control.getVersion();
	 * </DD>
	 * @return 1.0.0
	 */
	public String getVersion();
	
	/**
	 * Get the connected device firmware version.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * String version = _control.getFWVersion();
	 * </DD>
	 * @return Ver:1.31a
	 */
	public String getFWVersion();
	
	/**
	 * Get the connection status.
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
	 * ICipherConnBTDevice[] devices = _control.getBtDevices();
	 * </DD>
	 */
	public ICipherConnBTDevice[] getBtDevices();
	
	/**
	 * Connect to the BT scanner device.
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
	 * Connect to the BT scanner device by name and MAC address.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * try{<br>
	 * 		_control.connect(deviceName, deviceAddr);<br>
	 * }<br>
	 * catch(NullPointerException e){<br>
	 *      System.out.println(e);<br>
	 * }<br>
	 * </DD>
	 * @param deviceName: BT name and have been identified with this OS.
	 * @param deviceAddr: BT MAC address and have been identified with this OS. 
	 * @throws NullPointerException : if deviceName or deviceAddr is null, CipherConnectControl throws a NullPointerException.
	 */
    public void connect(String deviceName, String deviceAddr)throws NullPointerException;	
    
    /**
     * Disconnect from the BT scanner device.
     * <DT><B>code:</B><DT>
     * <DD>
     * _control.disconnect();
     * </DD>
     */
	public void disconnect();
	
	/**
	 * Monitor all events from CipherConnectControl2.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * try{<br>
	 * 		_control.addCipherConnectControl2Listener(new ICipherConnectControl2Listener(){<br>
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
	
	public void addCipherConnect2Listener(ICipherConnectControl2Listener listener)throws NullPointerException;
	
	/**
	 * Set auto Reconnect.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Set auto Reconnect.<br>
	 *<br>
	 * 		_control.setAuotReconnect(true);<br>
	 * <br>
	 * //Set autoReconnect stop. <br>
	 * <br>
	 * 		_control.setAuotReconnect(false);<br>
	 * <br>
	 * </DD>
	 * @param enable true | false 
	 * @param device BT scanner device and have been identified with this OS. 
	 */
	public void setAutoReconnect(boolean enable);
	
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
    public void setBLEMode(boolean bEnable) throws UnsupportedOperationException;
    
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
    public boolean startScanLEDevices() throws UnsupportedOperationException;
    
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
    public boolean stopScanLEDevices() throws UnsupportedOperationException;
    
    /**
	 * Reset the CipherConnectControl2 object status.
	 */
	public void reset();
	
    /**
	 * CipherConnectControl2 object should be closed before leaving service.
	 */
	public void close();
}
