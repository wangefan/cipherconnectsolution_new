package com.cipherlab.cipherconnect2.sdk;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;

public class CipherConnCtrl2EZMet implements ICipherConnCtrl2EZMet
{
	private static CipherConnCtrl2EZMet mMe = null;
	
	public static CipherConnCtrl2EZMet getInstance(Context context)
	{
		if (mMe == null)
			mMe = new CipherConnCtrl2EZMet(context);

		return mMe;
	}
	
	static public final int NClassicBTMode = 0;
	static public final int NBLEBTMode = 1;
	private int mNBTMode = NClassicBTMode;
	private Context mContext = null;
	protected ArrayList<ICipherConnectControl2Listener> mCtrlListenerList = null;
	
	//Data members
	private CipherConnCtrlmplBase    mCipherConnCtrlImpl = null;
	private CipherConnCtrlmplClassic mCipherConnCtrlImplClassic = null;
	private CipherConnCtrlmplBLE     mCipherConnCtrlImplBle = null;
	
	//Default Ctr
	
	private CipherConnCtrl2EZMet(Context context) {
		mContext = context;
		mCtrlListenerList = new ArrayList<ICipherConnectControl2Listener>();
		mCipherConnCtrlImplClassic = new CipherConnCtrlmplClassic(mContext);
		mCipherConnCtrlImplBle = new CipherConnCtrlmplBLE(mContext);
		SetBLEMode(false);
	}
	
	private void InitImplememtor(int nBTMode) {
		if(mCipherConnCtrlImpl != null)
			mCipherConnCtrlImpl.Reset();
		mNBTMode = nBTMode;
		switch (mNBTMode) {
		case NClassicBTMode:
			mCipherConnCtrlImpl = mCipherConnCtrlImplClassic;
			break;
		case NBLEBTMode:
			mCipherConnCtrlImpl = mCipherConnCtrlImplBle;
			break;
		default:
			throw new RuntimeException();
		}
		mCipherConnCtrlImpl.SetCipherConnectControlListener(mCtrlListenerList);
	}
	
	@Override
	public String getVersion()
	{
		return CipherConnectControlResource.lib_version;
	}
	
	
	public boolean isConnected()
	{
		return mCipherConnCtrlImpl.isConnected();
	}
	
	public ICipherConnBTDevice[] getBtDevices()
	{
		return mCipherConnCtrlImpl.getBtDevices();
	}
	
    public void connect(ICipherConnBTDevice device)throws NullPointerException
    {
		mCipherConnCtrlImpl.connect(device);
	}
    
    public void connect(String deviceName, String deviceAddr)throws NullPointerException
    {
		mCipherConnCtrlImpl.connect(deviceName, deviceAddr);
	}
    
	public void disconnect()
	{
		mCipherConnCtrlImpl.disconnect();
	}

	@Override
	public void addCipherConnect2Listener(ICipherConnectControl2Listener listener) throws NullPointerException 
	{
		if(listener == null)
			throw new NullPointerException();
				
		mCtrlListenerList.add(listener);
		
	}
	
	public void setAutoReconnect(boolean enable)throws NullPointerException
	{
		mCipherConnCtrlImpl.setAutoReconnect(enable);
	}
	
	public boolean isAutoReconnect()
	{
		return mCipherConnCtrlImpl.isAutoReconnect();
	}
	
	public boolean IsBLEModeSupported() {
		if(mContext != null && (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) )
			return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
		return false;
	}
	
	public void SetBLEMode(boolean bEnable) throws UnsupportedOperationException {
		if(bEnable && false == IsBLEModeSupported())
			throw new UnsupportedOperationException();
		mNBTMode = bEnable ? NBLEBTMode : NClassicBTMode;
		InitImplememtor(mNBTMode);
	}
	
	public boolean StartScanLEDevices() throws UnsupportedOperationException {
		return mCipherConnCtrlImpl.StartScanLEDevices();
	}
	
	public boolean StopScanLEDevices() throws UnsupportedOperationException {
		return mCipherConnCtrlImpl.StopScanLEDevices();
	}
	
	public boolean StartListening() 
	{
		if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.StartListening();
		return false;
	}
	
    public void StopListening()
    {
    	if(mCipherConnCtrlImpl != null)
			mCipherConnCtrlImpl.StopListening();
    }
    
    public Bitmap GetMacAddrBarcodeImage(int nWidth, int nHeight)
    {
    	if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.GetMacAddrBarcodeImage(nWidth, nHeight);
    	return null;
    }
    
    public Bitmap GetResetConnBarcodeImage(int nWidth, int nHeight)
    {
    	if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.GetResetConnBarcodeImage(nWidth, nHeight);
    	return null;
    }
    
    public Bitmap GetSettingConnBarcodeImage(int nWidth, int nHeight)
    {
    	if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.GetSettingConnBarcodeImage(nWidth, nHeight);
    	return null;
    }
    
    public Bitmap GetSettingConnQRCodeImage(int nWidth, int nHeight)
    {
    	if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.GetSettingConnQRCodeImage(nWidth, nHeight);
    	return null;
    }
}