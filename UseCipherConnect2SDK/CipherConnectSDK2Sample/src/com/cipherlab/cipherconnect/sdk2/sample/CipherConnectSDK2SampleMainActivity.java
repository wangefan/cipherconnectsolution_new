package com.cipherlab.cipherconnect.sdk2.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cipherlab.cipherconnect.sdk2.CipherConnCtrl2EZMet;
import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;
import com.cipherlab.cipherconnect.sdk2.ICipherConnCtrl2EZMet;
import com.cipherlab.cipherconnect.sdk2.ICipherConnectControl2Listener;
import com.cipherlab.cipherconnect.sdk2.sample.R;

public class CipherConnectSDK2SampleMainActivity extends Activity {
	///broadcast actions, broadcast to 
    public static final String ACTION_CLOSE_SELF =
            "com.cipherlab.cipherconnect.sdk2.sample.CipherConnectSDK2SampleMainActivity.CLOSE";
	public static ICipherConnCtrl2EZMet _CipherConnectControl = null;
	public ICipherConnectControl2Listener _ctrl2Listener = new ICipherConnectControl2Listener() {
		
		@Override
		public void onBeginConnecting(ICipherConnBTDevice device) {
			Log.d("CipherConnectControl_test","onBeginConnecting("+device.getDeviceName()+")");
			CipherConnectControl_onBeginConnecting(device.getDeviceName());
		}
		@Override
		public void onCipherConnectControlError(ICipherConnBTDevice device, int id, String message) {
			Log.d("CipherConnectControl_test","onCipherConnectControlError("+ device.getDeviceName() +",id="+id+",message="+message+")");
			CipherConnectControl_onCipherConnectControlError(device.getDeviceName(), id, message);
		}
		@Override
		public void onConnected(ICipherConnBTDevice device) {
			Log.d("CipherConnectControl_test","onConnected("+ device.getDeviceName() +")");
			CipherConnectControl_onConnected(device.getDeviceName());
		}
		@Override
		public void onConnecting(ICipherConnBTDevice device) {
			Log.d("CipherConnectControl_test","onConnecting("+device.getDeviceName()+")");
			CipherConnectControl_onConnecting(device.getDeviceName());
		}
		
		@Override
		public void onDisconnected(ICipherConnBTDevice device) {
			Log.d("CipherConnectControl_test","onDisconnected("+ device.getDeviceName() +")");
			CipherConnectControl_onDisconnected(device.getDeviceName());
		}
		
		@Override
		public void onGetLEDevice(ICipherConnBTDevice arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onMinimizeKeyboard() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onReceivingBarcode(ICipherConnBTDevice device, String barcode) {
			Log.d("CipherConnectControl_test","onReceivingBarcode("+device.getDeviceName()+",barcode="+barcode+")");
			CipherConnectControl_onReceivingBarcode(device.getDeviceName(), barcode);
		}
	};
	
	public static boolean mBIsListening = false;
	
	private CipherConnectSDK2SampleDataProvider mDataProvider;
    
    protected static final int MENU_About = Menu.FIRST;
    protected static final int MENU_Setting = Menu.FIRST+1;
    protected static final int MENU_Quit = Menu.FIRST+2;
    
    private EditText mTxtInputBarcode;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //android.os.Debug.waitForDebugger();
        setContentView(R.layout.main);
        
        this.mDataProvider = new CipherConnectSDK2SampleDataProvider(this);
        this.mTxtInputBarcode = (EditText)this.findViewById(R.id.txtInputBarcode);

        this.init_ui();
        this.CipherConnectControl_init();
    }
    
    private void CipherConnectControl_init(){
    	
    	initCtrl2();
    	
    	//set auto reconnect
    	boolean bAutoReconnect = CipherConnectSDK2SampleSettingInfo.isAutoReconnect(this);
    	try {
        	_CipherConnectControl.setAutoReconnect(bAutoReconnect);
		} catch (Exception e) {
			this.runOnUiThread(new MessageDlgAction("Can't be set AutoReconnect.["+e.getMessage()+"]"));
		}
    }
    private void initCtrl2() {
    	_CipherConnectControl = new CipherConnCtrl2EZMet(this);
    	_CipherConnectControl.addCipherConnect2Listener(_ctrl2Listener);
    }
    public void CipherConnectControl_onBeginConnecting(String deviceName) {
    	this.runOnUiThread(new MessageDlgAction("onBeginConnecting(deviceName="+deviceName+")"));
	}
    public void CipherConnectControl_onDisconnected(String deviceName) {
    	this.runOnUiThread(new MessageDlgAction("onDisconnected(deviceName="+deviceName+")"));
	}
	public void CipherConnectControl_onConnecting(String deviceName) {
		this.runOnUiThread(new MessageDlgAction("onConnecting(deviceName="+deviceName+")"));
	}
	public void CipherConnectControl_onConnected(String deviceName) {
		this.runOnUiThread(new MessageDlgAction("onConnected(deviceName="+deviceName+")"));
		final Intent brdConnState = new Intent(ACTION_CLOSE_SELF);
        sendBroadcast(brdConnState);
	}
	public void CipherConnectControl_onCipherConnectControlError(String deviceName, int id,
					String message){
		this.runOnUiThread(new MessageDlgAction("onCipherConnectControlError(deviceName="+deviceName+",id="+id+",message="+message+")"));
	}
	public void CipherConnectControl_onReceivingBarcode(String deviceName, String barcode) {
		this.runOnUiThread(new InputBarcodeAction(barcode));
	}
	
	private class MessageDlgAction extends Thread{
		private String mMessage;
		
		public MessageDlgAction(String message){
			this.mMessage = message;
		}
		
		public void run(){
			Toast.makeText(CipherConnectSDK2SampleMainActivity.this, this.mMessage, Toast.LENGTH_SHORT).show();
		}
	}
	
	private class InputBarcodeAction extends Thread{
		private String mBarcode;
		public InputBarcodeAction(String barcode){
			mBarcode = barcode;
		}
		public void run(){
			mTxtInputBarcode.setText(mBarcode);
			SetInputBarcode(mBarcode);
		}
	}
	
    private void init_ui(){
    	this.mTxtInputBarcode.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return txtInputBarcode_onKey(v,keyCode,event);
			}
		});
    	ImageView imagePicture = (ImageView)this.findViewById(R.id.imagePicture);
    	imagePicture.setImageResource(R.drawable.null_image);
    }
    
    private boolean txtInputBarcode_onKey(View v, int keyCode, KeyEvent event){
    	if(event.getAction()==1)
    		return false;
    	
    	if(keyCode==66){
	    	String barcode = (String)this.mTxtInputBarcode.getText().toString();
	    	if(barcode.length()==0)
	    		return false;
	    	
	    	SetInputBarcode(barcode);
	    	return true;
    	}
    	return false;
	}
    
    private void SetInputBarcode(String barcode){
    	RadioButton rbtReceiving = (RadioButton)this.findViewById(R.id.rbtReceiving);
    	
    	CipherConnectSDK2SampleDataObject obj = null;
    	if(rbtReceiving.isChecked())
    		obj = this.mDataProvider.AddQuantity(barcode);
    	else
    		obj = this.mDataProvider.DeleteQuantity(barcode);
    	
    	this.DB2UI(obj);
    }
    
    private void DB2UI(CipherConnectSDK2SampleDataObject obj){
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
    		Toast.makeText(this, "Can not find "+txtBarcode.getText(), Toast.LENGTH_SHORT).show();

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
		if(_CipherConnectControl!=null){
			_CipherConnectControl.setAutoReconnect(false);
			_CipherConnectControl.disconnect();
		}
		
		if(this.mDataProvider!=null)
			this.mDataProvider.close();
		
		_CipherConnectControl = null;
		this.mDataProvider = null;
		
		CipherConnectSDK2SampleSettingInfo.destroy();
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_About, 0, "About...");
		menu.add(0, MENU_Setting, 0, "Setting...");
		menu.add(0, MENU_Quit, 0, "Quit");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	     super.onOptionsItemSelected(item);
	     switch(item.getItemId()){
	          case MENU_About:
	        	  Intent about = new Intent(this,CipherConnectSDK2SampleAboutActivity.class);
	        	  setResult(RESULT_OK, about);
	        	  startActivity(about);
	              break;  
	          case MENU_Setting:
	        	  final boolean bisConnected = _CipherConnectControl.isConnected();
	        	  if(bisConnected) {
	        		  Intent setting = new Intent(this, DisconnectActivity.class);
		        	  setResult(RESULT_OK, setting);
		        	  startActivity(setting);
	        	  } else {
	        		  Intent setting = new Intent(this,CipherConnectSDK2SampleSettingActivity.class);
		        	  setResult(RESULT_OK, setting);
		        	  startActivity(setting);
	        	  }
	              break;
	         case MENU_Quit:
	             finish();
	             break;
	       }
	       return true;
	}
}