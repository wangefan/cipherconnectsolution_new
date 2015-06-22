package com.cipherlab.cipherconnectpro2;


import com.cipherlab.cipherconnect2.sdk.ICipherConnBTDevice;

import android.graphics.Bitmap;


public interface ICipherConnectManagerService {
	public static enum CONN_STATE {
		CONN_STATE_BEGINCONNECTING,
		CONN_STATE_CONNECTING,
		CONN_STATE_CONNECTED,
		CONN_STATE_CONNECTERR,
		CONN_STATE_DISCONNECT
	}
	public void setUpForBluetooth();
	/*Server service begin*/
	public boolean StartListenConn();
	public void StopListenConn();
	public CONN_STATE GetConnState();
	public Bitmap GetMacAddrBarcodeImage(int nWidth, int nHeight);
	public Bitmap GetResetConnBarcodeImage(int nWidth, int nHeight);
	public Bitmap GetSettingConnBarcodeImage(int nWidth, int nHeight);
	public Bitmap GetSettingConnQRcodeImage(int nWidth, int nHeight);
	/*Server service end*/
	public boolean isConnected();
    public ICipherConnBTDevice[] getBtDevices();
    public ICipherConnBTDevice GetConnDevice();
    public boolean connect(ICipherConnBTDevice device)throws Exception;
    public boolean connect(String deviceName, String deviceAddr)throws Exception;
    public void disConnect();
    public void AddListener(ICipherConnectManagerListener l);
    public void RemoveListener(ICipherConnectManagerListener l);
    public void setAutoConnect(boolean enable);
    public boolean isAuotConnect();
    public void stopSelf();
    public boolean IsBLEModeSupported();
    public void SetBLEMode(boolean bEnable) throws UnsupportedOperationException;
    public boolean StartScanLEDevices() throws UnsupportedOperationException;
    public boolean StopScanLEDevices() throws UnsupportedOperationException;
}
