package com.cipherlab.cipherconnectpro2;

import com.cipherlab.cipherconnectpro2.R;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BuildConnMethodPreference extends Preference 
{
	public enum BCEnum {
        SLAVE, SLAVE_QR, MASTER;
        @Override
        public String toString() 
        {
          switch(this) {
            case SLAVE: return CipherConnectSettingInfo.SLAVE;
            case SLAVE_QR: return CipherConnectSettingInfo.SLAVEQR;
            case MASTER: return CipherConnectSettingInfo.MASTER;
            default: throw new IllegalArgumentException();
          }
        }
    }
	//data members
	private ICipherConnectManagerService mCipherConnectService = null;
	private String mStrDefaultValues = "";
	private String mStrDefaultMode = "";
	private String mStrDefaultDevName = "";
	private String mStrDefaultDevAddr = "";
	private LinearLayout mLinearView = null;
	private BCEnum mBConnState = BCEnum.SLAVE;
	private boolean mIsAutoReConn = false;
	private String  mDeviceName = "";
	private String  mDeviceAddr = "";
	private Button mbtnBuildConn = null;
	private Button mbtnSearchDev = null;
	private ImageButton mbtnConnMathod = null;
	private OnPreferenceClickListener mOnPreferenceClickListener;
	private Button.OnClickListener mOnPreferenceClickScanListener ;
	private CheckBox.OnCheckedChangeListener mOnAutoCheckedChangeListener;
	private CheckBox mckAutoReConn = null;
	
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
			int next = ((mBConnState.ordinal() + 1) % BCEnum.values().length);
			mSetState(BCEnum.values()[next]);
			if(mCipherConnectService != null)
		  	{
		  		if(mBConnState != BCEnum.MASTER)
		  			mCipherConnectService.StartListenConn();
		  		else
		  			mCipherConnectService.StopListenConn();
		  	}
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
    
    private CheckBox.OnCheckedChangeListener mOnCheckedChangeListener = new CheckBox.OnCheckedChangeListener()
    {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) 
		{
			mIsAutoReConn = isChecked;
			mPersistValuses();
			if (mOnAutoCheckedChangeListener != null)
				mOnAutoCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
		}
    };

    private void mSetState(BCEnum state) {
        if(mBConnState == null)return;
        mBConnState = state;
    }
    
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
			mbtnConnMathod = (ImageButton) mLinearView.findViewById(R.id.btnConnMethod);
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
		if(mckAutoReConn == null)
		{
			mckAutoReConn = (CheckBox) mLinearView.findViewById(R.id.ckAutoReConn);
			mckAutoReConn.setOnCheckedChangeListener(mOnCheckedChangeListener);
		}
		
		mStrDefaultMode = getContext().getResources().getString(R.string.Str_defaultMode);
		mStrDefaultDevName = getContext().getResources().getString(R.string.Str_defaultDevName);
		mStrDefaultDevAddr = getContext().getResources().getString(R.string.Str_defaultDevAddr);
		boolean bDefaultAutoReConn = Boolean.parseBoolean(getContext().getResources().getString(R.string.Str_defaultAutoReConn));
		String strFormat = getContext().getResources().getString(R.string.Str_BMPrefFormat);
		mStrDefaultValues = String.format(strFormat, mStrDefaultMode, mStrDefaultDevName, mStrDefaultDevAddr, bDefaultAutoReConn);
		
		mDeviceName = mStrDefaultDevName;
		mDeviceAddr = mStrDefaultDevAddr;
		mIsAutoReConn = bDefaultAutoReConn;
	}
	
	//combine values to single string to save.
	private String mGeneratePersisString(BCEnum bcMode, String devName, String devAddr, boolean bAutoReConn)
	{
		String strValuse = (bcMode.equals(BCEnum.SLAVE)  ? CipherConnectSettingInfo.SLAVE : (bcMode.equals(BCEnum.SLAVE_QR) ? CipherConnectSettingInfo.SLAVEQR : CipherConnectSettingInfo.MASTER)) + ";" + devName + "|" + devAddr + "@" + ((Boolean)mIsAutoReConn).toString();
		return strValuse;
	}
	
	private void mPersistValuses()
	{
		persistString(mGeneratePersisString(mBConnState, mDeviceName, mDeviceAddr, mIsAutoReConn));
		CipherConnectSettingInfo.setBCMode(mBConnState.toString(), getContext());
	}
	
	public void updateButtons()
	{
		if(mCipherConnectService == null)
			return;
		switch(mBConnState)
		{
			case SLAVE:
			case SLAVE_QR:
			{
				mbtnSearchDev.setVisibility(View.GONE);
				mckAutoReConn.setVisibility(View.GONE);
				if(mCipherConnectService.GetConnState() == ICipherConnectManagerService.CONN_STATE.CONN_STATE_CONNECTED)
		    	{
					mbtnBuildConn.setEnabled(true);
		    		mbtnBuildConn.setText(R.string.strDisconnect);
		    		mbtnConnMathod.setEnabled(false);
		    		if (mBConnState.equals(BCEnum.SLAVE)) 
					{
		            	mbtnConnMathod.setImageResource(R.drawable.slave_conn_disable);
					}
					else if(mBConnState.equals(BCEnum.SLAVE_QR)) 
					{
						mbtnConnMathod.setImageResource(R.drawable.slave_conn_qr_disable);
					}
		    	}
		    	else
		    	{
		    		mbtnBuildConn.setEnabled(true);
		    		mbtnBuildConn.setText(R.string.setting_waitconnect);
		    		mbtnConnMathod.setEnabled(true);	    		
		    		if (mBConnState.equals(BCEnum.SLAVE)) 
					{
		            	mbtnConnMathod.setImageResource(R.drawable.slave_conn);
					}
					else if(mBConnState.equals(BCEnum.SLAVE_QR)) 
					{
						mbtnConnMathod.setImageResource(R.drawable.slave_conn_qr);
					}
		    	}
			}
			break;
			case MASTER:
			default:		
			{
				mbtnSearchDev.setVisibility(View.VISIBLE);
				mckAutoReConn.setVisibility(View.VISIBLE);
				if(mCipherConnectService.GetConnState() == ICipherConnectManagerService.CONN_STATE.CONN_STATE_CONNECTED)
		    	{
					mbtnBuildConn.setEnabled(true);
		    		mbtnBuildConn.setText(R.string.strDisconnect);
		    		mbtnSearchDev.setEnabled(false);
		    		mbtnConnMathod.setEnabled(false);
		    		mbtnConnMathod.setImageResource(R.drawable.master_conn_disable);
		    		mckAutoReConn.setEnabled(false);
		    	}
				else
				{
					//disable build connection button if no selection device. 
					if(mDeviceName.equals(mStrDefaultDevName) && 
					   mDeviceAddr.equals(mStrDefaultDevAddr)	)
						mbtnBuildConn.setEnabled(false);
					else
						mbtnBuildConn.setEnabled(true);
					String strConnFormat = getContext().getResources().getString(R.string.setting_buildconnect);
					String strText = String.format(strConnFormat, mDeviceName);
					mbtnBuildConn.setText(strText);
					mbtnSearchDev.setEnabled(true);
					mbtnConnMathod.setEnabled(true);
					mbtnConnMathod.setImageResource(R.drawable.master_conn);
					mckAutoReConn.setEnabled(true);
				}
			}
			break;
		}
	}
	
	//public functions
	public BCEnum GetBConnState()
	{
		return mBConnState;
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
    
    public void setPreferenceAutoChangeListener(CheckBox.OnCheckedChangeListener listener)
    {
    	mOnAutoCheckedChangeListener = listener;
    }
    
    public void setService(ICipherConnectManagerService cipherConnectService)
    {
    	mCipherConnectService = cipherConnectService;
    }

    
    public void setNoneDev()
    {
		mDeviceName = mStrDefaultDevName;
		mDeviceAddr = mStrDefaultDevAddr;
		mPersistValuses();
    }
    
    public void setLastDev(String devName, String devAddr)
    {
		mDeviceName = devName;
		mDeviceAddr = devAddr;
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
    
    public boolean getAutoReConn() 
    {
    	return mIsAutoReConn;
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
        	String strVals = getPersistedString(mStrDefaultValues);
    		
    		int nPosBreak = strVals.indexOf(";", 0);
    		String strMode = strVals.substring(0, nPosBreak);
    		String strDevName = strVals.substring(nPosBreak + 1, strVals.indexOf("|", 0));
    		nPosBreak = strVals.indexOf("|", 0);
    		String strAddr = strVals.substring(nPosBreak + 1, strVals.indexOf("@", 0));
    		nPosBreak = strVals.indexOf("@", 0);
    		String strAutoReConn = strVals.substring(nPosBreak + 1);
    		if(strMode.equals(CipherConnectSettingInfo.SLAVE))
    			mBConnState = BCEnum.SLAVE;
    		else if(strMode.equals(CipherConnectSettingInfo.SLAVEQR))
    			mBConnState = BCEnum.SLAVE_QR;
    		else if(strMode.equals(CipherConnectSettingInfo.MASTER))
    			mBConnState = BCEnum.MASTER;
    		mDeviceName = strDevName;
    		mDeviceAddr = strAddr;
    		mIsAutoReConn = Boolean.parseBoolean(strAutoReConn);
    		if(mckAutoReConn != null)
    			mckAutoReConn.setChecked(mIsAutoReConn);
        } else {
            // Set default state from the XML attribute
        	mPersistValuses();
        }
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) 
        {
        	convertView = mLinearView;
        	updateButtons();
        }

        return convertView;
    }
}