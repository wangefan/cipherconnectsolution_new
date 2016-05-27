package com.cipherlab.cipherconnect.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
//import android.widget.Toast;

public class CipherConnectDemoDataProvider {
	
	private final String FILE_REMOTE_PATH = "/sdcard/";
	private final String FILE_NAME = "CipherConnectDemo.txt";
	
	private Context mContext = null;
	private String mData = null;
	
	private void message(String msg){
		AlertDialog deleteAlert = new AlertDialog.Builder(this.mContext).create();
		deleteAlert.setTitle(R.string.app_name);
		deleteAlert.setMessage(msg);
		deleteAlert.setButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		deleteAlert.show();
	}
	
	public CipherConnectDemoDataProvider(Context context){
		this.mContext=context;
		this.open();
	}
	
	private File getRemoteFile(){
		String path = FILE_REMOTE_PATH+FILE_NAME;
		File file = new File(path);
		
        return file;
	}
	
	private String getLocalData(){	
		StringBuffer sb = new StringBuffer(); 
		try {
			InputStream stream = mContext.getAssets().open(FILE_NAME);
			int c; 
			while ((c = stream.read()) != -1) { 
				sb.append((char) c); 
			} 
			stream.close(); 
			
			return sb.toString();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}
	
	public void open() {
		File file = this.getRemoteFile();
		if(file.exists()){
			try { 
				FileInputStream stream = new FileInputStream(file);
				StringBuffer sb = new StringBuffer(); 
				
				int c; 
				while ((c = stream.read()) != -1) { 
					sb.append((char) c); 
				} 
				stream.close(); 
				this.mData = sb.toString(); 
			} 
			catch (Exception e) { 
				this.message("Error in creating the data file on "+file.getPath());
				//Toast.makeText(this.mContext,"Error in creating the data file on "+file.getPath(), Toast.LENGTH_SHORT ).show();
				System.out.println("Error in open, e="+e.getMessage());
			} 
		}
		else{
			try {
				file.createNewFile();
			} catch (Exception e){
				this.message("Error in creating the data file on "+file.getPath());
				//Toast.makeText(this.mContext, "Error in creating the data file on "+file.getPath(), Toast.LENGTH_SHORT).show();
				System.out.println("Error in close, e="+e.getMessage());
				return;
			}
			
			this.mData = this.getLocalData();
		}
	}
	
	public void close() {
		File file = this.getRemoteFile();
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (Exception e){
				this.message("Error in creating the data file on "+file.getPath());
				//Toast.makeText(this.mContext, "Error in creating the data file on "+file.getPath(), Toast.LENGTH_SHORT).show();
				System.out.println("Error in close, e="+e.getMessage());
				return;
			}
		}
		
		try { 
			FileOutputStream stream = new FileOutputStream(file); 
			stream.write(this.mData.getBytes()); 
			stream.flush(); 
			stream.close(); 
		} 
		catch (Exception e) { 
			this.message("Error in creating the data file on "+file.getPath());
			//Toast.makeText(this.mContext, "Error in creating the data file on "+file.getPath(), Toast.LENGTH_SHORT).show();
			System.out.println("Error in close, e="+e.getMessage());
		}  
	}
	
	public CipherConnectDemoDataObject AddQuantity(String barcode) {
		CipherConnectDemoDataObject obj = this.getObject(barcode);
		if(obj==null)
			return null;
		
		int quantity = obj.getQuantity();
		obj.setQuantity(quantity+1);
		this.setObject(obj);
		
		return obj;
	}
	
	public CipherConnectDemoDataObject DeleteQuantity(String barcode) {
		CipherConnectDemoDataObject obj = this.getObject(barcode);
		if(obj==null)
			return null;
		
		int quantity = obj.getQuantity();
		obj.setQuantity(quantity-1);
		this.setObject(obj);
		
		return obj;
	}
	
	private CipherConnectDemoDataObject getObject(String barcode) {
		if(barcode==null)
			return null;
		if(barcode.length()==0)
			return null;
		
		String code			="";
		String name			="";
		String quantity		="";
		String picture_path	="";
		String company		="";
		String description	="";
		
		int index0 = 0;
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int index4 = 0;
		int index5 = 0;
		int index6 = 0;
		
		System.out.println("getObject("+barcode+").data:"+this.mData);
		try {
			index0 = this.mData.indexOf(barcode);
			if(index0<0)
				return null;
			
			index1 = this.mData.indexOf(",",index0+1);
			if(index1<0)
				return null;
			
			index2 = this.mData.indexOf(",",index1+1);
			if(index2<0)
				return null;
			
			index3 = this.mData.indexOf(",",index2+1);
			if(index3<0)
				return null;
			
			index4 = this.mData.indexOf(",",index3+1);
			if(index4<0)
				return null;
			
			index5 = this.mData.indexOf(",",index4+1);
			if(index5<0)
				return null;
			
			if(index5+1>this.mData.length())
				index6 = this.mData.length();
			else
				index6 = this.mData.indexOf("\r\n",index5+1);
			
			System.out.println("getObject("+barcode+").index0:"+index0);
			System.out.println("getObject("+barcode+").index1:"+index1);
			System.out.println("getObject("+barcode+").index2:"+index2);
			System.out.println("getObject("+barcode+").index3:"+index3);
			System.out.println("getObject("+barcode+").index4:"+index4);
			System.out.println("getObject("+barcode+").index5:"+index5);
			System.out.println("getObject("+barcode+").index6:"+index6);
			
		} catch (Exception e) {
			System.out.println("getObject("+barcode+").e:"+e);
			return null;
		}
		
		System.out.println("getObject.data:"+this.mData);
		
		try {
			code=this.mData.substring(index0,index1);
			System.out.println("getObject("+barcode+").code:"+code);
		}catch (Exception e) {}
		try {
			name=this.mData.substring(index1+1,index2);
			System.out.println("getObject("+barcode+").name:"+name);
		}catch (Exception e) {}
		try {
			quantity=this.mData.substring(index2+1,index3);
			System.out.println("getObject("+barcode+").quantity:"+quantity);
		}catch (Exception e) {}
		try {
			picture_path=this.mData.substring(index3+1,index4);
			System.out.println("getObject("+barcode+").picture_path:"+picture_path);
		}catch (Exception e) {}
		try {
			company=this.mData.substring(index4+1,index5);
			System.out.println("getObject("+barcode+").company:"+company);
		}catch (Exception e) {}
		try {
			description=this.mData.substring(index5+1,index6);
			System.out.println("getObject("+barcode+").description:"+description);
		}catch (Exception e) {}
		
		int iquantity;
		try {
			iquantity = Integer.parseInt(quantity);
		} catch (Exception e) {
			iquantity = 0;
		}
		CipherConnectDemoDataObject obj = new CipherConnectDemoDataObject(code,name,iquantity,picture_path,company,description);
		System.out.println("getObject("+barcode+").obj:"+obj);
		return obj;
	}
	
	private void setObject(CipherConnectDemoDataObject obj) {
		if(obj==null)
			return;
		
		String barcode		=obj.getBarcode();
		String name			=obj.getName();
		String quantity		=Integer.toString(obj.getQuantity());
		String picture_path	=obj.getPicturePath();
		String company		=obj.getCompany();
		String description	=obj.getDescription();
		
		int index0 = 0;
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int index4 = 0;
		int index5 = 0;
		int index6 = 0;
		
		System.out.println("getObject("+barcode+").data:"+this.mData);
		try {
			index0 = this.mData.indexOf(barcode);
			if(index0<0)
				return;
			
			index1 = this.mData.indexOf(",",index0+1);
			if(index1<0)
				return;
			
			index2 = this.mData.indexOf(",",index1+1);
			if(index2<0)
				return;
			
			index3 = this.mData.indexOf(",",index2+1);
			if(index3<0)
				return;
			
			index4 = this.mData.indexOf(",",index3+1);
			if(index4<0)
				return;
			
			index5 = this.mData.indexOf(",",index4+1);
			if(index5<0)
				return;
			
			if(index5+1>this.mData.length())
				index6 = this.mData.length();
			else
				index6 = this.mData.indexOf("\r\n",index5+1);
			
			System.out.println("getObject("+barcode+").index0:"+index0);
			System.out.println("getObject("+barcode+").index1:"+index1);
			System.out.println("getObject("+barcode+").index2:"+index2);
			System.out.println("getObject("+barcode+").index3:"+index3);
			System.out.println("getObject("+barcode+").index4:"+index4);
			System.out.println("getObject("+barcode+").index5:"+index5);
			System.out.println("getObject("+barcode+").index6:"+index6);
			
		} catch (Exception e) {
			System.out.println("setObject("+barcode+").e:"+e);
			return;
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.mData.substring(0,index0));
		buffer.append(barcode).append(",");
		buffer.append(name).append(",");
		buffer.append(quantity).append(",");
		buffer.append(picture_path).append(",");
		buffer.append(company).append(",");
		buffer.append(description).append("\r\n");
		
		int end=index6+2;
		if(end>this.mData.length())
			end = this.mData.length();
		
		buffer.append(this.mData.substring(end));//\r\n
		
		this.mData=buffer.toString();
		System.out.println("setObject.data="+this.mData);
	}
}