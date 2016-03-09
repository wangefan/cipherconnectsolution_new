package com.cipherlab.cipherconnect.sdk2;

import android.graphics.Bitmap;

public interface ICipherConnCtrl2EZMet extends ICipherConnectControl2
{
	/**
	 * start to listen connection from remote device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Start listen devices to connect.<br>
	 * _control.startListening();<br>
	 * </DD>
	 * @return 	true : Start asynchronous Listen<br>
	 * 			false: Asynchronous Listen fail. 
	 */
    public boolean startListening();
    
    /**
	 * stop and clean all connections from remote device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * //Stop and clean all connections.<br>
	 * _control.stopListening();<br>
	 * </DD>
	 */
    public void stopListening(); 
    
    /**
	 * Generate code128 barcode bitmap to show MAC address for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.getMacAddrBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height. 
	 */
    public Bitmap getMacAddrBarcodeImage(int nWidth, int nHeight);
    
    /**
	 * Generate code128 barcode bitmap to reset connection command for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.getResetConnBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height.
	 */
    public Bitmap getResetConnBarcodeImage(int nWidth, int nHeight); 
    
    /**
	 * Generate code128 barcode bitmap to set connection command for Cipher device use. 
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * Bitmap img = _control.getSettingConnBarcodeImage(nWidth, nHeight);<br>
	 * </DD>
	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height.
	 */
    public Bitmap getSettingConnBarcodeImage(int nWidth, int nHeight); 
    
    /**
   	 * Generate QR code bitmap to setting connection command for connecting Cipher device use. 
   	 * <DT><B>code:</B><DT>
   	 * <DD>
   	 * Bitmap img = _control.getSettingConnQRCodeImage(nWidth, nHeight);<br>
   	 * </DD>
   	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height.
   	 */
     public Bitmap getSettingConnQRCodeImage(int nWidth, int nHeight);
     
     /**
 	 * Generate code128 bitmap to enable authentication command for Cipher device use. 
 	 * <DT><B>code:</B><DT>
 	 * <DD>
 	 * Bitmap img = _control.getEnableAuthBarcodeImage(nWidth, nHeight);<br>
 	 * </DD>
 	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height.
 	 */
     public Bitmap getEnableAuthBarcodeImage(int nWidth, int nHeight); 
     
     /**
  	 * Generate code128 bitmap to disable authentication command for Cipher device use. 
  	 * <DT><B>code:</B><DT>
  	 * <DD>
  	 * Bitmap img = _control.getDisableAuthBarcodeImage(nWidth, nHeight);<br>
  	 * </DD>
  	 * @param nWidth: specified bitmap width. 
	 *        nHeight: specified bitmap height.
  	 */
      public Bitmap getDisableAuthBarcodeImage(int nWidth, int nHeight);
      
      /**
    	 * Generate code128 bitmap to enabling slave/spp command for Cipher device use. 
    	 * <DT><B>code:</B><DT>
    	 * <DD>
    	 * Bitmap img = _control.getEnableSppBarcodeImage(nWidth, nHeight);<br>
    	 * </DD>
    	 * @param nWidth: specified bitmap width. 
    	 *        nHeight: specified bitmap height.
    	 */
      public Bitmap getEnableSppBarcodeImage(int nWidth, int nHeight);
}
