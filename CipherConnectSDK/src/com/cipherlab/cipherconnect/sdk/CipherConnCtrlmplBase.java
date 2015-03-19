package com.cipherlab.cipherconnect.sdk;

import java.util.ArrayList;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

abstract public class CipherConnCtrlmplBase {
	//Data members
	protected Context mContext = null;
	protected ArrayList<ICipherConnectControlListener> mListenerList = null;
	protected Bitmap mMACAddressBitmap = null;
	protected Bitmap mResetConnBitmap = null;
	protected Bitmap mSettingConnBitmap = null;
	
	public CipherConnCtrlmplBase(Context context) {
		mContext = context;
	}
	
	public void Reset() {
		SetCipherConnectControlListener(null);
		disconnect();
		setAuotReconnect(false, "");
	}
	
	public void SetCipherConnectControlListener(ArrayList<ICipherConnectControlListener> listenerList) throws NullPointerException {
		mListenerList = listenerList;
	}
	
	public boolean StartScanLEDevices() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public boolean StopScanLEDevices() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	//call back methods
	protected void fireCipherListenServerOnline() {
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				l.onListenServerOnline();
			}
		}
	}
	
	protected void fireCipherListenServerOffline() {
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				l.onListenServerOffline();
			}
		}
	}
	
	protected void fireCipherBeginConnectControl(String deviceName) {
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				l.onBeginConnecting(deviceName);
			}
		}
	}
	
	protected void fireReceivingBarcode(String deviceName,String barcode){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				//Log.e("CipherConnectControl","fireReceivingBarcode(deviceName="+deviceName+", barcode="+barcode+")");
				l.onReceivingBarcode(deviceName, barcode);
			}
		}
	}
	
	protected void fireConnecting(String deviceName){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				Log.e("CipherConnectControl","fireConnecting(deviceName="+deviceName+")");
				l.onConnecting(deviceName);
			}
		}
	}
	
	protected void fireConnected(String deviceName){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				Log.e("CipherConnectControl","fireConnected(deviceName="+deviceName+")");
				l.onConnected(deviceName);
			}
		}
	}
	
	protected void fireCipherConnectControlError(String deviceName, int id, String message){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				Log.e("CipherConnectControl","fireCipherConnectControlError(deviceName="+deviceName+", id="+id+", message="+message+")");
				l.onCipherConnectControlError(deviceName, id, message);
			}
		}
	}

	protected void fireDisconnected(String deviceName){
		if(mListenerList == null)
			return;
		for (ICipherConnectControlListener l : this.mListenerList) {
			if(l!=null)
			{
				Log.e("CipherConnectControl","fireDisconnected(deviceName="+deviceName+")");
				l.onDisconnected(deviceName);
			}
		}
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
	
	//abstract methods
	public abstract boolean isConnected();
		
	public abstract String[] getBluetoothDeviceNames();
		
	public abstract void connect(String deviceName)throws NullPointerException;	
	    
	public abstract void disconnect();
		
	public abstract void setAuotReconnect(boolean enable,String deviceName)throws NullPointerException;
		
	public abstract boolean isAutoReconnect();
	
	public abstract boolean StartListening();
	
	public abstract void StopListening();
	
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
}
