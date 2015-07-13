package com.cipherlab.cipherconnect2.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import org.apache.http.util.EncodingUtils;
import android.bluetooth.BluetoothSocket;
import com.cipherlab.help.ArrayHelper;
import com.cipherlab.help.CipherLog;

public class CipherConnectVerify {
	private final String mTAG = "CipherConnectVerify";
	private BluetoothSocket mBluetoothSocket = null;
	private byte[] mTransmitBuffer = null;
	private String mCurltPageInfo = "";
	
	public CipherConnectVerify(BluetoothSocket socket){
		this.mBluetoothSocket = socket;
		mCurltPageInfo = "";
	}
	
	public byte[] getTransmitBuffer(){
		return mTransmitBuffer;
	}
	
	public boolean verify()throws IOException, TimeoutException{
		CipherLog.e(mTAG,"verify() begin");
		if(this.sendRequestCommand(this.mBluetoothSocket)==false)
		{
			CipherLog.e(mTAG,"sendRequestCommand fail, return false");
			return false;
		}
		CipherLog.e(mTAG,"sendRequestCommand ok");
		if(this.recvRequestCommandTimeout(this.mBluetoothSocket, 5)==false)
		{
			CipherLog.e(mTAG,"recvRequestCommand fail, return false");
			return false;
		}
		CipherLog.e(mTAG,"recvRequestCommand ok");
		
		byte[] sendData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] recvData = new byte[1024];
		byte[] checkCode = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		Random rnd = new Random();
		for(int i=0;i<16;i++){
			int d = rnd.nextInt(99);
			if(d>0)
				sendData[i]=(byte)d;
		}
		//rnd.nextBytes(sendData);
		//sendData = new byte[]{49,55,40,83,85,53,6,69,19,24,63,18,71,90,83,45};
		if(this.getCheckCode(sendData,checkCode)==false)
		{
			CipherLog.e(mTAG,"getCheckCode fail, return false");
			return false;
		}
		CipherLog.e(mTAG,"getCheckCode ok");
		
		if(this.sendCommand(this.mBluetoothSocket, sendData)==false)
		{
			CipherLog.e(mTAG,"sendCommand to verify fail, return false");
			return false;
		}
		CipherLog.e(mTAG,"sendCommand to verify ok");
		if(this.recvCommandTimeout(this.mBluetoothSocket, recvData, 5)==false)
		{
			CipherLog.e(mTAG,"recvVerifyCommand fail, return false");
			return false;
		}
		CipherLog.e(mTAG,"recvVerifyCommand ok");
		
		//Send command to get list page information.
		do
		{
			String strGetLtPgCmd = "#@109950\r";
			if(sendCommand(mBluetoothSocket, strGetLtPgCmd.getBytes()) == false)
			{
				CipherLog.e(mTAG,"sendCommand to get list page info fail, get no list page info");
				break;
			}
			
			CipherLog.e(mTAG,"sendCommand to get list page info OK");
			
			StringBuilder strRead = new StringBuilder();
			strRead.setLength(0);
			mReadWrapperRetString.setPar(mBluetoothSocket.getInputStream());
			do            
	    	{
	    		try {
	    			final int nTimeOut = 1;
	    			strRead.append(mReadWrapperRetString.read(nTimeOut));
				}
	    		catch (TimeoutException e) 
	    		{
	    			CipherLog.e(mTAG,"sendCommand to get list page info end");
	    			break;
	    		}
	    		catch (Exception e) 
				{
					if(e != null)
						e.printStackTrace();
					break;
				}
	    	} while (true);
			mCurltPageInfo = strRead.toString();
		}
		while(false);
		
		if(recvData==null || recvData.length<checkCode.length+_Header.length)
			return false;
		
		
		int index = ArrayHelper.IndexOf(recvData, ArrayHelper.sum(_Header,checkCode));
		
		recvData = null;
		checkCode = null;
		sendData = null;

		if(index>-1)
			return true;
		
		return false;
	}
	
	public boolean sendRequestCommand(BluetoothSocket socket){
		OutputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.sendRequestCommand:Can't get the outputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		byte[] buffer = {'#','@',',',',',',',',','\r'};
		try {
			stream.write(buffer);
			return true;
		} catch (IOException e) {
			CipherLog.e("CipherConnect","CipherConnectVerify.sendRequestCommand:Can't write to the Device",e);
			return false;
		}
	}
	
	private class ReadWrapperPar
	{
		private InputStream mInStream = null;
        public void setPar(InputStream in) {
        	mInStream = in;
        }

        public int read(byte[] ba, int nTimeOutSec) throws TimeoutException, IOException {
        	
        	final int nTimeOutMilliSec = nTimeOutSec * 1000;
        	final int nTimeStepMilliSec = 100;
        	int nCurTimeMilliSec = 0;
        	CipherLog.d(mTAG, "ReadWrapperPar read byte array begin, nTimeOutMilliSec = " + nTimeOutMilliSec);
        	while( nCurTimeMilliSec < nTimeOutMilliSec)
            {
                if( mInStream.available() > 0 )
                {
                	CipherLog.d(mTAG, "mInStream is available:");
                	int nResult = mInStream.read(ba);
                	if(CipherLog._DEBUG)
                	{
                		String strRead = new String(ba);
                		CipherLog.d(mTAG, strRead);	
                	}
                	return nResult;
                }
                else
                {
                	try {
    					Thread.sleep(nTimeStepMilliSec);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    					throw new TimeoutException();
    				}
                	nCurTimeMilliSec += nTimeStepMilliSec;
                	CipherLog.d(mTAG, "mInStream no data, sleep, nCurTimeMilliSec = " + nCurTimeMilliSec);
                }
            }
        	throw new TimeoutException();
        }
    }
	private class ReadWrapperRetString
	{
		private InputStream mInStream = null;
        
        public void setPar(InputStream in) {
        	mInStream = in;
        }

        public String read(int nTimeOutSec) throws TimeoutException, IOException {
        	final int nTimeOutMilliSec = nTimeOutSec * 1000;
        	final int nTimeStepMilliSec = 100;
        	int nCurTimeMilliSec = 0;
        	CipherLog.d(mTAG, "ReadWrapperRetString read begin, nTimeOutMilliSec = " + nTimeOutMilliSec);
        	while( nCurTimeMilliSec < nTimeOutMilliSec)
            {
        		int nAvailable = mInStream.available();
                if( nAvailable > 0 )
                {
                	CipherLog.d(mTAG, "mInStream is available, nAvailable = " + nAvailable);
                	byte ba[] = new byte[nAvailable];
                	mInStream.read(ba);
                	String strResult = new String(ba); 
                	if(CipherLog._DEBUG)
                	{
                		CipherLog.d(mTAG, strResult);	
                	}
                	return strResult;
                }
                else
                {
                	try {
    					Thread.sleep(nTimeStepMilliSec);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    					throw new TimeoutException();
    				}
                	nCurTimeMilliSec += nTimeStepMilliSec;
                	CipherLog.d(mTAG, "mInStream no data, sleep, nCurTimeMilliSec = " + nCurTimeMilliSec);
                }
            }
        	throw new TimeoutException();
        }
    }
	
	private ReadWrapperPar mReadArrayWrapper = new ReadWrapperPar();
	private ReadWrapperRetString mReadWrapperRetString = new ReadWrapperRetString();
    
	//timeout: sec
	public boolean recvRequestCommandTimeout(BluetoothSocket socket, int nTimeoutSec) throws TimeoutException
	{
		InputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getInputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.recvRequestCommand:Can't get the inputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		byte[] data = new byte[1024];
		try {
			if(mReadArrayWrapper != null)
			{
				mReadArrayWrapper.setPar(stream);
				mReadArrayWrapper.read(data, nTimeoutSec);
			}
			else {
				stream.read(data);
			}
		}
		catch (TimeoutException e) 
		{
			CipherLog.e(mTAG, "recvRequestCommandTimeout fail, Can't recv from the Device",e);
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			CipherLog.e(mTAG, "recvRequestCommandTimeout fail, Can't recv from the Device",e);
			e.printStackTrace();
			return false;
		}
		
		data = ArrayHelper.clear(data);
		int index = ArrayHelper.IndexOf(data, ArrayHelper.sum(_Header,_RecvHead));
		CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.index="+index);
		CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.data="+EncodingUtils.getAsciiString(data));
		if(index>-1){
			this.mTransmitBuffer = ArrayHelper.removeAll(data,ArrayHelper.sum(_Header,_RecvHead));
			if(mTransmitBuffer!=null)
				CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.mTransmitBuffer="+EncodingUtils.getAsciiString(mTransmitBuffer));
			return true;
		}
		
		return false;
	}
	
	public boolean recvRequestCommand(BluetoothSocket socket){
		InputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getInputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.recvRequestCommand:Can't get the inputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		byte[] data = new byte[10240];
		try {
			Thread.sleep(7000);
			stream.read(data);
			//Thread.sleep(2000);
		} catch (Exception e) {
			CipherLog.e("CipherConnect","CipherConnectVerify_recvRequestCommand:Can't recv from the Device",e);
			return false;
		}
		
		data = ArrayHelper.clear(data);
		int index = ArrayHelper.IndexOf(data, ArrayHelper.sum(_Header,_RecvHead));
		CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.index="+index);
		CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.data="+EncodingUtils.getAsciiString(data));
		if(index>-1){
			this.mTransmitBuffer = ArrayHelper.removeAll(data,ArrayHelper.sum(_Header,_RecvHead));
			if(mTransmitBuffer!=null)
				CipherLog.d("CipherConnect","CipherConnectVerify_recvRequestCommand.mTransmitBuffer="+EncodingUtils.getAsciiString(mTransmitBuffer));
			return true;
		}
		
		return false;
	}
	
	public boolean sendCommand(BluetoothSocket socket,byte[] sendData){
		OutputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.sendCommand:Can't get the outputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		try {
			stream.write(sendData);
		} catch (IOException e) {
			CipherLog.e("CipherConnect","CipherConnectVerify.sendCommand:Can't write to the Device",e);
			return false;
		}
		return true;
	}
	
	public boolean recvCommandTimeout(BluetoothSocket socket,byte[] recvData, int nTimeoutSec){
		InputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getInputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.recvVerifyCommand:Can't get the outputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		try {
			if(mReadArrayWrapper != null)
			{
				mReadArrayWrapper.setPar(stream);
				mReadArrayWrapper.read(recvData, nTimeoutSec);
			}
			else {
				stream.read(recvData);
			}
		} 
		catch (TimeoutException e) 
		{
			CipherLog.e(mTAG, "recvCommandTimeout fail, InterruptedException occurs, Can't recv from the Device",e);
			e.printStackTrace();
			return false;
		}
		catch (Exception e) {
			CipherLog.e(mTAG, "recvCommandTimeout fail, Exception occurs, Can't recv from the Device",e);
			return false;
		}

		return true;
	}
	
	public boolean recvVerifyCommand(BluetoothSocket socket,byte[] recvData){
		InputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getInputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.recvVerifyCommand:Can't get the outputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		try {
			Thread.sleep(1000);
			stream.read(recvData);
		} catch (Exception e) {
			CipherLog.e("CipherConnect","CipherConnectVerify.recvVerifyCommand:Can't write to the Device",e);
			return false;
		}

		return true;
	}
	
	private static final byte[] _InIndex = { 11, 5, 2, 14, 1, 9, 15, 11, 0, 3, 13, 12, 6, 10, 7, 4 };
    private static final byte[] _OutIndex = { 13, 4, 1, 9, 5, 0, 10, 8, 12, 15, 3, 14, 2, 7, 11, 6 };
    private static final byte[] _CIndex = { 0, 13, 43, 35, 78, 36, 59, 7, 34, 25, 17, 39, 17, 73, 94, 66 };
    private static final String _Encode_Table = "qZeC+bUmi.sf$k;*):Ru7KStzicH(]OASJUE!XNA:FJD^tcIOeRxDrDK@(h&b^38";
    
    private static byte[] _Header = { -52, 5, -34 };
    private static byte[] _RecvHead = {1,-1,1,-1};
	
	private boolean getCheckCode(byte[] codebuf,byte[] checkCode)
    {
		if (codebuf == null)
            return false;
        if (codebuf.length != 16)
            return false;
        if (checkCode == null)
            return false;
        if (checkCode.length != 16)
            return false;

        int buf;
        int hbuf;
        int c = 93;
        int a, ch;

        buf = codebuf[8];
        a = (int)codebuf[_InIndex[0]];
        for (int i = 0; i < 16; i++)
        {
            buf *= a;
            hbuf = buf;

            c += (buf & 0xff);
            hbuf >>= 8;
            c += (hbuf & 0xff);
            c += _CIndex[i];
            c *= 3;
            buf = (byte)(buf + hbuf & 0xff);
            buf &= 63;
            ch = (int)_Encode_Table.charAt((int)buf & 0xff);
            checkCode[_OutIndex[i]] = (byte)ch;
            if (i == 15)
                break;
            
            a = (ch + c)& 0xff;
            buf = codebuf[_InIndex[i + 1]];
        }
        return true;
    }
}