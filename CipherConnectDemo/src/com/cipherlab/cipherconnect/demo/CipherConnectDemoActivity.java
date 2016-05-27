package com.cipherlab.cipherconnect.demo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class CipherConnectDemoActivity extends Activity {
    private CipherConnectDemoDataProvider provider;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.init_ui();
        this.provider = new CipherConnectDemoDataProvider(this);
    }
    
    private void init_ui(){
    	EditText txtInputBarcode = (EditText)this.findViewById(R.id.txtInputBarcode);
    	txtInputBarcode.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return txtInputBarcode_onKey(v,keyCode,event);
			}
		});
    	ImageView imagePicture = (ImageView)this.findViewById(R.id.imagePicture);
    	imagePicture.setImageResource(R.drawable.null_image);
    }
    
    private boolean txtInputBarcode_onKey(View v, int keyCode, KeyEvent event){
    	//if(event.getAction()==1)
    	//	return false;
    	
    	if(keyCode==66 || keyCode==KeyEvent.KEYCODE_ENTER)
    	{
	    	RadioButton rbtReceiving = (RadioButton)this.findViewById(R.id.rbtReceiving);
	    	TextView txtInputBarcode = (TextView)this.findViewById(R.id.txtInputBarcode);
	    	
	    	String barcode = (String)txtInputBarcode.getText().toString();
	    	barcode = barcode.replace("\n", "");
	    	txtInputBarcode.setText("");
	    	if(barcode.length()==0)
	    		return false;
	    	
	    	CipherConnectDemoDataObject obj = null;
	    	if(rbtReceiving.isChecked())
	    		obj = this.provider.AddQuantity(barcode);
	    	else
	    		obj = this.provider.DeleteQuantity(barcode);
	    	
	    	this.DB2UI(obj);
	    	
	    	//v.requestFocus();
	    	//v.clearFocus();
	    	return true;
    	}
    	return false;
	}
    
    private void DB2UI(CipherConnectDemoDataObject obj){
    	TextView txtQuantity = (TextView)this.findViewById(R.id.txtQuantity);
    	TextView txtBarcode = (TextView)this.findViewById(R.id.txtBarcode);
    	TextView txtName = (TextView)this.findViewById(R.id.txtName);
    	TextView txtCompany = (TextView)this.findViewById(R.id.txtCompany);
    	TextView txtDescription = (TextView)this.findViewById(R.id.txtDescription);
    	ImageView imagePicture = (ImageView)this.findViewById(R.id.imagePicture);

    	if(obj!=null){
        	txtQuantity.setText(Integer.toString(obj.getQuantity()));
        	txtBarcode.setText(obj.getBarcode());
        	txtName.setText(obj.getName());
        	txtCompany.setText(obj.getCompany());
        	txtDescription.setText(obj.getDescription());
        	
        	String picturePath = obj.getPicturePath();
        	if(picturePath==null || picturePath.length()==0)
        		imagePicture.setImageResource(R.drawable.null_image);
        	else{
        		if(picturePath.equals("80008062.jpg"))
        			imagePicture.setImageResource(R.drawable.c_80008062);
        		else if(picturePath.equals("83008330.jpg"))
        			imagePicture.setImageResource(R.drawable.c_83008330);
        		else if(picturePath.equals("84008400.jpg"))
        			imagePicture.setImageResource(R.drawable.c_84008400);
        		else if(picturePath.equals("85008500.jpg"))
        			imagePicture.setImageResource(R.drawable.c_85008500);
        		else if(picturePath.equals("94009400.jpg"))
        			imagePicture.setImageResource(R.drawable.c_94009400);
        		else{
        			try {
        				Uri uriPicture = Uri.parse(picturePath);
            			imagePicture.setImageURI(uriPicture);
            			if(imagePicture.getDrawable()==null)
            				imagePicture.setImageResource(R.drawable.null_image);
					} catch (Exception e) {
						imagePicture.setImageResource(R.drawable.null_image);
					}
        		}
        	}
    	}
    	else{
        	txtQuantity.setText("");
        	txtBarcode.setText("");
        	txtName.setText("");
        	txtCompany.setText("");
        	txtDescription.setText("");
    		imagePicture.setImageResource(R.drawable.null_image);
    	}
    }
 
	@Override
	protected void onDestroy() {
		this.provider.close();
		super.onDestroy();
	}
}