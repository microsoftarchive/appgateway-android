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

import android.text.TextUtils;

/**
 * Describes relevant attributes for the connection to the cloud.
 */
public class ConnectionTraits implements Serializable {

    /**
     * UID required for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Authentication token.
     */
    public String token;

    /**
     * Proxy agent to route requests.
     */
    public AgentEntity agent;

    /**
     * Session ID.
     */
    public String sessionID;

    /**
     * Error message if any.
     */
    public String error;

    /**
     * Default constructor.
     */
    public ConnectionTraits() {}
    
    /**
     * Instantiates class with error string.
     * 
     * @param err Error string.
     */
    public ConnectionTraits(String err) {
        error = err;
    }

    /**
     * Sets token.
     * 
     * @param newToken Token to assign.
     * 
     * @return Self.
     */
    public ConnectionTraits setToken(String newToken) {
        token = newToken;
        return this;
    }

    /**
     * Sets proxy agent.
     * 
     * @param newAgent Agent to assign
     * 
     * @return Self.
     */
    public ConnectionTraits setAgent(AgentEntity newAgent) {
        agent = newAgent;
        return this;
    }

    /**
     * Sets sesion ID.
     * 
     * @param newSession Sesion ID string.
     * 
     * @return Self.
     */
    public ConnectionTraits setSession(String newSession) {
        sessionID = newSession;
        return this;
    }

    /**
     * Sets error message.
     * 
     * @param error Error message.
     * 
     * @return Self.
     */
    public ConnectionTraits setError(String error) {
        this.error = error;
        return this;
    }

    /**
     * Identifies if there was an error during recent operation.
     * 
     * @return <code>True</code> if there was an error, <code>false</code> otherwise.
     */
    public boolean isError() {
        return !TextUtils.isEmpty(error);
    }
}