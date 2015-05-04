package com.cipherlab.BTScannerFWUpdate.sdk;

/**
 * The interface is utility for updating scanner 166x by blueTooth.
 *
 */
public interface IBTScannerFWUpdate 
{
	/**
	 * The function to update firmware.
	 * 
	 * @param String: firmware full file path.
	 * @return boolean: true or false .
	 * @Throw BTScannerFWUpdateException: Exception contains message to notify client.
	 */
	public boolean updateFW(String filePath) throws BTScannerFWUpdateException;
	
	/**
	 * The function to get firmware version.
	 * 
	 * @return String: version info.
	 * @Throw BTScannerFWUpdateException: Exception contains message to notify client.
	 */
	public String getVersion()throws BTScannerFWUpdateException;
	
	/**
	 * The function to set listener.
	 * 
	 * @param IBTScannerFWUpdateListener: listener to be set.
	 * @Throw NullPointerException: null Exception if the listener is null.
	 */
	public void setListener(IBTScannerFWUpdateListener listener)throws NullPointerException;
}
