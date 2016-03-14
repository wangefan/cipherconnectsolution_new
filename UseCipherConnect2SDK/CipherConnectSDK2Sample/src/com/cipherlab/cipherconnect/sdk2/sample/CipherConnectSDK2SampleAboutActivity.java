package com.cipherlab.cipherconnect.sdk2.sample;


import com.cipherlab.cipherconnect.sdk2.sample.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CipherConnectSDK2SampleAboutActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        this.init_ui();
    }
	
   private void init_ui(){
	   TextView txtVersion = (TextView)this.findViewById(R.id.txtVersion);
	   txtVersion.setText(R.string.about_version_value);
    }
}