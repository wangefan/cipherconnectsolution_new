package com.cipherlab.cipherconnectpro2;

import com.cipherlab.help.CipherLog;

import android.content.Context;
import android.os.PowerManager;

public class CipherConnectWakeLock {

	// miller,2012/8/27 for suspend screen backlight
	private static PowerManager pm = null;
	private static PowerManager.WakeLock mWakeLock = null;

	public static void initial(Context c) {
		if (pm == null) {
			pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
		}
	}

	public static void enable() {
		if (mWakeLock == null) {
			CipherLog.d("Miller", "Suspend Backlight");
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
			mWakeLock.acquire();
		}
	}

	public static void disable() {
		if (mWakeLock != null && mWakeLock.isHeld()) {
			CipherLog.d("Miller", "Resume Backlight");
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
