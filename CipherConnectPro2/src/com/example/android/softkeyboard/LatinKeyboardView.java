/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.android.softkeyboard;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;

import com.cipherlab.help.CipherLog;

public class LatinKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    private static final String TAG = "LatinKeyboardView";

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);        
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);        
    }
    
    @Override
    public boolean setShifted(boolean shifted) 
    {
    	Log.d(TAG, "setShifted(shifted="+shifted+")");
    	return super.setShifted(shifted);
    }
    
    @Override
    public boolean isShifted() 
    {
    	boolean b = super.isShifted();
    	Log.d(TAG, "isShifted = " + b);
    	return b;
    }

    @Override
    protected boolean onLongPress(Key key) {
    	CipherLog.d("LatinKeyboardView", "onLongPress()");
    	if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else {
            return super.onLongPress(key);
        	//return onLongPressEx(key);
        }
    }
    

    
//    private boolean onLongPressEx(Key key) 
//    {
//    	Context context = this.getContext();
//    	View custom = LayoutInflater.from(context)
//    		    .inflate(R.xml.qwerty_popup_template, new FrameLayout(context));
//		PopupWindow popup = new PopupWindow(context);
//		popup.setContentView(custom);
//    	
//		popup.setWidth(this.getWidth());
//	    popup.setHeight(this.getHeight());
//	    popup.showAtLocation(custom, Gravity.NO_GRAVITY, (int)this.getX(), (int)this.getY());
//		
//	    return true;
//    	PopupWindow mPopupKeyboard = new PopupWindow(this.getContext());            
//    	mPopupKeyboard.setBackgroundDrawable(null); 
//    	
//    	if(mPopupKeyboard != null)
//    	{
//    	    this.dismissPopupKeyboard();
//    	    View mMiniKeyboardContainer = null;
//    	    KeyboardView mMiniKeyboard = null;
//    	    View closeButton = null;   
//    	    
//    	    mMiniKeyboardContainer = LayoutInflater.from(this.getContext()).inflate(R.xml.qwerty_popup_template, null);
//    	    mMiniKeyboard = (KeyboardView) mMiniKeyboardContainer.findViewById(R.id.popup_keyboardView);
//    	    closeButton = mMiniKeyboardContainer.findViewById(R.id.closeButton);
//    	    if (closeButton != null) 
//    	    {
//    	        closeButton.setOnClickListener(new OnClickListener()            
//    	        {
//    	            @Override
//    	            public void onClick(View arg0) 
//    	            {
//    	                mPopupKeyboard.dismiss();
//    	        });
//    	    }
    	    //mMiniKeyboard.setOnKeyboardActionListener(null);
//
//    	    String resourcestring = "abcdefghi";
//    	    mMiniKeyboard.setKeyboard(new Keyboard(this.getBaseContext(), R.xml.kbd_popup_template, alternates, 3, 0));
//    	    mMiniKeyboard.setPopupParent(mCandidateView);           
//    	    mPopupKeyboard.setContentView(mMiniKeyboardContainer);
//    	    mPopupKeyboard.setWidth(LayoutParams.WRAP_CONTENT);
//    	    mPopupKeyboard.setHeight(LayoutParams.WRAP_CONTENT);
//    	    mPopupKeyboard.showAtLocation(mCandidateView, Gravity.TOP, 0, 0);
    	//}   
    	
    	//return false;
//    }
}
