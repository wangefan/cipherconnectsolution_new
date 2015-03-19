package com.cipherlab.cipherconnectpro;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BuildConnMethodPreference extends Preference 
{
	//data members
	private LinearLayout mLinearView = null;
	private boolean mIsSlaveConn = true;
	private Button mbtnBuildConn = null;
	private Button mbtnScanDev = null;
	private ToggleButton mbtnConnMathod = null;
	private OnPreferenceClickListener mOnPreferenceClickListener;
	private Button.OnClickListener mOnPreferenceClickScanListener ;
	
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
	    	persistBoolean(mIsSlaveConn);	//Save to preference setting.
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
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
        	mIsSlaveConn = getPersistedBoolean(true);
        } else {
            // Set default state from the XML attribute
        	mIsSlaveConn = (Boolean) defaultValue;
        	persistBoolean(mIsSlaveConn);
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