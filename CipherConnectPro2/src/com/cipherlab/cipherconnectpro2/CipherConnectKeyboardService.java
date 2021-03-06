package com.cipherlab.cipherconnectpro2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.cipherlab.cipherconnect.sdk2.ICipherConnBTDevice;
import com.cipherlab.help.CipherLog;
import com.cipherlab.util.KeyboardUtil;
import com.example.android.softkeyboard.SoftKeyboard;

public class CipherConnectKeyboardService extends SoftKeyboard {

    private boolean isOnStartInputView = false;
    private static final String TAG = "CipherConnectKeyboardService()";
	private ICipherConnectManagerService mCipherConnectManagerService;
	private Handler mMainThrdHandler = new Handler();
    private ICipherConnectManagerListener mCipherConnectManagerListener = new ICipherConnectManagerListener()
    {
        public void onDisconnected() {
        }

        public void onConnecting() {
        }

        public void onConnected() {
        }

        public void onConnectError(String message) {
        }

        public void onBarcode(String barcode) {
            sendBarcode(barcode);
        }
        
        public void onMinimizeKeyboard()
        {
        	//Handle Minimize Command
        	mMainThrdHandler.post(new Runnable(){
            	public void run()
            	{
            		boolean bSetMin = !CipherConnectSettingInfo.isMinimum(CipherConnectKeyboardService.this);	
                    setKeyboardMinimize(bSetMin);
                    CipherConnectSettingInfo.setMinimum(bSetMin, CipherConnectKeyboardService.this);
            	}
        	});
        }
        
        public void onGetLEDevice(final ICipherConnBTDevice device) {
        	
        }
    };
    
    private ServiceConnection mSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCipherConnectManagerService = ((CipherConnectManagerService.LocalBinder) service)
                                           .getService();
            
            mCipherConnectManagerService.AddListener(mCipherConnectManagerListener);
        }

        public void onServiceDisconnected(ComponentName className) {
        	if(mCipherConnectManagerListener != null)
        	{
        		mCipherConnectManagerService.RemoveListener(mCipherConnectManagerListener);
        	}
        }
    };

    @Override
    public void onCreate() {
    	CipherLog.d(TAG, "onCreate(): begin");
        super.onCreate();

        if (CipherLog._DEBUG)
            android.os.Debug.waitForDebugger();

        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
            CipherLog.d(TAG, "onCreate(): start CipherConnectManagerService");
        } catch (Exception e) {
            CipherLog.e(this.getResources().getString(R.string.ime_name),
                  "CipherConnectSettingActivity.ConnectStatus_bt_startService:",
                  e);
        }
        CipherLog.d(TAG, "onCreate(): end");
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
    	CipherLog.d(TAG, "onStartInputView(): restarting= "+restarting);
    	
        super.onStartInputView(info, restarting);
        this.isOnStartInputView = true;
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
    	CipherLog.d(TAG, "onFinishInputView(): begin");
        super.onFinishInputView(finishingInput);
        this.isOnStartInputView = false;
    }

    @Override
    public void onDestroy() {
    	CipherLog.d(TAG, "onDestroy(): begin");
    	if(mCipherConnectManagerListener != null && CipherConnectSettingInfo.isDisconnSwch(this))
    	{
    		mCipherConnectManagerService.disConnect();
    	}
    	
        this.unbindService(this.mSConnection);
        
        if(mCipherConnectManagerListener != null)
    	{
    		mCipherConnectManagerService.RemoveListener(mCipherConnectManagerListener);
    	}

        if (!KeyboardUtil
                .isEnableingKeyboard(this, R.string.ime_service_name)) {
            if (this.mCipherConnectManagerService != null) {
                mCipherConnectManagerService.stopSelf();
            }
        }
        
        this.mCipherConnectManagerService = null;

        super.onDestroy();
        CipherLog.d(TAG, "onDestroy(): end");
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // super.onKey(primaryCode, keyCodes);
    	final Integer nSettingCode = getResources().getInteger(R.integer.keycodeSetting);
    	final Integer nPreIM = getResources().getInteger(R.integer.keycodePreIM);
    	if( primaryCode == nSettingCode)
    	{
    		Intent intentSett = new Intent(this, CipherConnectSettingActivity.class);
    		intentSett.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(intentSett);
    		return;
    	}
    	else if( primaryCode == nPreIM)
    	{
    		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	    final IBinder token = getWindow().getWindow().getAttributes().token;
    	    imm.switchToLastInputMethod(token);
    		return;
    	}
    	
        InputConnection ic = getCurrentInputConnection();
        switch (primaryCode) {
        case Keyboard.KEYCODE_CANCEL:
        case Keyboard.KEYCODE_DELETE:
        case Keyboard.KEYCODE_DONE:
        case Keyboard.KEYCODE_ALT:
        case Keyboard.KEYCODE_SHIFT:
        case Keyboard.KEYCODE_MODE_CHANGE:
        case 10:
            super.onKey(primaryCode, keyCodes);
            break;
            // case Keyboard.KEYCODE_SHIFT:
            // ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, 115));
            // this.getk
            // updateShiftKeyState();
            // break;
        default:
            // KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, primaryCode);
            // ic.sendKeyEvent(event);
            // ic.sendKeyEvent(event)
            // int caps = ic.getCursorCapsMode(reqModes);
        	
        	if(this.mInputView!=null)
        	{
                if (this.mInputView.isShifted()) 
                {
                    primaryCode = Character.toUpperCase(primaryCode);

                    //Update by Visual,2016/7/4
                    //Supported CapsLock
                    if(super.mCapsLock==false)
                    	this.mInputView.setShifted(false);
                }
        	}

            StringBuffer sb = new StringBuffer();
            sb.append((char) primaryCode);
            
            if(ic!=null)
            	ic.commitText(sb.toString(), 1);
            
            sb = null;
            break;
        }
    }

    public synchronized void sendBarcode(String barcode) {
    	//CipherLog.d(TAG, "sendBarcode(): barcode= "+barcode);
    	if (this.isOnStartInputView) {
            if (barcode == null)
                return;
            if (barcode.length() == 0)
                return;

            char[] data = barcode.toCharArray();
            CipherLog.e("sendBarcode", "sendBarcode("+barcode+")");
            
            this.waitEx(5);

            InputConnection ic = this.getCurrentInputConnection();
           
            for (char c : data) {
                
            	//Added by Visual.chen, 2016/05/17{
            	if (c == '\n') 
            	{
        	    	CipherLog.e("sendBarcode", "Enter");
        	    	this.keyDownUp(ic, KeyEvent.KEYCODE_ENTER);
                    continue;
                } 
            	else if (c == '\t') 
            	{
        	    	CipherLog.e("sendBarcode", "Tab");
        	    	this.keyDownUp(ic, KeyEvent.KEYCODE_TAB);
                    continue;
                }
            	//}Added by Visual.chen, 2016/05/17
            	
                this.waitEx(3);
                String s = String.valueOf(c);
    	    	CipherLog.e("sendBarcode", "commitText("+s+")");
                ic.commitText(s, 1);
                
                String BarcodeInterval = CipherConnectSettingInfo.getBarcodeInterval(this);
            	if (BarcodeInterval.equals("No select"))
            		BarcodeInterval = "7";
            	
            	long timer = Integer.parseInt(BarcodeInterval);
            	this.waitEx(timer);                    
            }
        }
    }
    
    private void waitEx(long time)
    {
        try {
			this.wait(time);
		} 
        catch (InterruptedException e) {
        	e.printStackTrace();
		}
    }
    
    /*
     * Added by Visual.chen, 2016/05/17
     */
    private void keyDownUp(InputConnection inputConnection, int keyEventCode) 
    {
    	if(inputConnection==null)
    		return;
    	
    	inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
    	inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    @Override
    public void onUnbindInput() {
        CipherLog.d("xxxx", "onUnbindInput");
        if (KeyboardUtil.isEnableingKeyboard(this, R.string.ime_service_name) == true) {
            if (KeyboardUtil.checkKeyboard(this) == false) {
                CipherConnectNotification.pause_notify(this);
            }
        }
        super.onUnbindInput();
        CipherLog.d("xxxx", "onUnbindInput end");
    }

    /*
     * private boolean isEnableingKeyboard(){ InputMethodManager imm =
     * (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     * List<InputMethodInfo> list = imm.getEnabledInputMethodList();
     * if(list==null || list.size()==0) return false;
     *
     * for (InputMethodInfo imo : list) { String name = imo.getServiceName();
     * CipherLog.d(this.getResources().getString(R.string.ime_name),
     * "InputMethodInfo.name="+name); if(name==null || name.length()==0)
     * continue;
     *
     * if(name.equals(CipherConnectKeyboardService.class.getName())){
     * CipherLog.d(this.getResources().getString(R.string.ime_name),
     * "The CipherConnectKeyboard was enable."); return true; }
     *
     * }
     *
     * CipherLog.d(this.getResources().getString(R.string.ime_name),
     * "The CipherConnectKeyboard was not enable."); return false; }
     */
}
