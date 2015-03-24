package com.cipherlab.cipherconnectpro;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class BuildConnMethodPreference extends Preference 
{
	//data members
	final private String SLAVE = "slave";
	final private String MASTER = "master";
	private LinearLayout mLinearView = null;
	private boolean mIsSlaveConn = true;
	private Button mbtnBuildConn = null;
	private Button mbtnScanDev = null;
	private ToggleButton mbtnConnMathod = null;
	private OnPreferenceClickListener mOnPreferenceClickListener;
	private Button.OnClickListener mOnPreferenceClickScanListener ;
	private TextView mtvLastDevName = null;
	
	//member functions
	private Button.OnClickListener mClickBuildConn = new Button.OnClickListener()
    {
    	@Override
		public void onClick(View v)
		{
    		if (mOnPreferenceClickListener != null )
    		{
    			mOnPreferenceClickListener.onPreferenceClick(BuildConnMethodPreference.this);
    		}
		}
    };
    
    private ToggleButton.OnClickListener mClickConnMethod = new ToggleButton.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
		  	mIsSlaveConn = !mIsSlaveConn;
		  	mPersistValuses();  //Save to preference setting.
		  	
	    	mUpdateUI();
		}
	};
	
	private Button.OnClickListener mClickScan = new Button.OnClickListener()
    {
    	@Override
		public void onClick(View v)
		{
    		if (mOnPreferenceClickScanListener != null )
    		{
    			mOnPreferenceClickScanListener.onClick(v);
    		}
		}
    };
	
	private void mInitCtrls()
	{
		if(mLinearView == null)
		{
			final LayoutInflater layoutInflater =
		            (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			mLinearView = (LinearLayout)layoutInflater.inflate(R.layout.build_conn_method_preference, null, false);
		}
		
		if(mbtnBuildConn == null)
		{
			mbtnBuildConn = (Button) mLinearView.findViewById(R.id.btnBuildConn);
			if(mbtnBuildConn != null)
			{
				mbtnBuildConn.setOnClickListener(mClickBuildConn);
			}			
		}
		
		if(mbtnConnMathod == null)
		{
			mbtnConnMathod = (ToggleButton) mLinearView.findViewById(R.id.btnConnMethod);
			if(mbtnConnMathod != null)
			{
				mbtnConnMathod.setOnClickListener(mClickConnMethod);
			}			
		}
		
		if(mbtnScanDev == null)
		{
			mbtnScanDev = (Button) mLinearView.findViewById(R.id.btnScanDevice);
			if(mbtnScanDev != null)
			{
				mbtnScanDev.setOnClickListener(mClickScan);
			}			
		}
		if(mtvLastDevName == null)
		{
			mtvLastDevName = (TextView) mLinearView.findViewById(R.id.tvwLastDevice);
		}
	}
	
	private void mGetPersistValues()
	{
		String strDefault = getContext().getResources().getString(R.string.Str_defaultBMPreference);
		String val = getPersistedString(strDefault);
		int nPosBreak = val.indexOf(";", 0);
		String strMode = val.substring(0, nPosBreak);
		String strDev = val.substring(nPosBreak + 1);
		mIsSlaveConn = strMode.equals(SLAVE) ? true : false;
		if(mtvLastDevName != null)
			mtvLastDevName.setText(strDev);
	}
	
	private void mPersistValuses()
	{
		String strValuse = mIsSlaveConn ? SLAVE : MASTER;
		strValuse = strValuse + ";" + mtvLastDevName.getText();
		persistString(strValuse);
	}
	
	private void mUpdateUI()
	{
		if(mIsSlaveConn)
		{
			mbtnScanDev.setEnabled(false);
		}
		else
		{
			mbtnScanDev.setEnabled(true);
		}
	}
	
	//public functions
	public boolean IsSlaveConn()
	{
		return mIsSlaveConn;
	}

    public BuildConnMethodPreference(Context context) {
        super(context);
        mInitCtrls();
    }

    public BuildConnMethodPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInitCtrls();
    }

    public BuildConnMethodPreference(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
        mInitCtrls();
    }
	
    /**
     * Sets the callback to be invoked when this Preference is clicked.
     * 
     * @param onPreferenceClickListener The callback to be invoked.
     */
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
    	mOnPreferenceClickListener = onPreferenceClickListener;
    }
    
    public void setOnPreferenceClickScanListener(Button.OnClickListener onPreferenceClickScanListener) {
    	mOnPreferenceClickScanListener = onPreferenceClickScanListener;
    }
    
    public void setButtonMode(boolean bIsConnected) {
    	if(bIsConnected)
    	{
    		mbtnConnMathod.setEnabled(false);
    		mbtnBuildConn.setText(R.string.strDisconnect);
    		mbtnScanDev.setEnabled(false);
    		
    	}
    	else
    	{
    		mbtnConnMathod.setEnabled(true);
    		mbtnBuildConn.setText(R.string.setting_buildconnect);
    		mbtnScanDev.setEnabled(!IsSlaveConn());
    	}
    }
    
    public void setLastDevName(String name) {
    	if(mtvLastDevName != null)
    	{
    		mtvLastDevName.setText(name);
    		mPersistValuses();
    	}
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
        	mGetPersistValues();
        } else {
            // Set default state from the XML attribute
        	String strDefaultVal = (String) defaultValue;
        	if(strDefaultVal != null)
        	{
        		persistString(strDefaultVal);
        	}
        }
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) 
        {
        	convertView = mLinearView;
        	mbtnConnMathod.setChecked(mIsSlaveConn);
        	mUpdateUI();
        }

        return convertView;
    }
}