package com.cipherlab.cipherconnectpro2;

import com.cipherlab.util.KeyboardUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class SetInputMethod extends Activity 
{
	//Data members
	ImageButton mBtnOpenSetting = null;
	ImageButton mBtnEnableIM = null;
	ImageButton mBtnChooseIM = null;
	
	//Receiver
	BroadcastReceiver mReceiver = new BroadcastReceiver ()
	{
		@Override
	    public void onReceive(Context context, Intent intent) 
		{
	        String action = intent.getAction();
	        if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) 
	        {
	            if(true == KeyboardUtil.isIMDefault(SetInputMethod.this))
	            {
	            	finish();
	            }
	        }
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setimputmehod);
		
		mBtnOpenSetting = (ImageButton) findViewById(R.id.btnOpenSettin);
		if(mBtnOpenSetting != null)
		{
			mBtnOpenSetting.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					setResult(RESULT_OK, null);
			        onBackPressed();
				}
			});
		}
		
		mBtnEnableIM = (ImageButton) findViewById(R.id.btnEnableIM);
		if(mBtnEnableIM != null)
		{
			mBtnEnableIM.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
				}
			});
		}
		
		mBtnChooseIM = (ImageButton) findViewById(R.id.btnChooseIM);
		if(mBtnChooseIM != null)
		{
			mBtnChooseIM.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					try {  
	                    ((InputMethodManager) getApplicationContext()  
	                            .getSystemService(Context.INPUT_METHOD_SERVICE))  
	                            .showInputMethodPicker();  
	                } catch (Exception e) {  
	                	Toast.makeText(SetInputMethod.this, "can`t open input mehod picker", Toast.LENGTH_LONG).show();  
	                    e.printStackTrace();  
	                }  
				}
			});
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() 
	{
		IntentFilter filter = new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED);
		registerReceiver(mReceiver, filter);
		if(mBtnEnableIM != null && mBtnChooseIM != null)
		{
			mBtnEnableIM.setEnabled(false);
			mBtnChooseIM.setEnabled(false);
			
			if(false == KeyboardUtil.isEnableingKeyboard(this, R.string.ime_service_name))
			{
				mBtnEnableIM.setEnabled(true);
			}
			else if(false == KeyboardUtil.isIMDefault(this))
			{
				mBtnChooseIM.setEnabled(true);
				
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
