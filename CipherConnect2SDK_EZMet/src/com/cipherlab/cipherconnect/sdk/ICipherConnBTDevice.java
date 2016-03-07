package com.cipherlab.cipherconnect.sdk;

public interface ICipherConnBTDevice {
	/**
	 * Get the name of Bluetooth device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * String name = device.getDeviceName();
	 * </DD>
	 * @return name
	 */
	public String getDeviceName();
	
	/**
	 * Get the MAC address of Bluetooth device.
	 * <DT><B>code:</B><DT>
	 * <DD>
	 * String address = device.getAddress();
	 * </DD>
	 * @return address
	 */
	public String getAddress();
}
