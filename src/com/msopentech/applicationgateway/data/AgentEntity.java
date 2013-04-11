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
package com.msopentech.applicationgateway.data;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.msopentech.applicationgateway.utils.Utility;

/**
 * Represents an agent with its id and name.
 */
public class AgentEntity implements Serializable{

    /**
     * UID required for serialization.
     */
    private static final long serialVersionUID = 12L;

    /**
     * Agent ID JSON item key.
     */
    private static final String AGENT_ID_KEY = "agent_id";

    /**
     * Stored agent id.
     */
    private String mAgentId;

    /**
     * Stored agent display name.
     */
    private String mDisplayName;

    /**
     * Default constructor.
     */
    public AgentEntity() {}

    /**
     * Class constructor.
     * 
     * @param agentId Agent ID.
     * @param displayName Agent display name.
     */
    public AgentEntity(String agentId, String displayName) {
        mAgentId = agentId;
        mDisplayName = displayName;
    }

    /**
     * Returns agent ID.
     * 
     * @return Stored agent id.
     */
    public String getAgentId() {
        return mAgentId;
    }

    /**
     * Returns Agent display name.
     * 
     * @return Stored display name.
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Returns JSON object String that wraps agent ID with format:
     * "{ {@link #AGENT_ID_KEY} : [agent_id] }"
     * 
     * @return Agents Id JSON String value.
     */
    public String getAgentIdJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(AGENT_ID_KEY, mAgentId);
        } catch (JSONException e) {
            Utility.showToastNotification(e.toString());
        }
        return json.toString();
    }    
    
    /**
     * Sets id of the agent.
     * 
     * @param agentId Agents Id value.
     */
    public void setAgentId(String agentId) {
        this.mAgentId = agentId;
    }

    /**
     * Sets JSON for agent_id.
     * 
     * @param displayName the Agents display name.
     */
    public void setAgentIdJSON(String agentIdJSON) {
        try {
            JSONObject agentJSON = new JSONObject(agentIdJSON);
            if (agentJSON != null) {
                String agentID = agentJSON.getString(AGENT_ID_KEY);
                if (!TextUtils.isEmpty(agentID)) {
                    mAgentId = agentID;
                }
            }
        } catch (JSONException e) {
            Utility.showToastNotification(e.toString());
        }
    }

    /**
     * Sets display name of the agent.
     * 
     * @param displayName the Agents display name.
     */
    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }    
}
