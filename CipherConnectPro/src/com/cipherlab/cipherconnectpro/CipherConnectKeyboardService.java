package com.cipherlab.cipherconnectpro;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.inputmethodservice.Keyboard;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.cipherlab.cipherconnect.sdk.ICipherConnBTDevice;
import com.cipherlab.cipherconnectpro.R;
import com.cipherlab.util.KeyboardUtil;
import com.example.android.softkeyboard.SoftKeyboard;

public class CipherConnectKeyboardService extends SoftKeyboard {

    private boolean isOnStartInputView = false;
    private static final String TAG = "CipherConnectKeyboardService()";
	private ICipherConnectManagerService mCipherConnectManagerService;
   
    private ServiceConnection mSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mCipherConnectManagerService = ((CipherConnectManagerService.LocalBinder) service)
                                           .getService();
            
            init_ScannerService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mCipherConnectManagerService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (CipherConnectSettingInfo._DEBUG)
            android.os.Debug.waitForDebugger();

        try {
            Intent intent = new Intent(this, CipherConnectManagerService.class);
            this.startService(intent);
            this.bindService(intent, mSConnection, Context.BIND_AUTO_CREATE);
            
            if (CipherConnectSettingInfo._DEBUG)
            	Log.d(TAG, "onCreate(): start CipherConnectManagerService");
        } catch (Exception e) {
            Log.e(this.getResources().getString(R.string.ime_name),
                  "CipherConnectSettingActivity.ConnectStatus_bt_startService:",
                  e);
        }
    }

    private void init_ScannerService() {
        this.mCipherConnectManagerService.AddListener(new ICipherConnectManagerListener() {

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
            
            public void onGetLEDevice(final ICipherConnBTDevice device) {
            	
            }
        });
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
    	Log.d(TAG, "onStartInputView(): restarting= "+restarting);
    	
        super.onStartInputView(info, restarting);

        if (this.mCipherConnectManagerService.isConnected() == false) {
            String[] deviceNames = this.mCipherConnectManagerService
                                   .getBtDeviceNames();
            if (deviceNames != null && deviceNames.length > 0) {
                String device_name = CipherConnectSettingInfo
                                     .getLastDeviceName(this);
                try {
                    this.mCipherConnectManagerService.connect(device_name);
                } catch (Exception e) {
                    Log.e(this.getResources().getString(R.string.ime_name),
                          "CipherConnectService.onStartInputView:", e);
                }
            }
        }

        if (CipherConnectSettingInfo.isAutoConnect(this)) {
            if (!this.mCipherConnectManagerService.isAuotConnect())
                this.mCipherConnectManagerService.AuotConnect(true, CipherConnectSettingInfo.getLastDeviceName(this));
        }

        this.isOnStartInputView = true;
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        this.isOnStartInputView = false;
    }

    @Override
    public void onDestroy() {
        this.unbindService(this.mSConnection);

        if (!KeyboardUtil
                .isEnableingKeyboard(this, R.string.ime_service_name)) {
            if (this.mCipherConnectManagerService != null) {
                mCipherConnectManagerService.stopSelf();
            }
        }
        this.mCipherConnectManagerService = null;

        super.onDestroy();
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        // super.onKey(primaryCode, keyCodes);

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
            if (this.mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
                this.mInputView.setShifted(false);
            }

            StringBuffer sb = new StringBuffer();
            sb.append((char) primaryCode);
            ic.commitText(sb.toString(), 1);
            sb = null;
            break;
        }
    }

    public synchronized void sendBarcode(String barcode) {
    	//Log.d(TAG, "sendBarcode(): barcode= "+barcode);
    	
        if (this.isOnStartInputView) {
            if (barcode == null)
                return;
            if (barcode.length() == 0)
                return;

            char[] data = barcode.toCharArray();
            Log.e("sendBarcode", "sendBarcode("+barcode+")");
            
            this.waitEx(5);

            InputConnection ic = getCurrentInputConnection();
            
            //ic.commitText("\n", 1);
            for (char c : data) {
                /*
            	if (c == '\n') {
        	    	Log.e("sendBarcode", "Enter");

					this.waitEx(10);
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ENTER));
					this.waitEx(20);

                    continue;
                } else if (c == '\t') {
					this.waitEx(10);
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_TAB));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_TAB));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_TAB));
                    this.waitEx(20);

                    continue;
                }
                */
                this.waitEx(3);
                String s = String.valueOf(c);
    	    	Log.e("sendBarcode", "commitText("+s+")");
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

    @Override
    public void onBindInput() {
        Log.d("xxxx", "onBindInput");
        if (KeyboardUtil.isEnableingKeyboard(this, R.string.ime_service_name) == true) {
            CipherConnectNotification.resume_notify(this);
        }
        super.onBindInput();
    }

    @Override
    public void onUnbindInput() {
        Log.d("xxxx", "onUnbindInput");
        if (KeyboardUtil.isEnableingKeyboard(this, R.string.ime_service_name) == true) {
            if (KeyboardUtil.checkKeyboard(this) == false) {
                CipherConnectNotification.pause_notify(this);
            }
        }
        super.onUnbindInput();
    }

    /*
     * private boolean isEnableingKeyboard(){ InputMethodManager imm =
     * (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     * List<InputMethodInfo> list = imm.getEnabledInputMethodList();
     * if(list==null || list.size()==0) return false;
     *
     * for (InputMethodInfo imo : list) { String name = imo.getServiceName();
     * Log.d(this.getResources().getString(R.string.ime_name),
     * "InputMethodInfo.name="+name); if(name==null || name.length()==0)
     * continue;
     *
     * if(name.equals(CipherConnectKeyboardService.class.getName())){
     * Log.d(this.getResources().getString(R.string.ime_name),
     * "The CipherConnectKeyboard was enable."); return true; }
     *
     * }
     *
     * Log.d(this.getResources().getString(R.string.ime_name),
     * "The CipherConnectKeyboard was not enable."); return false; }
     */
}
