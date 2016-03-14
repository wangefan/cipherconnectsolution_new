package com.cipherlab.cipherconnect.sdk2.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DisconnectActivity extends Activity {
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
        public void onReceive(Context context, Intent intent) 
		{
        	final String action = intent.getAction();
        	if(action.equals(CipherConnectSDK2SampleMainActivity.ACTION_CLOSE_SELF))
	            finish();
		}
    };
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disconnect_activity);
        
        Button btnDisconn = (Button) findViewById(R.id.idBtDisconnect);
        btnDisconn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CipherConnectSDK2SampleMainActivity._CipherConnectControl.disconnect();
				finish();
			}
		});
    }

	@Override
	protected void onResume() {
		final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CipherConnectSDK2SampleMainActivity.ACTION_CLOSE_SELF);
		registerReceiver(mReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mReceiver);
		super.onPause();
	}
}
