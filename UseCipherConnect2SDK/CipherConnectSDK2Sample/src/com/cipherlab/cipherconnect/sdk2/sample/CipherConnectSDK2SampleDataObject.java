package com.cipherlab.cipherconnect.sdk2.sample;

public class CipherConnectSDK2SampleDataObject {
	private String barcode;
	private String name;
	private int quantity;
	private String company;
	private String description;
	private String picture_path;
	
	public CipherConnectSDK2SampleDataObject(String barcode,String name,int quantity,String picture_path,String company,String description){
		this.barcode = barcode;
		this.name = name;
		this.quantity = quantity;
		this.picture_path = picture_path;
		this.company = company;
		this.description = description;
	}
	
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getName() {
		if(this.name==null)
			return "";
		
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getQuantity() {
		return this.quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public String getCompany() {
		if(company==null)
			return "";
		
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDescription() {
		if(description==null)
			return "";
		
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPicturePath() {
		if(picture_path==null)
			return "";
		
		return picture_path;
	}
	
	public void setPicturePath(String path) {
		this.picture_path = path;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(this.getBarcode()).append(",");
		buffer.append(this.getName()).append(",");
		buffer.append(this.getQuantity()).append(",");
		buffer.append(this.getName()).append(",");
		buffer.append(this.getDescription()).append(",");
		buffer.append(this.getPicturePath());
		
		return buffer.toString();
	}
}