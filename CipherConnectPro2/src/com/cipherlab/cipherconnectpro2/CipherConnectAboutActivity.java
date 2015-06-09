package com.cipherlab.cipherconnectpro2;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.cipherlab.cipherconnectpro2.R;

public class CipherConnectAboutActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.init_ui();
    }

    /*
     * <!----------------------------------------------------------------->
     * @Name: init_ui()
     * @Description: Set CipherLab company information. 
     *   
     * @param: N/A
     * @param: N/A
     * return: N/A 
     * <!----------------------------------------------------------------->
     * */
    private void init_ui() {
        TextView txtVersion = (TextView) this.findViewById(R.id.txtVersion);
        txtVersion.setText(R.string.about_version_value);

        TextView txtWebsite = (TextView) this.findViewById(R.id.website);
        txtWebsite.setText(
            Html.fromHtml("<a href=\"http://www.cipherlab.com\">http://www.cipherlab.com</a> "));
        txtWebsite.setMovementMethod(LinkMovementMethod.getInstance());

        TextView txtHelp = (TextView) this.findViewById(R.id.help);
        txtHelp.setText(
            Html.fromHtml("<a href=\"http://connect.cipherlab.com\">http://connect.cipherlab.com</a> "));
        txtHelp.setMovementMethod(LinkMovementMethod.getInstance());
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
}
