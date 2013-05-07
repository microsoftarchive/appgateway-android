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

package com.msopentech.applicationgateway;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.msopentech.applicationgateway.connection.OnOperationExecutionListener;
import com.msopentech.applicationgateway.connection.Router;
import com.msopentech.applicationgateway.data.ConnectionTraits;
import com.msopentech.applicationgateway.data.Credentials;
import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements sign-in authentication: receiving token, agents and session.
 */
public class SignInActivity extends Activity implements OnOperationExecutionListener {

    /**
     * Authentication alert key for intent extras.
     */
    public static final String ALERT_MESSAGE_KEY = "sign_in_alert";

    /**
     * Key to indicate whether user is prompted to enter credentials (if true) or only prompted if stored credentials fail (if false).
     */
    public static final String UI_PROMPT = "UI_PROMPT";    
    
    /**
     * Indicates whether the progress bar must or must not be shown when the screen changes its orientation.
     */
    private static boolean mIsWorkInProgress = false;
    
    @SuppressLint("StringFormatMatches")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	try{
	        super.onCreate(savedInstanceState);

            final boolean uiPrompt = Boolean.parseBoolean(getIntent().getStringExtra(UI_PROMPT));

            Credentials loadedCredentials = AuthPreferences.loadCredentials();
	        setContentView(R.layout.sign_in);
	        
            showWorkInProgress(false);

	        setAlertMessage(getIntent().getStringExtra(ALERT_MESSAGE_KEY));

	        TextView cancelButton = (TextView)findViewById(R.id.sign_in_cancel);
	        TextView doneButton = (TextView)findViewById(R.id.sign_in_done);
	
	        EditText username = (EditText)findViewById(R.id.sign_in_user_name);
	        EditText password = (EditText)findViewById(R.id.sign_in_password);
	
	        if (loadedCredentials.getUsername() != null && loadedCredentials.getPassword() != null) {
	            username.setText(loadedCredentials.getUsername());
	            password.setText(loadedCredentials.getPassword());
	        }
	
	        View.OnClickListener listener = new View.OnClickListener() {
	            public void onClick(View view) {
	            	switch(view.getId()) {
	            		case R.id.sign_in_cancel: {
	                        Intent resultIntent = getIntent();
                            setResult(RESULT_CANCELED, resultIntent);
	                        finish();
	                        break;
	            		}
	            		case R.id.sign_in_done: {
	                        EditText username = (EditText)findViewById(R.id.sign_in_user_name);
	                        EditText password = (EditText)findViewById(R.id.sign_in_password);
	
                            Object[] tokenTaskArgs = new Object[2];
                            tokenTaskArgs[0] = new Credentials(username.getText().toString(), password.getText().toString());
                            tokenTaskArgs[1] = uiPrompt;

                            Router.performRequest(Router.ACTION_AUTHENTICATE, tokenTaskArgs, SignInActivity.this, SignInActivity.this);
	                        break;
	            		}
	            	}
	            }
	        };
	        
	        cancelButton.setOnClickListener(listener);
	        doneButton.setOnClickListener(listener);
	        
            if (!uiPrompt && loadedCredentials.getUsername() != null && loadedCredentials.getPassword() != null) {
                Router.performRequest(Router.ACTION_AUTHENTICATE, new Object[] {loadedCredentials, uiPrompt}, SignInActivity.this, SignInActivity.this);
            }

            TextView applicationHelp = (TextView) findViewById(R.id.sign_in_app_help);
            applicationHelp.setText(Html.fromHtml(getResources().getString(R.string.sign_in_app_help, getResources().getString(R.string.sign_in_app_help_url))));
            applicationHelp.setMovementMethod(LinkMovementMethod.getInstance());
            
            if (AuthPreferences.loadFirstRun()) {
                Utility.showMessageDialog(Html.fromHtml(getResources().getString(R.string.first_run_message, getResources().getString(R.string.sign_in_app_help_url))), getResources().getString(R.string.message_dialog_title), SignInActivity.this);
                AuthPreferences.storeFirstRun(false);
            }
    	} catch(final Exception e) {
    		Utility.showAlertDialog(SignInActivity.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), SignInActivity.this);
    	}
    }

    @Override
    protected void onResume() {
        try {
            showWorkInProgress(mIsWorkInProgress);
        } catch (final Exception e) {
            Utility.showAlertDialog(SignInActivity.class.getSimpleName() + ".onResume(): Failed. " + e.toString(), SignInActivity.this);
        }        
        super.onResume();
    }

    /**
     * Toggles between showing either progress indicator or a content pane with actual data.
     * 
     * @param isWorkInProgress Progress status.
     */
    private void showWorkInProgress(boolean isWorkInProgress) {
        try {
            View progressIndicator = findViewById(R.id.sign_in_progress);
            View contentPane = findViewById(R.id.sign_in_content);
            View helpText = findViewById(R.id.sign_in_app_help);
            progressIndicator.setVisibility(isWorkInProgress ? View.VISIBLE : View.GONE);
            contentPane.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
            helpText.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
        } catch (final Exception e) {
            Utility.showAlertDialog(SignInActivity.class.getSimpleName() + ".showWorkInProgress(): Failed. " + e.toString(), SignInActivity.this);
        }
    }

    /**
     * Sets alert message in case something goes wrong during authentication process.
     * 
     * @param alertMessage Message to display.
     */
    private void setAlertMessage(CharSequence alertMessage) {
        try {
            if(alertMessage == null) { 
                return;
            }
            TextView alertMessageView = (TextView)findViewById(R.id.sign_in_alert_message);
            alertMessageView.setVisibility(View.VISIBLE);
            alertMessageView.setText(alertMessage, TextView.BufferType.SPANNABLE);
            alertMessageView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (final Exception e) {
            Utility.showAlertDialog(SignInActivity.class.getSimpleName() + ".setAlertMessage(): Failed. " + e.toString(), SignInActivity.this);
        }
    }    

    @Override
    public void onBeforeExecution(int operation) {
            mIsWorkInProgress = true;
            showWorkInProgress(mIsWorkInProgress);
        }

    @Override
    public void onExecutionComplete(int operation, Object result[]) {
        try {
            mIsWorkInProgress = false;
            ConnectionTraits traits = (ConnectionTraits)result[0];

            if (result[0] == null || result.length <= 0 || result[0] == null) {
                String error = getResources().getString(R.string.sign_in_attempt_failed);
                setAlertMessage(error);
                showWorkInProgress(mIsWorkInProgress);
                EnterpriseBrowserActivity.mTraits.error = error;
            } else {
                if (!TextUtils.isEmpty(traits.error)) {
                    if (traits.error.contains("No connectors found.")) { // Special case no connector found.  Router.java does not yet use resource file.
                        setAlertMessage(Html.fromHtml(getResources().getString(R.string.sign_in_no_connector, getResources().getString(R.string.sign_in_app_help_url))));
                    } else {
                        setAlertMessage(traits.error);
                    }
                    showWorkInProgress(mIsWorkInProgress);
                    EnterpriseBrowserActivity.mTraits = traits;
                } else {
                    Intent resultIntent = getIntent().putExtra(EnterpriseBrowserActivity.EXTRAS_TRAITS_KEY, traits);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        } catch (final Exception e) {
                Utility.showAlertDialog(SignInActivity.class.getSimpleName() + ".onExecutionComplete(): Failed. " + e.toString(), SignInActivity.this);
        }
    }
}