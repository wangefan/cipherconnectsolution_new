package com.cipherlab.cipherconnect.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.util.EncodingUtils;

import android.bluetooth.BluetoothSocket;

import com.cipherlab.help.ArrayHelper;
import com.cipherlab.help.CipherLog;

public class CipherConnectVerify {
	
	private BluetoothSocket mBluetoothSocket = null;
	private byte[] mTransmitBuffer = null;
	
	public CipherConnectVerify(BluetoothSocket socket){
		this.mBluetoothSocket = socket;
	}
	
	public byte[] getTransmitBuffer(){
		return mTransmitBuffer;
	}
	
	public boolean verify()throws IOException{

		if(this.sendRequestCommand(this.mBluetoothSocket)==false)
			return false;
		if(this.recvRequestCommand(this.mBluetoothSocket)==false)
			return false;
		
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
			return false;
		if(this.sendVerifyCommand(this.mBluetoothSocket, sendData)==false)
			return false;
		if(this.recvVerifyCommand(this.mBluetoothSocket, recvData)==false)
			return false;
		
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
	
	public boolean sendVerifyCommand(BluetoothSocket socket,byte[] sendData){
		OutputStream stream = null;
		try {
			stream = this.mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			CipherLog.e("CipherConnect", "CipherConnectVerify.sendVerifyCommand:Can't get the outputStream of BluetoothSocket", e);
			return false;
		}
		if(stream==null)
			return false;
		
		try {
			stream.write(sendData);
		} catch (IOException e) {
			CipherLog.e("CipherConnect","CipherConnectVerify.sendVerifyCommand:Can't write to the Device",e);
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