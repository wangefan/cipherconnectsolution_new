package com.cipherlab.cipherconnect2.sdk;

import android.graphics.Bitmap;

public interface ICipherConnCtrl2EZMet extends ICipherConnectControl2
{
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