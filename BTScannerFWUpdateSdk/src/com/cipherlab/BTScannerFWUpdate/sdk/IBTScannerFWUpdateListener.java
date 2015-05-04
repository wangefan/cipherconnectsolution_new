package com.cipherlab.BTScannerFWUpdate.sdk;

/**
 * Listener to listening update firmware progress.
 * @author yifan.wang
 */
public interface IBTScannerFWUpdateListener {
	/**
	 * The function to report progress.
	 * 
	 * @param message: Report update firmware status.
	 * @param nProgress : Report progress , 0~100
	 */
	public void onProgress(String message, int nProgress);
}
