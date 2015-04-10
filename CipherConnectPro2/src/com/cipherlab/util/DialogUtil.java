package com.cipherlab.util;

import android.app.AlertDialog.Builder;
import android.content.Context;

import com.cipherlab.cipherconnectpro2.R;

public class DialogUtil {
    public static Builder newAlertDialog(Context context,int r_title,int r_message) {
        Builder builder = new Builder(context);
        builder.setTitle(r_title);
        builder.setMessage(r_message);
        builder.setIcon(R.drawable.alert_dialog_icon);
        return builder;
    }
}
