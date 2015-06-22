package com.cipherlab.cipherconnect2.sdk;

import java.util.ArrayList;

import com.cipherlab.help.CipherLog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;

abstract public class CipherConnCtrlmplBase {
	//Data members
	private final String mTAG = "CipherConnCtrlmplBase"; 
	protected Context mContext = null;
	protected ArrayList<ICipherConnectControl2Listener> mListenerList = null;
	protected Bitmap mMACAddressBitmap = null;
	protected Bitmap mResetConnBitmap = null;
	protected Bitmap mSettingConnBitmap = null;
	protected Bitmap mSettingConnQRCodeBitmap = null;
	protected boolean  mBHasConnection = false;
	
	//for auto re-connect
	protected boolean mBAuoReconnect = false;
	private Handler mHandlerCheckConn = new Handler();
	protected ICipherConnBTDevice mAutoConnDevice = null;
	//for auto re-connect end
	
	protected Handler mMainThrdHandler = new Handler();
	public CipherConnCtrlmplBase(Context context) {
		mContext = context;
		mBAuoReconnect = false;
	}
	
	public void SetCipherConnectControlListener(ArrayList<ICipherConnectControl2Listener> listenerList) throws NullPointerException {
		mListenerList = listenerList;
	}
	
	public boolean StartScanLEDevices() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public boolean StopScanLEDevices() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	protected void fireCipherBeginConnectControl(ICipherConnBTDevice device) {
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				l.onBeginConnecting(device);
			}
		}
	}
	
	protected void fireReceivingBarcode(ICipherConnBTDevice device,String barcode){
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				//CipherLog.e("CipherConnectControl","fireReceivingBarcode(deviceName="+deviceName+", barcode="+barcode+")");
				l.onReceivingBarcode(device, barcode);
			}
		}
	}
	
	protected void fireMinimizeCmd()
	{
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				l.onMinimizeCmd();
			}
		}
	}
	
	protected void fireConnecting(ICipherConnBTDevice device){
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				CipherLog.e("CipherConnectControl","fireConnecting(deviceName="+device.getDeviceName()+")");
				l.onConnecting(device);
			}
		}
	}
	
	protected void fireConnected(ICipherConnBTDevice device){
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				CipherLog.e("CipherConnectControl","fireConnected(deviceName="+device.getDeviceName()+")");
				l.onConnected(device);
			}
		}
	}
	
	protected void fireCipherConnectControlError(ICipherConnBTDevice device, int id, String message){
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				CipherLog.e("CipherConnectControl","fireCipherConnectControlError(deviceName="+device.getDeviceName()+", id="+id+", message="+message+")");
				l.onCipherConnectControlError(device, id, message);
			}
		}
	}

	protected void fireDisconnected(ICipherConnBTDevice device){
		if(mListenerList == null)
			return;
		for (ICipherConnectControl2Listener l : this.mListenerList) {
			if(l!=null)
			{
				CipherLog.e("CipherConnectControl","fireDisconnected(deviceName="+device.getDeviceName()+")");
				l.onDisconnected(device);
			}
		}
	}
	
	protected void setHasConnectionInMainThrd(boolean bHasConn) 
	{
		final boolean bPass = bHasConn;
		mMainThrdHandler.post(new  Runnable() {
			public void run()
			{
				mBHasConnection = bPass;
			}
		});
	}
	
	private Bitmap mGenerateBCodeBMP(String strContent, int nWidth, int nHeight)
	{
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF000000;
		
		MultiFormatWriter writer = new MultiFormatWriter();
	    BitMatrix result = null;
	    try {
	        result = writer.encode(strContent, BarcodeFormat.CODE_128, nWidth, nHeight, null);
	    } catch (IllegalArgumentException iae) {
	    	iae.printStackTrace();
	        return null;
	    } catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	    int width = result.getWidth();
	    int height = result.getHeight();
	    int[] pixels = new int[width * height];
	    for (int y = 0; y < height; y++) {
	        int offset = y * width;
	        for (int x = 0; x < width; x++) {
	        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
	        }
	    }

	    Bitmap bmp = Bitmap.createBitmap(width, height,
	        Bitmap.Config.ARGB_8888);
	    bmp.setPixels(pixels, 0, width, 0, 0, width, height);
	    return bmp; 
	}
	
	private Bitmap mGenerateQRCodeBMP(String strContent, int nWidth, int nHeight)
	{
		BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;
		QRCodeWriter  qrCodeWriter = new QRCodeWriter();
		BitMatrix result = null;
	    try {
	    	result = qrCodeWriter.encode(strContent, barcodeFormat, nWidth, nHeight);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    Bitmap bmp = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.RGB_565);
	    for (int x = 0; x < nWidth; x++){
	        for (int y = 0; y < nHeight; y++){
	            bmp.setPixel(x, y, result.get(x,y) ? Color.BLACK : Color.WHITE);
	        }
	    }
	    return bmp; 
	}
	
	//abstract methods
	abstract public void Reset();
	
	public abstract boolean isConnected();
	
	public abstract ICipherConnBTDevice[] getBtDevices();
		
	public abstract void connect(ICipherConnBTDevice device)throws NullPointerException;	
	
	public abstract void connect(String deviceName, String deviceAddr)throws NullPointerException;
	    
	public abstract void disconnect();
	
	public abstract boolean StartListening();
	
	public abstract void StopListening();
	
	// @param: boolean bSetTimer
	// true: set timer with 8secs to connect again, 
	// false: remove timer and all pending runnable.
	protected void mSetCheckConnTimer(boolean bSetTimer) {
		if(bSetTimer) {
			final int CHECK_TIME_STAMP = 8000;
			Runnable checkConn = new Runnable() {
				@Override
				public void run(){
					if(mAutoConnDevice != null)
					{
						CipherLog.d(mTAG, "mSetCheckConnTimer true, try to re-connect");
						connect(mAutoConnDevice);
					}
					else
					{
						CipherLog.d(mTAG, "mSetCheckConnTimer true, but no device to re-connect");
					}
				}
			};
			
			mHandlerCheckConn.postDelayed(checkConn, CHECK_TIME_STAMP);
		}
		else {
			mHandlerCheckConn.removeCallbacksAndMessages(null);
		}
	}
	
	public void setAutoReconnect(boolean enable) throws NullPointerException 
	{
		mBAuoReconnect = enable;
		if(mBAuoReconnect == false)
			mSetCheckConnTimer(false);
	}

	public boolean isAutoReconnect() {
		return mBAuoReconnect;
	}
	
	public Bitmap GetMacAddrBarcodeImage(int nWidth, int nHeight)
	{
		if(mMACAddressBitmap == null)
		{
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			if(btAdapter == null)
				return null; 
					
		    String strLocalMACAdres = btAdapter.getAddress();
		    if(strLocalMACAdres == null || strLocalMACAdres.isEmpty())
		    	return null; 
		    
		    strLocalMACAdres = strLocalMACAdres.replace(":", "");
		    strLocalMACAdres = "0x" + strLocalMACAdres;
	
		    mMACAddressBitmap = mGenerateBCodeBMP(strLocalMACAdres, nWidth, nHeight);
		}
	    return mMACAddressBitmap; 
	}
	
	public Bitmap GetResetConnBarcodeImage(int nWidth, int nHeight)
	{
		if(mResetConnBitmap == null)
		{
			String strResetConnCmd = "#@100003#";
			mResetConnBitmap = mGenerateBCodeBMP(strResetConnCmd, nWidth, nHeight);
		}
		
		return mResetConnBitmap;
	}
	
	public Bitmap GetSettingConnBarcodeImage(int nWidth, int nHeight)
	{
		if(mSettingConnBitmap == null)
		{
			String strSettingConnCmd = "88686471166254";
			mSettingConnBitmap = mGenerateBCodeBMP(strSettingConnCmd, nWidth, nHeight);
		}
		
		return mSettingConnBitmap;
	}
	
	public Bitmap GetSettingConnQRCodeImage(int nWidth, int nHeight)
    {
		if(mSettingConnQRCodeBitmap == null)
		{
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			if(btAdapter == null)
				return null; 
					
		    String strLocalMACAdres = btAdapter.getAddress();
		    if(strLocalMACAdres == null || strLocalMACAdres.isEmpty())
		    	return null; 
		    
		    strLocalMACAdres = strLocalMACAdres.replace(":", "");
		    strLocalMACAdres = "0X" + strLocalMACAdres;
		    final String strFullCmd =  "#@CipherLab10999310000710016110103988686471166254" + strLocalMACAdres;
		    mSettingConnQRCodeBitmap = mGenerateQRCodeBMP(strFullCmd, nWidth, nHeight);
		}
		
		return mSettingConnQRCodeBitmap;
    }
}
