package com.cipherlab.cipherconnect.sdk;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.cipherlab.help.ArrayHelper;

public class Cipher1860Helper {
	
	public static boolean IsPackageData(byte[] buffer)
	{
		if(buffer==null)
			return false;
		if(buffer.length<=2)
			return false;
		if(buffer[0]!= 0x5A)
			return false;
		
		int local = buffer[1];
		if(buffer.length<local)
			return false;

		if(buffer[local+3]== 0x7E)
			return true;
		
		return false;
	}
	
	public static StringBuffer RFID_PackageDataMoreTag(byte[] buffer) throws UnsupportedEncodingException
	{
		StringBuffer sb = new StringBuffer();
		if(buffer==null)
			return sb;

		if(buffer.length<=2)
			return sb;
		
		List<Byte> buffer_out = new ArrayList<Byte>();
		
		int len = buffer.length;
		int size=0; 
		int end = 0;
		for (int i = 0; i < len; i++) {
			if(buffer[i]==0x5A && i<len-4)
			{
				size= buffer[i+1];
				end = i+size+4;
				
				for (int j = i; j < end; j++) {
					buffer_out.add(buffer[j]);
				}
				
				byte[] temp = ArrayHelper.asByteArray(buffer_out);
				buffer_out.clear();
				sb.append(RFID_PackageData(temp));
				sb.append("\n");

				i=end-1;
			}
		}
		return sb;
	}
	
	public static StringBuffer RFID_PackageData(byte[] buffer) throws UnsupportedEncodingException
    {
        /* [Y][M][D][h][m][s][ms][RFID_Func][UL_MSB][UL_LSB]
           [PC_MSB][PC_LSB][EPC_MSB][...][EPC_LSB][CRC_MSB][CRC_LSB]
            UL: Length of PC+EPC+CRC */
    	
		int year = buffer[5] + 100;
        int month = buffer[6] - 1;
        int day = buffer[7];
        int hour = buffer[8];
        int min = buffer[9];
        int second = buffer[10];
        //int ms = buffer[11];
        //int RFID_Func = packet[12];
        
        /* Get RFID length */
        int TagDataLength = (buffer[13] << 8) + (buffer[14] & 0xFF);
        
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String date = s.format(new Date(year, month, day, hour, min, second)) + " ";
        
		StringBuffer sb = new StringBuffer(); 
		sb.append(date);
		byte[] array = new byte[TagDataLength];
		System.arraycopy(buffer, 15, array, 0, TagDataLength);
		
		String EPC = getHexString(array);
		sb.append(EPC);
		
		return sb;
    }	   

	private static final byte[] HEX_CHAR_TABLE = {
            (byte)'0', (byte)'1', (byte)'2', (byte)'3',
            (byte)'4', (byte)'5', (byte)'6', (byte)'7',
            (byte)'8', (byte)'9', (byte)'a', (byte)'b',
            (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    };    
	
	private static String getHexString(byte[] raw) throws UnsupportedEncodingException 
    {
         byte[] hex = new byte[2 * raw.length];
            int index = 0;

            for (byte b : raw) {
              int v = b & 0xFF;
              hex[index++] = HEX_CHAR_TABLE[v >>> 4];
              hex[index++] = HEX_CHAR_TABLE[v & 0xF];
            }
            return new String(hex, "ASCII");
    }
	
}
