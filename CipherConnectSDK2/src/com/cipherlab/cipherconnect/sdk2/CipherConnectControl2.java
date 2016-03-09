package com.cipherlab.cipherconnect.sdk2;

import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;


public class CipherConnectControl2 implements ICipherConnectControl2 {
	
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
	
	public CipherConnectControl2(Context context) {
		mContext = context;
		mCtrlListenerList = new ArrayList<ICipherConnectControl2Listener>();
		mCipherConnCtrlImplClassic = new CipherConnCtrlmplClassic(mContext);
		mCipherConnCtrlImplBle = new CipherConnCtrlmplBLE(mContext);
		setBLEMode(false);
	}
	
	private void InitImplememtor(int nBTMode) {
		if(mCipherConnCtrlImpl != null)
			mCipherConnCtrlImpl.reset();
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
	
	public void setAutoReconnect(boolean enable)
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
	
	public void setBLEMode(boolean bEnable) throws UnsupportedOperationException {
		if(bEnable && false == IsBLEModeSupported())
			throw new UnsupportedOperationException();
		mNBTMode = bEnable ? NBLEBTMode : NClassicBTMode;
		InitImplememtor(mNBTMode);
	}
	
	public boolean startScanLEDevices() throws UnsupportedOperationException {
		return mCipherConnCtrlImpl.startScanLEDevices();
	}
	
	public boolean stopScanLEDevices() throws UnsupportedOperationException {
		return mCipherConnCtrlImpl.stopScanLEDevices();
	}
	
	@Override
	public void reset() {
		if(mCipherConnCtrlImpl != null)
			mCipherConnCtrlImpl.reset();
	}

	@Override
	public void close() {
		mNBTMode = NClassicBTMode;
		mContext = null;
		mCtrlListenerList = null;
		mCipherConnCtrlImplClassic.reset();
		mCipherConnCtrlImplBle.reset();
		mCipherConnCtrlImplClassic = null;
		mCipherConnCtrlImplBle = null;
	}

	@Override
	public String getFWVersion() {
		if(mCipherConnCtrlImpl != null)
			return mCipherConnCtrlImpl.getFWVersion();
    	return "";
	}
}
