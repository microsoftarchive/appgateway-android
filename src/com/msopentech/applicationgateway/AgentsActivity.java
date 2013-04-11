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

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.msopentech.applicationgateway.connection.OnOperationExecutionListener;
import com.msopentech.applicationgateway.connection.Router;
import com.msopentech.applicationgateway.data.AgentEntity;
import com.msopentech.applicationgateway.data.ConnectionTraits;
import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Allows the user to choose an agent.
 */
public class AgentsActivity extends Activity implements OnOperationExecutionListener {
    /**
     * Notifies the user of the agent's being used.
     */
    private static final String IN_USE_MARK = "[CURRENTLY IN USE] ";

    /**
     * A list view that contains the names of the agents.
     */
    private ListView mAgentsListView = null;

    /**
     * Contains agents' data.
     */
    private Vector<AgentEntity> mAgentsDataStorage = new Vector<AgentEntity>(0);

    /**
     * Indicates whether the progress bar must or must not be shown when the screen changes its orientation.
     */
    private static boolean mIsWorkInProgress = false;    

        @Override
    public void onBeforeExecution(int operation) {
            showWorkInProgress(true);
        }

        @Override
    public void onExecutionComplete(int operation, Object[] result) {
        try {
                mIsWorkInProgress = false;
            if(result == null) {
                    AgentsActivity.this.runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                Utility.showToastNotification(getResources().getString(R.string.agents_obtain_failed));
                return;
            }
                    });
                } else {
                JSONArray rawAgentsStorage = (JSONArray) result[0];
                ConnectionTraits traits = (ConnectionTraits) result[1];

            for (int i = 0; i < rawAgentsStorage.length(); i++) {
                try {
                    JSONObject item = rawAgentsStorage.getJSONObject(i);
                    String agent_name = item.getString(Router.JSON_AGENT_DISPLAY_NAME_KEY);
                    String agent_id = item.getString(Router.JSON_AGENT_ID_KEY);

                        if (traits.agent.getAgentId().contentEquals(agent_id)) {
                        agent_name = IN_USE_MARK + agent_name;
                    }

                    mAgentsDataStorage.add(new AgentEntity(agent_id, agent_name));
                } catch (JSONException e) {
                    continue;
                }
            }

            int agentsNumber = mAgentsDataStorage.size();
            if (agentsNumber == 0) {
                Utility.showToastNotification("There are no agents available.");
                return;
            }

            String agentNames[] = new String[agentsNumber];
            for (int i = 0; i < agentsNumber; i++) {
                agentNames[i] = mAgentsDataStorage.elementAt(i).getDisplayName();
            }

            RelativeLayout agentsHeader = (RelativeLayout) getLayoutInflater().inflate(R.layout.list_header, null);
            ((TextView) agentsHeader.findViewById(R.id.list_header)).setText(R.string.agents_list_header);
            mAgentsListView.addHeaderView(agentsHeader, agentNames, false);
                    ArrayAdapter<String> list1 = new ArrayAdapter<String>(AgentsActivity.this, android.R.layout.simple_list_item_1, agentNames);

            mAgentsListView.setAdapter(list1);

                    showWorkInProgress(false);
                }

                showWorkInProgress(mIsWorkInProgress);
            } catch (final Exception e) {
                Utility.showAlertDialog(AgentsActivity.class.getSimpleName() + ".onPostExecute(): Failed. " + e.toString(), AgentsActivity.this);
            }
        }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.agent_list);

            TextView cancelButton = (TextView) findViewById(R.id.agent_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        final Intent resultIntent = getIntent();
                        setResult(RESULT_CANCELED, resultIntent);
                        finish();                        
                    } catch (final Exception e) {
                        Utility.showAlertDialog(AgentsActivity.class.getSimpleName() + "cancelButton onClick listener failed. " + e.toString(), AgentsActivity.this);
                    }
                }
            });

            final ConnectionTraits traits = (ConnectionTraits) getIntent().getSerializableExtra(EnterpriseBrowserActivity.EXTRAS_TRAITS_KEY);

            Router.performRequest(Router.ACTION_OBTAIN_AGENTS, new Object[] { traits }, AgentsActivity.this, AgentsActivity.this);

            mAgentsListView = (ListView) AgentsActivity.this.findViewById(R.id.agent_agents_list);
            mAgentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    try {
                        String selectedAgentName = mAgentsListView.getItemAtPosition(position).toString();
                        final Intent resultIntent = getIntent();
    
                        if (!selectedAgentName.startsWith(IN_USE_MARK)) {
                            ConnectionTraits resultTraits = new ConnectionTraits().setToken(traits.token).setAgent(mAgentsDataStorage.elementAt(position - 1));

                            Router.performRequest(Router.ACTION_OBTAIN_SESSION, new Object[] { resultTraits }, new OnOperationExecutionListener() {
                                @Override
                                public void onExecutionComplete(int operation, Object[] result) {
                                    final ConnectionTraits traits = (ConnectionTraits) result[1];
                                    if (traits.isError()) {
                                        AgentsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Utility.showToastNotification(traits.error);
                                                return;
                                            }
                                        });
                                        setResult(RESULT_CANCELED, resultIntent);
                                        finish();
                                    }
                                    AuthPreferences.storePreferredAgent(traits.agent);
                                    resultIntent.putExtra(EnterpriseBrowserActivity.EXTRAS_TRAITS_KEY, traits);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }

                                @Override
                                public void onBeforeExecution(int operation) {}
                            }, AgentsActivity.this);
                        } else {
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    } catch (Exception e) {
                        Utility.showAlertDialog(e.toString(), AgentsActivity.this);
                        return;
                    }
                }
            });
        } catch (Exception e) {
            Utility.showAlertDialog(e.toString(), this);
            return;
        }
    }

    /**
     * Toggles between showing either progress indicator or a content pane with actual data.
     * 
     * @param isWorkInProgress Progress status.
     */
    private void showWorkInProgress(boolean isWorkInProgress) {
        try {
            View progressIndicator = findViewById(R.id.agent_progress);
            View contentPane = findViewById(R.id.agent_agents_list);
            View contentDescription = findViewById(R.id.agent_description);
            View button = findViewById(R.id.agent_buttons_block);
            progressIndicator.setVisibility(isWorkInProgress ? View.VISIBLE : View.GONE);
            contentPane.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
            contentDescription.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
            button.setVisibility(isWorkInProgress ? View.GONE : View.VISIBLE);
        } catch (final Exception e) {
            Utility.showAlertDialog(AgentsActivity.class.getSimpleName() + ".showWorkInProgress(): Failed. " + e.toString(), AgentsActivity.this);
        }
    }
}
