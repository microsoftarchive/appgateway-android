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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements Advanced Router Settings ability.
 */
public class AdvancedRouterSettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.advanced_router_settings);

	        TextView cancelButton = (TextView)findViewById(R.id.advanced_router_settings_cancel);
	        TextView doneButton = (TextView)findViewById(R.id.advanced_router_settings_done);
	
	        String routerURL = AuthPreferences.loadRouterURL();
	        
	        final EditText url = (EditText)findViewById(R.id.advanced_router_settings_router_url);
	        url.setText(routerURL);

            Boolean preferredUseSmartBrowser = AuthPreferences.loadUseSmartBrowser();
            ToggleButton smartBrowserButton = (ToggleButton)findViewById(R.id.smart_browser_button);
            if (preferredUseSmartBrowser) {
                smartBrowserButton.setText("ON");
            }
	        
	        View.OnClickListener listener = new View.OnClickListener() {
	            public void onClick(View view) {
	            	switch(view.getId()) {
	            		case R.id.advanced_router_settings_cancel: {
	                        Intent resultIntent = getIntent();
	                        setResult(RESULT_OK, resultIntent);
	                        finish();
	                        break;
	            		}
	            		case R.id.advanced_router_settings_done: {
                            String newRouterURL = url.getText().toString();

                            if (!newRouterURL.endsWith("/")) {
                            	newRouterURL = newRouterURL + "/";
                            }

                            if (!newRouterURL.startsWith(EnterpriseBrowserActivity.HTTPS_PREFIX_ATTRIBUTE) && !newRouterURL.startsWith(EnterpriseBrowserActivity.HTTP_PREFIX_ATTRIBUTE)) {
                            	newRouterURL = EnterpriseBrowserActivity.HTTPS_PREFIX_ATTRIBUTE + newRouterURL;
                            }
                            
                            url.setText(newRouterURL);
	                        
                            String temp = null;
                            
                            ToggleButton smartBrowserButton = (ToggleButton)findViewById(R.id.smart_browser_button);
                            temp = smartBrowserButton.getText().toString();
                            Boolean smartBrowserOn = false;
                            
                            if (temp.contentEquals("ON"))
                            	smartBrowserOn = true;

                            ToggleButton clearCookiesButton = (ToggleButton)findViewById(R.id.clear_cookies_button);
                            temp = clearCookiesButton.getText().toString();
                            Boolean clearCookiesOn = false;
                            
                            if (temp.contentEquals("ON"))
                            	clearCookiesOn = true;
                            
                            Intent resultIntent = getIntent();
                            resultIntent.putExtra(EnterpriseBrowserActivity.CLOUD_CONNECTION_HOST_PREFIX, newRouterURL);
                            resultIntent.putExtra(EnterpriseBrowserActivity.EXTRAS_SMART_BROWSER_ON, smartBrowserOn);
                            resultIntent.putExtra(EnterpriseBrowserActivity.EXTRAS_CLEAR_COOKIES_ON, clearCookiesOn);
                            setResult(RESULT_OK, resultIntent);
                            finish();	                        
                            break;
                        }
                    }
                }
            };
        
            cancelButton.setOnClickListener(listener);
            doneButton.setOnClickListener(listener);        
        } catch(final Exception e) {
            Utility.showAlertDialog(AdvancedRouterSettingsActivity.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), AdvancedRouterSettingsActivity.this);
        }
    }
}