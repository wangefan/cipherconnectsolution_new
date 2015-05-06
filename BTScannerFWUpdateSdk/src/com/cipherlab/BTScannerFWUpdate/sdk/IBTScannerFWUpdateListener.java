package com.cipherlab.BTScannerFWUpdate.sdk;

/**
 * Listener to report updating firmware progress and status.
 * @author yifan.wang
 */
public interface IBTScannerFWUpdateListener {
	/**
	 * The function to report updating progress.
	 * 
	 * @param message: Report update firmware status.
	 * @param nProgress : Report progress , 0~100
	 */
	public void onProgress(String message, int nProgress);
	
	/**
	 * The function is to report updating firmware error.
	 */
	public void onUpdateFWErr(BTScannerFWUpdateException e);
	
	/**
	 * The function to report update firmware complete.
	 */
	public void onUpdateFWComplete();
}
