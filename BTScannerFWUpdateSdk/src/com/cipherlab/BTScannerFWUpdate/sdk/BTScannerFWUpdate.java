package com.cipherlab.BTScannerFWUpdate.sdk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.bluetooth.BluetoothSocket;

public class BTScannerFWUpdate implements IBTScannerFWUpdate
{
	static private final String TAGForFW = "BTScannerFWUpdate";
	static private IBTScannerFWUpdate mMe = null;
	
	//Data members.
	private BluetoothSocket mbtSocket = null;
	private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private IBTScannerFWUpdateListener mListener = null;
	
	static IBTScannerFWUpdate getInst(BluetoothSocket btSocket) throws BTScannerFWUpdateException
	{
		if(mMe == null)
			mMe = new BTScannerFWUpdate(btSocket);
		return mMe;
	}
	
	private BTScannerFWUpdate(BluetoothSocket btSocket) throws BTScannerFWUpdateException, NullPointerException
	{
		if(mbtSocket == null)
			throw new NullPointerException();
		
		mbtSocket = btSocket;
		try {
			mInStream = mbtSocket.getInputStream();
			mOutStream = mbtSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_NOT_VALID_SCKT);
		}
	}
	
	private void mFireProgress(final String strMsg, int nProgress)
	{
		if(mListener != null)
			mListener.onProgress(strMsg, nProgress);
	}
	
	//-------------------------------------------------------------------------//
	private final static String BUSY  = "FB\r";
	private final static String ACK   = "FA\r";
	private final static String NAK   = "FN\r";
	private final static String FAULT = "FF\r";
	private final static String ACK1  = "ACK1\r";
	private final static String ACK2  = "ACK2\r";
	//-------------------------------------------------------------------------//
	private boolean mSetDownLoadMode()
	{
		StringBuilder strbdRet = new StringBuilder();		
		
		if (mSend ("#@109997\r") == true)	//Let scanner in download mode.
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			CipherLog.d(TAGForFW, "Let scanner in download OK");
			
			final int nTryCount = 3;
			for (Integer idxTry = 0; idxTry < nTryCount; ++idxTry)
			{	
				CipherLog.d(TAGForFW, "mSetDownLoadMode, try " + ((Integer)(idxTry+1)).toString() + " times");
				if (mSend_Read ("\r", strbdRet) != true)       
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x mSend_Read(0x0d) fail, will break the loop");
					break;
				}
				
				if (strbdRet.toString().equals(NAK))   
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read NAK, return true");
					return true;
				}
				    
				if (strbdRet.toString().equals(FAULT)){
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read FAULT, return true");
					return true;
				}
				    
				if (mSend_Read ("SYNTECH\r",  strbdRet ) != true)     
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, send 166x SYNTECH fail! break"); 
					break;
				}

				if (strbdRet.toString().equals(NAK))   
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read NAK, return true");
					return true;
				}
				
				if (false == strbdRet.toString().equals(ACK1))
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read ACK1, continue");
					continue;
				}
							
				if (mSend_Read ("DOWNLOAD\r", strbdRet) != true)
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x send DOWNLOAD fail, break");
					break;
				}

				if (strbdRet.toString().equals(NAK))   
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read NAK, return true");
					return true;
				}
				
				if (strbdRet.toString().equals(ACK2)) 
				{
					CipherLog.d(TAGForFW, "mSetDownLoadMode, 166x read ACK2, return true");
					return true;
				}
			}
		}
		return false;
	}
		
	private boolean mEraseFlash()
	{
		CipherLog.d(TAGForFW, "mEraseFlash begin");
		StringBuilder strbdRet = new StringBuilder();
			
		final int nTryCount = 8;
		for (Integer idxTry = 0; idxTry < nTryCount; ++idxTry)
		{
			CipherLog.d(TAGForFW, "mEraseFlash, try " + ((Integer)(idxTry+1)).toString() + " times");
			if (mSend_Read(":ERASE\r", strbdRet) != true)
			{
				CipherLog.d(TAGForFW, "mEraseFlash, send 166x :ERASE\r fail! break"); 
				break;
			}

			if (strbdRet.toString().equals(ACK))
			{
				CipherLog.d(TAGForFW, "mEraseFlash, 166x read ACK, return true");
				return true;
			}
			    
			if (strbdRet.toString().equals(BUSY))
			{
				CipherLog.d(TAGForFW, "mEraseFlash, busy");
				strbdRet.setLength(0);
				if (mRead(8, strbdRet) == true && strbdRet.toString().equals(ACK))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private enum UpFWState {
		STATE_START,
		STATE_LENGTH,
		STATE_ADDRESS,        
		STATE_DATA,             
		STATE_CHECKSUM,        
		STATE_RETURN,          
		STATE_LENGTH2,   
	};
	
	private boolean mDownLoadDataShx (int nPt, byte szLine[])
	{
		CipherLog.d(TAGForFW, "mDownLoadDataShx begin");
		int nSum=0;

		szLine [2] = (byte) (nPt - 2);                      // data length.
		for (int idx = 2; idx < nPt; idx++)
			nSum += szLine [idx];
			
		nSum %= 256;
		szLine [nPt++] = (byte) (0xFF - nSum);              // checksum.
		szLine [nPt++] = '\r';

		final int nTryCount = 10;
		for(int idxTry = 0; idxTry < nTryCount; ++idxTry)                              // wait for ACK.
		{
			CipherLog.d(TAGForFW, "mDownLoadDataShx try " + ((Integer)(idxTry+1)).toString() + " times");
			StringBuilder sBuilderRet = new StringBuilder();
			if (mSend_Read (szLine, nPt, sBuilderRet) != true)   // send data.
				break;

			for(int idxjTry = 0; idxjTry < nTryCount; ++idxjTry)                             // wait for ACK.
			{
				if (sBuilderRet.toString().equals(ACK))
				{
					CipherLog.d(TAGForFW, "mDownLoadDataShx OK");
					return true;
				}
				else if (sBuilderRet.toString().equals(NAK))
					break;
				else if (sBuilderRet.toString().equals(FAULT))
					return false;
				else
				{
					if (mRead(2, sBuilderRet) == false)
						return false;
				}
			}
		}

		return false;
	}
	
	int Numeral (char c)
	{
		return (int) ((c >= 'A') ? (c - 'A' + 10) : (c -= '0'));
	}
	
	private boolean mWriteFlash(BufferedInputStream bufInStream, int nFileSize)
	{
		int    nAccum=0;
		int    nPt=0;                      // counter for line buffer.
		int    nData = 0;                    // data length.
		int    nAddr = 4;                    // address length.
		
		byte   szData[] = new byte[258];	// buffer for reading data from file.
		byte   byAddr[] = new byte[4];
		char   c1, c2;
		long   nAddr1=0, nAddr2=0;
		
		UpFWState nState = UpFWState.STATE_START;         // format state.
		byte szLine[] = new byte[258];
		
		while (true)
		{
			int nRead = 0;
			try {
				nRead = bufInStream.read(szData, 0, 256);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(nRead <= 0 || nAccum >= nFileSize)
				break;
			nAccum += nRead;

			int nOffsetBuffer = 0;
			
			while (nOffsetBuffer < 256)
			{
				c1 = (char)szData [nOffsetBuffer++];
				c2 = (char)szData [nOffsetBuffer++];

				switch (nState)
				{
				case STATE_START:
				{
					if ((c2 == '7') || (c2 == '9'))  // end of data.
					{
						if ((nPt > 0) && mDownLoadDataShx(nPt, szLine) == false)
							return false;

						szLine [0] = 'S';
						szLine [1] = '7';
						szLine [2] = 5;
						szLine [3] = szLine [4] = szLine [5] = szLine [6] = '\0';
						szLine [7] = (byte) 0xfa;
						szLine [8] = '\r';

						if (mSend_Read (szLine, 9, new StringBuilder()) != true)
							break;
						
						mFireProgress("write flash progress", 100);
						return true;
					}

					if (nPt == 0)
					{
						szLine [0] = 'S';
						szLine [1] = '3';
						nPt = 3;                     // skip the data length.
					}

					nState = (c2 == '2') ? UpFWState.STATE_LENGTH2 : UpFWState.STATE_LENGTH;	
				}
					break;
				case STATE_LENGTH:
				{
					int n1 = Numeral (c1);
					int n2 = Numeral (c2);
					nData  = n1 * 16 + n2 - 5;       // deduct the addr & chksum
					nState = UpFWState.STATE_ADDRESS;
					nAddr  = 4;
				}
					break;
				case STATE_LENGTH2:
				{
					int n1 = Numeral (c1);
					int n2 = Numeral (c2);
					nData  = n1 * 16 + n2 - 4;       // deduct the addr & chksum
					nState = UpFWState.STATE_ADDRESS;
					byAddr [3] = '\0';
					nAddr  = 3;
					if (nPt < 7)                     // insert an 0 to address.
						szLine [nPt++] = '\0';
				}
					break;

				case STATE_ADDRESS:
				{
					int n1 = Numeral (c1);
					int n2 = Numeral (c2);
					int n = n1 * 16 + n2;
					byAddr [--nAddr] = (byte) n;
					nAddr2 = (nAddr2 << 8) + n;   //20100110 CY  << 4 -> << 8

					if (nPt < 7)                     // should keep the address.
						szLine [nPt++] = (byte)n;

					if (nAddr <= 0)
					{
						nState = UpFWState.STATE_DATA;
						if ((nPt > 7) && (nAddr1 != nAddr2) && (nAddr1 > 0))
						{
							if (mDownLoadDataShx (nPt, szLine) == false)
								return false;

							szLine [0] = 'S';
							szLine [1] = '3';
							szLine [3] = byAddr [3];
							szLine [4] = byAddr [2];
							szLine [5] = byAddr [1];
							szLine [6] = byAddr [0];
							nPt = 7;
						}
						nAddr1 = nAddr2 + nData;		// next address
						nAddr2 = 0;
					}
				}
					break;
				case STATE_DATA:
				{
					int n1 = Numeral (c1);
					int n2 = Numeral (c2);
					szLine [nPt++] = (byte) (n1 * 16 + n2);
					if (--nData <= 0)
						nState = UpFWState.STATE_CHECKSUM;
				}
					break;
				case STATE_CHECKSUM:
					nState = UpFWState.STATE_RETURN;
					break;

				case STATE_RETURN:
					nState = UpFWState.STATE_START;
					//if (nPt > 100)
	                if (nPt >= (64 + 7))
					{
						if (mDownLoadDataShx (nPt, szLine) == false)
							return false;
						nPt = 0;                     // clear counter.
					}
					break;
				}
			}
			
			int nProgress = Math.round((float)(nAccum*80)/(float)(nFileSize)+20);
			CipherLog.d(TAGForFW, "Download progress = " + ((Integer)(nProgress)).toString());
			mFireProgress("write flash progress", nProgress);
		}
		
		return false;
	}
	
	//Private member functions
	boolean mSend(String strSend)
    {
		if(mOutStream != null)
		{
			try {
				mOutStream.write(strSend.getBytes());
			} catch (IOException e) {
				if(e != null)
					e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
    }
	
	boolean mRead (long nTimeoutSec, StringBuilder strRead)
    {
    	byte byteRet = '\r';
     	long nTimeOut = System.currentTimeMillis() + nTimeoutSec * 1000; 

     	do            // keep on reading while not receive CR.
    	{
    		try {
				byteRet = (byte) mInStream.read();
			} 
    		catch (Exception e) 
			{
				if(e != null)
					e.printStackTrace();
				return false;
			}
     		
     		strRead.append((char)byteRet);

     		if (System.currentTimeMillis() > nTimeOut)   // check if time out.
     		{
     			CipherLog.d(TAGForFW, "Read data = (" + strRead.toString() + "), timeout! return false");
     			return false;
    		}
    	} while (byteRet != '\r');
     	
    	CipherLog.d(TAGForFW, "Read data = (" + strRead.toString() + "), return true");
    	return true;
    }
	
	boolean mSend_Read (byte baSend[], int nLen, StringBuilder strRead)
    {
		if(mOutStream != null)
		{
			try {
	    		mOutStream.write(baSend, 0, nLen);
			} catch (IOException e) {
				if(e != null)
					e.printStackTrace();
				return false;
			} 

	    	strRead.setLength(0);
	    	return mRead (8, strRead);
		}
		return false;
    }
	
	boolean mSend_Read (String strSend, StringBuilder strRead)
    {
    	byte[] ba = null;
    	try {
    		ba = strSend.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
    	try {
			mOutStream.write(ba);
			
		} catch (IOException e) {
			if(e != null)
				e.printStackTrace();
			return false;
		} 

    	strRead.setLength(0);
    	return mRead (8, strRead);
    }

		
	@Override
	public boolean updateFW(String filePath) throws BTScannerFWUpdateException 
	{
		boolean bRet = false;
		BufferedInputStream bufInStream = null;
		try {
			if(mOutStream == null || mInStream == null || filePath.length() <= 0)
				return false;
			
			mFireProgress("Set scanner to download mode", 0);
			if(false == mSetDownLoadMode())
			{
				CipherLog.d(TAGForFW, "mSetDownLoadMode fail!");
				throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_DOWNLOADMODE_FAIL);
			}
			
			mFireProgress("Set scanner to download mode ok", 10);
			CipherLog.d(TAGForFW, "mSetDownLoadMode OK!");
			
			Thread.sleep(200);
					
			mFireProgress("Erase flash", 10);
			if(false == mEraseFlash())
			{
				CipherLog.d(TAGForFW, "mEraseFlash fail!");
				throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_ERASEFLASH_FAIL);
			}
			
			mFireProgress("Erase flash ok", 20);
			CipherLog.d(TAGForFW, "mEraseFlash OK!");
		
			Thread.sleep(200);
				
			File fwfile = new File(filePath);
			if(fwfile.exists() == false)
			{
				CipherLog.d(TAGForFW, "can`t find firmware " + filePath);
				throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_FOUNDNOT_FILE);
			}
					
			int nFileSize = (int) fwfile.length();
			try {
				bufInStream = new BufferedInputStream(new FileInputStream(fwfile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_FOUNDNOT_FILE);
			}
			
			mFireProgress("write flash begin", 20);
			if(false == mWriteFlash(bufInStream, nFileSize))
			{
				CipherLog.d(TAGForFW, "mWriteFlash fail!");
				throw new BTScannerFWUpdateException(BTScannerFWUpdateException.INFO_ERASEFLASH_FAIL);
			}
			else{
				CipherLog.d(TAGForFW, "mWriteFlash done!");
			}
		}
		catch (BTScannerFWUpdateException e) {
			throw e;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		finally {
			if(bufInStream != null)
			{
				try {
					bufInStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				bufInStream = null;
			}
		}
		
		return bRet;
	}

	@Override
	public String getVersion() throws BTScannerFWUpdateException {
		String strResult = "";
		try
		{
			StringBuilder strbdRet = new StringBuilder();
			if (mSend("#@109995\r") == false)
			{
				return strResult;
			}
			
			Thread.sleep(500);
			
			if (mSend_Read ("VERSION\r", strbdRet) == false)
			{
				return strResult;
			}
			
			strResult = strbdRet.toString();
			CipherLog.d(TAGForFW, "scaner version = " + strbdRet.toString());
			
			if (mSend_Read ("EXIT\r", strbdRet) == false)
			{
				return strResult;
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return strResult;
	}

	@Override
	public void setListener(IBTScannerFWUpdateListener listener)
			throws NullPointerException {
		if(listener == null)
			throw new NullPointerException();
		mListener = listener;
	}
}
