package com.cipherlab.cipherconnectpro;
import com.cipherlab.cipherconnect2.sdk.ICipherConnBTDevice;

public interface ICipherConnectManagerListener {
    public void onBarcode(String barcode);
    public void onMinimizeCmd();
    public void onConnecting();
    public void onConnected();
    public void onConnectError(String message);
    public void onDisconnected();
    public void onGetLEDevice(final ICipherConnBTDevice device);
}
