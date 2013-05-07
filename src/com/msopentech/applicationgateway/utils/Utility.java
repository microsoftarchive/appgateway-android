/*
 *  Copyright (c) Microsoft Open Technologies
 *  All rights reserved. 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 *  THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT 
 *  LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, 
 *  MERCHANTABLITY OR NON-INFRINGEMENT. 
 *  See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */
package com.msopentech.applicationgateway.utils;

import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.R;
import com.msopentech.applicationgateway.connection.Router;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Implements common utility methods.
 */
public class Utility {

    /**
     * Shows alert dialog with provided message.
     * 
     * @param error Error message to be displayed.
     * @param context Application context.
     */
    public static void showAlertDialog(String error, Context context) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(ApplicationGateway.getAppContext().getResources().getString(R.string.alert_dialog_title));

            if (!error.contains(Router.EXCEPTION_OCCURRED)) {
                error = Router.EXCEPTION_OCCURRED + error;
            }

            builder.setMessage(error);
            builder.setPositiveButton(ApplicationGateway.getAppContext().getResources().getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (final Exception e) {
            Log.d(Utility.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }
    }

    /**
     * Shows initial message.
     * 
     * @param message Spanned message to be displayed.
     * @param context Application context.
     */
    public static void showMessageDialog(Spanned message, String title, Context context) {
        try {
            TextView messageTextView = new TextView(context);
            messageTextView.setText(message);
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            messageTextView.setPadding(20, 20, 20, 20);
        
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setView(messageTextView);
            builder.setPositiveButton(ApplicationGateway.getAppContext().getResources().getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (final Exception e) {
            Log.d(Utility.class.getSimpleName(), "showAlertDialog(): Failed.", e);
        }
    }
        
    /**
     * Shows a toast notification.
     * 
     * @param text Message to be displayed.
     */
    public static void showToastNotification(String text) {
        try {
            Toast.makeText(ApplicationGateway.getAppContext(), text, Toast.LENGTH_LONG).show();
        } catch (final Exception e) {
            Log.d(Utility.class.getSimpleName(), "showToastNotification(): Failed.", e);
        }
    }
}
