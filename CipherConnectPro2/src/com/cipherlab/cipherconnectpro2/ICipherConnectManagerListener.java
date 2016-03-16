package com.cipherlab.cipherconnectpro2;
import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;

public interface ICipherConnectManagerListener {
    public void onBarcode(String barcode);
    public void onMinimizeKeyboard();
    public void onConnecting();
    public void onConnected();
    public void onConnectError(String message);
    public void onDisconnected();
    public void onGetLEDevice(final ICipherConnBTDevice device);
}
