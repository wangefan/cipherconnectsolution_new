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
	private ICipherConnectManagerService mCipherConnectService = null;
	private String mStrDefaultMode = "";
	private String mStrDefaultDevName = "";
	private String mStrDefaultDevAddr = "";
	private LinearLayout mLinearView = null;
	private boolean mIsSlaveConn = true;
	private String  mDeviceName = "";
	private String  mDeviceAddr = "";
	private Button mbtnBuildConn = null;
	private Button mbtnSearchDev = null;
	private ToggleButton mbtnConnMathod = null;
	private OnPreferenceClickListener mOnPreferenceClickListener;
	private Button.OnClickListener mOnPreferenceClickScanListener ;
	private TextView mtvLastDevNameTitle = null;
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
		  	updateButtons();
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
		
		if(mbtnSearchDev == null)
		{
			mbtnSearchDev = (Button) mLinearView.findViewById(R.id.btnSearchDevice);
			if(mbtnSearchDev != null)
			{
				mbtnSearchDev.setOnClickListener(mClickScan);
			}			
		}
		if(mtvLastDevNameTitle == null)
			mtvLastDevNameTitle = (TextView) mLinearView.findViewById(R.id.tvTitle);
		if(mtvLastDevName == null)
			mtvLastDevName = (TextView) mLinearView.findViewById(R.id.tvwLastDevice);
		
		
		mStrDefaultMode = getContext().getResources().getString(R.string.Str_defaultMode);
		mStrDefaultDevName = getContext().getResources().getString(R.string.Str_defaultDevName);
		mStrDefaultDevAddr = getContext().getResources().getString(R.string.Str_defaultDevAddr);
		mDeviceName = mStrDefaultDevName;
		mDeviceAddr = mStrDefaultDevAddr;
	}
	
	//combine values to single string to save.
	private String mGeneratePersisString(boolean IsSlaveMode, String devName, String devAddr)
	{
		String strValuse = (IsSlaveMode ? SLAVE : MASTER) + ";" + devName + "|" + devAddr;
		return strValuse;
	}
	
	private void mGetPersistValues()
	{
		String strDefFormat = getContext().getResources().getString(R.string.Str_defaultBMPrefFormat);
		String formatVal = getPersistedString(strDefFormat);
		String strVal = String.format(formatVal, mStrDefaultMode, mStrDefaultDevName, mStrDefaultDevAddr);
		int nPosBreak = strVal.indexOf(";", 0);
		String strMode = strVal.substring(0, nPosBreak);
		String strDevName = strVal.substring(nPosBreak + 1, strVal.indexOf("|", 0));
		nPosBreak = strVal.indexOf("|", 0);
		String strAddr = strVal.substring(nPosBreak + 1);
		mIsSlaveConn = strMode.equals(SLAVE) ? true : false;
		mDeviceName = strDevName;
		mDeviceAddr = strAddr;
		if(mtvLastDevName != null)
			mtvLastDevName.setText(mDeviceName);
	}
	
	private void mPersistValuses()
	{
		persistString(mGeneratePersisString(mIsSlaveConn, mDeviceName, mDeviceAddr));
	}
	
	public void updateButtons()
	{
		if(mCipherConnectService == null)
			return;
		if(mIsSlaveConn)
		{
			mbtnSearchDev.setVisibility(View.GONE);
			mtvLastDevNameTitle.setVisibility(View.GONE);
			mtvLastDevName.setVisibility(View.GONE);
			if(mCipherConnectService.GetConnState() == ICipherConnectManagerService.CONN_STATE.CONN_STATE_CONNECTED)
	    	{
				mbtnBuildConn.setEnabled(true);
	    		mbtnBuildConn.setText(R.string.strDisconnect);
	    		mbtnConnMathod.setEnabled(false);
	    	}
	    	else
	    	{
	    		mbtnBuildConn.setEnabled(true);
	    		mbtnBuildConn.setText(R.string.setting_waitconnect);
	    		mbtnConnMathod.setEnabled(true);	    		
	    	}
		}
		else	//Master mode
		{
			mbtnSearchDev.setVisibility(View.VISIBLE);
			mtvLastDevNameTitle.setVisibility(View.VISIBLE);
			mtvLastDevName.setVisibility(View.VISIBLE);
			if(mCipherConnectService.GetConnState() == ICipherConnectManagerService.CONN_STATE.CONN_STATE_CONNECTED)
	    	{
				mbtnBuildConn.setEnabled(true);
	    		mbtnBuildConn.setText(R.string.strDisconnect);
	    		mbtnConnMathod.setEnabled(false);
	    		mbtnSearchDev.setEnabled(false);
	    	}
			else
			{
				//disable build connection button if no selection device. 
				String strDefValue = mGeneratePersisString(false, mStrDefaultDevName, mStrDefaultDevAddr);
				String strCurValue = mGeneratePersisString(mIsSlaveConn, mDeviceName, mDeviceAddr);
				if(strCurValue.equals(strDefValue))
					mbtnBuildConn.setEnabled(false);
				else
					mbtnBuildConn.setEnabled(true);
				mbtnBuildConn.setText(R.string.setting_buildconnect);
				
				mbtnConnMathod.setEnabled(true);
				mbtnSearchDev.setEnabled(true);
			}
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
    
    public void setService(ICipherConnectManagerService cipherConnectService)
    {
    	mCipherConnectService = cipherConnectService;
    }
    
    public void setNoneDev()
    {
		mDeviceName = mStrDefaultDevName;
		mDeviceAddr = mStrDefaultDevAddr;
		mtvLastDevName.setText(mDeviceName);
		mPersistValuses();
    }
    
    public void setLastDev(String devName, String devAddr)
    {
		mDeviceName = devName;
		mDeviceAddr = devAddr;
		mtvLastDevName.setText(devName);
		mPersistValuses();
    }
    
    public String getLastDevName() 
    {
    	return mDeviceName;
    }
    
    public String getLastDevAddr() 
    {
    	return mDeviceAddr;
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
        	updateButtons();
        }

        return convertView;
    }
}