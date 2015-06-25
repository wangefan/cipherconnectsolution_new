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
    
    /**
   	 * Generate setting connection 2D code command for connecting Cipher device use. 
   	 * <DT><B>code:</B><DT>
   	 * <DD>
   	 * Bitmap img = _control.GetSettingConnQRCodeImage(nWidth, nHeight);<br>
   	 * </DD>
   	 */
     public Bitmap GetSettingConnQRCodeImage(int nWidth, int nHeight);
     
     /**
 	 * Generate enabling authentication code128 command for Cipher device use. 
 	 * <DT><B>code:</B><DT>
 	 * <DD>
 	 * Bitmap img = _control.GetEnableAuthBarcodeImage(nWidth, nHeight);<br>
 	 * </DD>
 	 */
     public Bitmap GetEnableAuthBarcodeImage(int nWidth, int nHeight); 
     
     /**
  	 * Generate disabling authentication code128 command for Cipher device use. 
  	 * <DT><B>code:</B><DT>
  	 * <DD>
  	 * Bitmap img = _control.GetDisableAuthBarcodeImage(nWidth, nHeight);<br>
  	 * </DD>
  	 */
      public Bitmap GetDisableAuthBarcodeImage(int nWidth, int nHeight);
      
      /**
    	 * Generate Enabling slave/spp code128 command for Cipher device use. 
    	 * <DT><B>code:</B><DT>
    	 * <DD>
    	 * Bitmap img = _control.GetEnableSppBarcodeImage(nWidth, nHeight);<br>
    	 * </DD>
    	 */
        public Bitmap GetEnableSppBarcodeImage(int nWidth, int nHeight);
}
