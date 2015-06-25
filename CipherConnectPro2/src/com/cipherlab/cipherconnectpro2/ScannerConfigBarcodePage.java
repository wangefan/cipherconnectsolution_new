package com.cipherlab.cipherconnectpro2;

import com.cipherlab.help.CipherLog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class ScannerConfigBarcodePage extends Activity 
{
	//Constant
	private final String mTAG = "ScannerConfigBarcodePage";
	
	//Data members
	private ICipherConnectManagerService mCipherConnectService = null;
	private ImageView mBCodeEnableAuthImage;
	private ImageView mBCodeDisableAuthImage;
	private ImageView mBCodeEnableSppImage;

	//inner class
	private ServiceConnection mSConnection = new ServiceConnection() 
	{
		public void onServiceConnected(ComponentName className, IBinder service) 
		{
			CipherLog.d(mTAG, "onServiceConnected, get mListenConnService and set SetKeepService true");
            mCipherConnectService = ((CipherConnectManagerService.LocalBinder) service).getService();
            
            if(mCipherConnectService != null)
            {
            	float fWidthPxl = 300.0f;
        		float fHeightPxl = 100.0f;
        		
        		Resources rsc = getResources();
        		if(rsc != null)
        		{
        			fWidthPxl = rsc.getDimension(R.dimen.BarcodeWidth);
        			fHeightPxl = rsc.getDimension(R.dimen.BarcodeHeight);
        		}
        		
        		mBCodeEnableAuthImage.setImageBitmap(mCipherConnectService.GetEnableAuthBarcodeImage((int)fWidthPxl, (int)fHeightPxl));
        		mBCodeDisableAuthImage.setImageBitmap(mCipherConnectService.GetDisableAuthBarcodeImage((int)fWidthPxl, (int)fHeightPxl));
        		mBCodeEnableSppImage.setImageBitmap(mCipherConnectService.GetEnableSppBarcodeImage((int)fWidthPxl, (int)fHeightPxl));
            }
        }

        public void onServiceDisconnected(ComponentName className) 
        {
        	CipherLog.d(mTAG, "onServiceDisconnected");
        	mCipherConnectService = null;
        }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scannerconfigbarcodepage);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBCodeEnableAuthImage = (ImageView) findViewById(R.id.imageAuthenOn);
		mBCodeDisableAuthImage = (ImageView) findViewById(R.id.imageAuheOff);
		mBCodeEnableSppImage = (ImageView) findViewById(R.id.imageSppOn);
		
		 try {
			 Intent intent = new Intent(this, CipherConnectManagerService.class);
			 bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
	     } catch (Exception e) {
			 e.printStackTrace();
			 finish();
	     }
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
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
