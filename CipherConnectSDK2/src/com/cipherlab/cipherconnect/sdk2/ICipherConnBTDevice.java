package com.cipherlab.cipherconnect.sdk2;

import java.io.Serializable;

public interface ICipherConnBTDevice extends Serializable
{
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
