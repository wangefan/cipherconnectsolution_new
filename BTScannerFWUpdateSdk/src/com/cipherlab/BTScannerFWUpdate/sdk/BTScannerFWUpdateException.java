package com.cipherlab.BTScannerFWUpdate.sdk;

public class BTScannerFWUpdateException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7767570200409349139L;
	public static final int INFO_NOT_CIPHER_DEVICE = 0;
	public static final int INFO_NOT_VALID_SCKT = 1;
	public static final int INFO_DOWNLOADMODE_FAIL = 2;
	public static final int INFO_ERASEFLASH_FAIL = 3;
	public static final int INFO_FOUNDNOT_FILE = 4;
	private int mInfoID;
	
	public BTScannerFWUpdateException(int nInfoID)
	{
		super();
		mInfoID = nInfoID;
	}
	
	public String getMessage()
	{
		String strResult = "Unknown Message";
		switch (mInfoID) {
		case INFO_NOT_CIPHER_DEVICE:
			strResult = "Not Cipher Devices";
			break;
		case INFO_NOT_VALID_SCKT:
			strResult = "Not valid socket";
			break;
		case INFO_DOWNLOADMODE_FAIL:
			strResult = "set download mode fail";
			break;
		case INFO_ERASEFLASH_FAIL:
			strResult = "erase flash fail";
			break;
		case INFO_FOUNDNOT_FILE:
			strResult = "found not firmware file path";
			break;
		default:
			break;
		}
		return strResult; 
	}
}
