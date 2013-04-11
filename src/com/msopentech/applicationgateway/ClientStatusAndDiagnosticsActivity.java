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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements Advanced Router Settings ability.
 */
public class ClientStatusAndDiagnosticsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.client_status_and_diagnostics);

            TextView accountTextView = (TextView) findViewById(R.id.client_status_orgid_account);
            TextView routerTextView = (TextView) findViewById(R.id.client_status_router_url);
            TextView agentTextView = (TextView) findViewById(R.id.client_status_agent_name);
            TextView agentIDTextView = (TextView) findViewById(R.id.client_status_agentid);
            TextView sessionIDTextView = (TextView) findViewById(R.id.client_status_sessionid);

            ImageButton accountStatus = (ImageButton) findViewById(R.id.client_status_orgid_status);
            ImageButton routerStatus = (ImageButton) findViewById(R.id.client_status_router_status);
            ImageButton agentStatus = (ImageButton) findViewById(R.id.client_status_agent_status);
            ImageButton agentIDStatus = (ImageButton) findViewById(R.id.client_status_agentid_status);
            ImageButton sessionIDStatus = (ImageButton) findViewById(R.id.client_status_sessionid_status);

            if (!(EnterpriseBrowserActivity.mTraits == null || TextUtils.isEmpty(EnterpriseBrowserActivity.mTraits.token))) {
                accountTextView.setText(AuthPreferences.loadCredentials().getUsername());
                accountStatus.setImageResource(R.drawable.connection_green);
            } else {
                accountTextView.setText(null);
                accountStatus.setImageResource(R.drawable.connection_red);
            }
            
            routerTextView.setText(EnterpriseBrowserActivity.CLOUD_CONNECTION_HOST_PREFIX);
            
            if (!(EnterpriseBrowserActivity.mTraits == null || EnterpriseBrowserActivity.mTraits.agent == null)) {
                agentTextView.setText(EnterpriseBrowserActivity.mTraits.agent.getDisplayName());
                agentIDTextView.setText(EnterpriseBrowserActivity.mTraits.agent.getAgentId());
            } else {
                agentTextView.setText(null);
            	agentIDTextView.setText(null);
            }
            
            if (!(EnterpriseBrowserActivity.mTraits == null)) {
            	sessionIDTextView.setText(EnterpriseBrowserActivity.mTraits.sessionID);
            } else {
            	sessionIDTextView.setText(null);
            }

            // Need to make more granular, but for now if we have a router we can obtain an agent and agent id, as well as a session id
            // based on the logic of the SignInActivity.
            if (!(EnterpriseBrowserActivity.mTraits == null || TextUtils.isEmpty(EnterpriseBrowserActivity.mTraits.sessionID))) {
                routerStatus.setImageResource(R.drawable.connection_green);
                agentStatus.setImageResource(R.drawable.connection_green);
                agentIDStatus.setImageResource(R.drawable.connection_green);
                sessionIDStatus.setImageResource(R.drawable.connection_green);
            } else {
                routerStatus.setImageResource(R.drawable.connection_red);
                agentStatus.setImageResource(R.drawable.connection_red);
                agentIDStatus.setImageResource(R.drawable.connection_red);
                sessionIDStatus.setImageResource(R.drawable.connection_red);
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(ClientStatusAndDiagnosticsActivity.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), ClientStatusAndDiagnosticsActivity.this);
        }
    }

//    public void onBrowseRouterSystemPage(View view) {
//        Intent resultIntent = getIntent();
//        resultIntent.putExtra(EnterpriseBrowserActivity.EXTRAS_BROWSE_TO_ROUTER_SYSTEM_PAGE_KEY, EnterpriseBrowserActivity.EXTRAS_BROWSE_TO_ROUTER_SYSTEM_PAGE_KEY);
//        setResult(RESULT_OK, resultIntent);
//        finish();
//    }

    public void onOKClick(View view) {
        Intent resultIntent = getIntent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}