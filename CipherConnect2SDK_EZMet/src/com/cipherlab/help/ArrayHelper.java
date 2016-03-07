package com.cipherlab.help;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ArrayHelper {
	
	public static int IndexOf(byte[] source, byte[] key)
    {
        if (source == null || source.length == 0)
            return -1;
        if (key == null || key.length == 0)
            return -1;

        for (int i = 0; i < (source.length - key.length) + 1; i++)
        {
        	if(source[i]==0)
        		continue;
        	
            int conformSize = 0;
            for (int j = 0; j < key.length; j++)
            {
                if (source[i + j] == key[j])
                    conformSize++;
                else
                	break;
            }
            if (conformSize == key.length)
                return i;
        }
        return -1;
    }
	
	public static List<Byte> asByteList(byte[] source){
		if(source==null)
			return null;
		
		List<Byte> buffer = new ArrayList<Byte>();
		for (byte b : source) {
			buffer.add(b);
		}
		return buffer;
	}
	
	public static byte[] asByteArray(List<Byte> source){
		if(source==null)
			return null;
		
		byte[] buffer = new byte[source.size()];
		for(int i=0;i<source.size();i++)
			buffer[i]=source.get(i);
		
		source=null;
		return buffer;
	}
	
	public static byte[] removeAll(byte[] source, byte[] key)
    {
        if (source == null || source.length == 0)
            return null;
        if (key == null || key.length == 0)
            return source;
        if(source.length<=key.length)
        	return null;
        
        ByteBuffer buffer = ByteBuffer.wrap(source); 
        ByteBuffer keyBuffer = ByteBuffer.wrap(key); 
        
        int remainingBytes = buffer.compareTo(keyBuffer);
        byte[] dst = new byte[remainingBytes]; 
        buffer.get(dst, key.length, remainingBytes); 
        return dst;
    }
	
	/**
	 * @param value1
	 * @param value2
	 * @return value1+value2
	 */
	public static byte[] sum(byte[] value1, byte[] value2){
		int len1 = 0;
		int len2 = 0;
		if(value1!=null)
			len1 = value1.length;
		if(value2!=null)
			len2 = value2.length;
		
		byte[] buffer = new byte[len1+len2];
		int index = 0;
		for(int i=0;i<len1;i++)
			buffer[index++]=value1[i];
		for(int i=0;i<len2;i++)
			buffer[index++]=value2[i];
		
		return buffer;
	}
	
	public static byte[] ArrayList2Byte(ArrayList<Byte> buffer){
		if(buffer==null)
			return null;
		
		if(buffer.isEmpty())
			return new byte[0];
		
		byte[] newbuffer = new byte[buffer.size()];
		int i = 0;
		for (Byte byte1 : buffer) {
			newbuffer[i++]=byte1;
		}
		
		return newbuffer;
	}
	
	/**
	 * clear 0.
	 * @param source
	 * @return
	 */
	public static byte[] clear(byte[] source){
		if(source==null)
			return null;
		
		int len = source.length;
		List<Byte> buffer = new ArrayList<Byte>();
		
		int count=0;
		for(int i=len-1;i>=0;i--){
			byte b = source[i];
			if(count==0)
			{
				if(b!=0)
				{
					count++;
					buffer.add(b);
				}
			}
			else
			{
				buffer.add(b);
			}
		}
		
		byte[] data = new byte[buffer.size()];
		for (int i = 0, j=data.length-1; i < data.length; i++,j--) {
			data[i] = buffer.get(j);
		}
		
		return data;
	}
	
	public static byte[] append(byte[] a, byte[] b){
		if(a==null || a.length<=0)
			return b;
		if(b==null || b.length<=0)
			return a;
		
		b=clear(b);
		int len_a = a.length;
		int len_b = b.length;
		List<Byte> buffer = new ArrayList<Byte>();
		for(int i=0;i<len_a;i++)
		{
			byte d = a[i];
			buffer.add(d);
		}
		for(int i=0;i<len_b;i++)
		{
			byte d = b[i];
			buffer.add(d);
		}
		
		byte[] data = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			data[i] = buffer.get(i);
		}
		
		return data;
	}
}