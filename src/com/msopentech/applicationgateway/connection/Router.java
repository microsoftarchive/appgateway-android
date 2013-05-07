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
package com.msopentech.applicationgateway.connection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.msopentech.applicationgateway.EnterpriseBrowserActivity;
import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.R;
import com.msopentech.applicationgateway.data.AgentEntity;
import com.msopentech.applicationgateway.data.ConnectionTraits;
import com.msopentech.applicationgateway.data.Credentials;
import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.utils.Utility;
import com.msopentech.applicationgateway.utils.XmlUtility;

/**
 * Implements helper class to perform authentication requests.
 */
public class Router {

    /**
     * Exception string used for error detection.
     */
    public static final String EXCEPTION_OCCURRED = "Exception occurred: ";

    /**
     * Response JSON key to get agent id.
     */
    public static String JSON_AGENT_ID_KEY = "agent_id";

    /**
     * Response JSON key to get agent display name.
     */
    public static String JSON_AGENT_DISPLAY_NAME_KEY = "display_name";

    /**
     * Response JSON key to get agents list.
     */
    public static String JSON_AGENTS_KEY = "agents";

    /**
     * Response JSON key to get session id.
     */
    public static String JSON_SESSION_ID_KEY = "session_id";

    /**
     * Token error string.
     */
    private static String ERROR_TOKEN = "Token error - %s";  

    /**
     * Session error string.
     */
    private static String ERROR_SESSION = "Session error - %s";    

    /**
     * Agent error string.
     */
    private static String ERROR_AGENT = "Agent error - %s"; 

    /**
     * Action to obtain token.<br/>
     * <b>Input arguments</b>: Object[]{ {@link Credentials} }<br/>
     * <b>Output arguments</b>: Object[]{ {@link String} token, {@link ConnectionTraits} };
     */
    public static final int ACTION_OBTAIN_TOKEN = 0;

    /**
     * Action to obtain agents list.<br/>
     * <b>Input arguments</b>: Object[]{ {@link ConnectionTraits} }<br/>
     * <b>Output arguments</b>: Object[]{ {@link JSONArray}, {@link ConnectionTraits} };
     */
    public static final int ACTION_OBTAIN_AGENTS = 1;

    /**
     * Action to obtain one agent.<br/>
     * <b>Input arguments</b>: Object[]{ {@link ConnectionTraits} }<br/>
     * <b>Output arguments</b>: Object[]{ {@link AgentEntity}, {@link ConnectionTraits} };
     */
    public static final int ACTION_OBTAIN_AGENT = 2;        

    /**
     * Action to obtain session ID.<br/>
     * <b>Input arguments</b>: Object[]{ {@link ConnectionTraits} }<br/>
     * <b>Output arguments</b>: Object[]{ {@link String} sessionID, {@link ConnectionTraits} };
     */
    public static final int ACTION_OBTAIN_SESSION = 3;

    /**
     * Action to obtain token.<br/>
     * <b>Input arguments</b>: Object[]{ {@link Credentials} }<br/>
     * <b>Output arguments</b>: Object[]{ {@link ConnectionTraits} };
     */
    public static final int ACTION_AUTHENTICATE = 4;    

    /**
     * Template string to be used while composing requests.
     */
    private static String requestTemplate = "<s:Envelope xmlns:s='http://www.w3.org/2003/05/soap-envelope' " +
            "xmlns:o='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd' " +
            "xmlns:p='http://schemas.xmlsoap.org/ws/2004/09/policy' " +
            "xmlns:u='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd' " +
            "xmlns:a='http://www.w3.org/2005/08/addressing' " +
            "xmlns:wssc='http://schemas.xmlsoap.org/ws/2005/02/sc' " +
            "xmlns:t='http://schemas.xmlsoap.org/ws/2005/02/trust'> " +
              "<s:Header> " + 
                "<o:Security> " +
                  "<u:Timestamp u:Id='_0'> " +
                    "<u:Created>#{created}</u:Created> " +
                    "<u:Expires>#{expires}</u:Expires> " +
                  "</u:Timestamp> " +
                  "<o:UsernameToken u:Id='uuid-588aa9a6-b538-49c4-9112-625ec501575d-8'> " +
                    "<o:Username>#{user}</o:Username> " +
                    "<o:Password>#{pass}</o:Password> " +
                  "</o:UsernameToken> " +
                "</o:Security> " +
              "</s:Header> " +
              "<s:Body> " +
                "<t:RequestSecurityToken> " +
                  "<p:AppliesTo> " +
                    "<a:EndpointReference> " +
                      "<a:Address>#{resource}</a:Address> " +
                    "</a:EndpointReference> " +
                  "</p:AppliesTo> " +
                  "<t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType> " +
                  "<p:PolicyReference URI='MCMBI'></p:PolicyReference> " +
                  "<o:LoginOptions>3</o:LoginOptions> " +
                 "</t:RequestSecurityToken> " +
               "</s:Body> " +
             "</s:Envelope>";

    /**
     * Performs <b>asynchronous</b> HTTP request and obtains requested data. If activity argument is not <code>null</code> listener methods
     * will be executed on UI thread that this activity is running on.
     * 
     * @param request Request type.
     * @param arguments Request arguments.
     * @param listener Operation listener.
     * @param activity Activity to get the thread the delegate will run on.
     *
     * @see #ACTION_OBTAIN_TOKEN 
     * @see #ACTION_OBTAIN_AGENTS
     * @see #ACTION_OBTAIN_AGENT 
     * @see #ACTION_OBTAIN_SESSION
     * @see #ACTION_AUTHENTICATE
     */
    public static void performRequest(int request, Object[] arguments, OnOperationExecutionListener listener, Activity activity) {
        new AgentsAsyncTask(request, listener, activity).execute(arguments);
    }
    
    /**
     * Performs HTTP POST request and obtains authentication token.
     * 
     * @param credentials Authentication credentials
     * 
     * @return {@link ConnectionTraits} with valid token OR with an error message if exception is caught or token retrieval failed or
     *         token is <code>null</code> or an empty string. Does NOT return <code>null</code>.
     */
    private static ConnectionTraits obtainToken(Credentials credentials) {
        ConnectionTraits connection = new ConnectionTraits();

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost("https://login.microsoftonline.com/extSTS.srf");

            request.addHeader("SOAPAction", "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue");
            request.addHeader("Content-Type", "application/soap+xml; charset=utf-8");

            Date now = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ");
            String nowAsString = df.format(now);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.SECOND, 10 * 60);
            Date expires = calendar.getTime();
            String expirationAsString = df.format(expires);

            String message = requestTemplate;

            message = message.replace("#{user}", credentials.getUsername());
            message = message.replace("#{pass}", credentials.getPassword());
            message = message.replace("#{created}", nowAsString);
            message = message.replace("#{expires}", expirationAsString);
            message = message.replace("#{resource}", "appgportal.cloudapp.net");

            StringEntity requestBody = new StringEntity(message, HTTP.UTF_8);
            requestBody.setContentType("text/xml");
            request.setEntity(requestBody);

            BasicHttpResponse response = null;
            response = (BasicHttpResponse) client.execute(request);

            HttpEntity entity = response.getEntity();
            InputStream inputStream = null;
            String token = null;

            inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer actualResponse = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                actualResponse.append(line);
                actualResponse.append('\r');
            }
            token = actualResponse.toString();

            String start = "<wst:RequestedSecurityToken>";

            int index = token.indexOf(start);

            if (-1 == index) {
                DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                InputStream errorInputStream = new ByteArrayInputStream(token.getBytes());
                Document currentDoc = documentBuilder.parse(errorInputStream);

                if (currentDoc == null) {
                    return null;
                }

                String errorReason = null;
                String errorExplained = null;
                Node rootNode = null;
                if ((rootNode = currentDoc.getFirstChild()) != null && /* <S:Envelope> */
                        (rootNode = XmlUtility.getChildNode(rootNode, "S:Body")) != null &&
                        (rootNode = XmlUtility.getChildNode(rootNode, "S:Fault")) != null) {
                    Node node = null;
                    if ((node = XmlUtility.getChildNode(rootNode, "S:Reason")) != null) {
                        errorReason = XmlUtility.getChildNodeValue(node, "S:Text");
                    }
                    if ((node = XmlUtility.getChildNode(rootNode, "S:Detail")) != null 
                            && (node = XmlUtility.getChildNode(node, "psf:error")) != null
                            && (node = XmlUtility.getChildNode(node, "psf:internalerror")) != null) {
                        errorExplained = XmlUtility.getChildNodeValue(node, "psf:text");
                    }
                }

                if (!TextUtils.isEmpty(errorReason) && !TextUtils.isEmpty(errorExplained)) {
                    logError(null, Router.class.getSimpleName() + ".obtainToken(): " + errorReason + " - " + errorExplained);
                    connection.setError(String.format(ERROR_TOKEN, errorReason + ": " + errorExplained));
                    return connection;
                }
            } else {
                token = token.substring(index);

                String end = "</wst:RequestedSecurityToken>";

                index = token.indexOf(end) + end.length();
                token = token.substring(0, index);

                if (!TextUtils.isEmpty(token)) {
                    connection.setToken(token);
                    return connection;
                }
            }
        } catch (final Exception e) {
            logError(e, Router.class.getSimpleName() + ".obtainToken() Failed");
            return connection.setError(String.format(ERROR_TOKEN, "Token retrieval failed with exception."));
        }
        return connection.setError(String.format(ERROR_TOKEN, "Token retrieval failed."));
    }

    /**
     * Performs HTTP GET request and retrieves proxy agents list. One agent is chosen from it and returned as a result.
     * 
     * @param traits Connection traits.
     * 
     * @return Agent entity. Returns <code>null</code> if exception is caught. Always provides error description if error occurs.
     */
    private static AgentEntity obtainAgent(ConnectionTraits traits) {
        try {
            JSONArray json = obtainAgents(traits);
            if(traits.isError()) return null;
            
            traits.agent = new AgentEntity();

            JSONObject item = null;
            String agentId = null;
            String agentDisplayName = null;
            boolean preferredAgentFound = false;
            AgentEntity agent = new AgentEntity();

            AgentEntity preferredAgent = AuthPreferences.loadPreferredAgent();

            for (int i = 0; i < json.length(); i++) {
                try {
                    item = json.getJSONObject(i);
                    agentId = item.getString(JSON_AGENT_ID_KEY);
                    agentDisplayName = item.getString(JSON_AGENT_DISPLAY_NAME_KEY);

                    if (preferredAgent != null && preferredAgent.getAgentId() != null && preferredAgent.getAgentId().contentEquals(agentId) && preferredAgent.getDisplayName() != null
                        && preferredAgent.getDisplayName().contentEquals(agentDisplayName)) 
                    	preferredAgentFound = true;

                    if (i == 0 || preferredAgentFound) {
                        agent = new AgentEntity();
                        agent.setAgentId(agentId);
                        agent.setDisplayName(agentDisplayName);
                        item = new JSONObject();
                        item.put(JSON_AGENT_ID_KEY, agentId);
                        agent.setAgentIdJSON(item.toString());

                        traits.setAgent(agent);

                        if (preferredAgentFound) {
                            if (!agent.getAgentId().isEmpty()) {
                                return agent;
                            } else {
                                traits.setError(String.format(ERROR_AGENT, "Connector ID is empty."));
                            }
                        }
                    }
                } catch (JSONException e) {
                    traits.setError(String.format(ERROR_AGENT, "Connectors list parsing failed."));
                    return null;
                }
            }
            if(agent == null || TextUtils.isEmpty(agent.getAgentId())) {
                traits.setError(String.format(ERROR_AGENT, "Connector ID is null or empty."));
            } else {
                return agent;
            }
        } catch (final Exception e) {
            logError(e, Router.class.getSimpleName() + ".obtainConnectors() Failed");
            traits.setError(String.format(ERROR_AGENT, "Connector retrieval failed."));
        }

        return null;
    }

    /**
     * Performs HTTP GET request and retrieves proxy agents list.
     * 
     * @param traits Connection traits.
     * 
     * @return {@link JSONArray} containing agents list. Returns <code>null</code> if exception is caught. Always provides error description if error occurs.
     */
    private static JSONArray obtainAgents(ConnectionTraits traits) {
        try {
            if (traits == null || TextUtils.isEmpty(traits.token)) {
                String errorText = String.format(ERROR_AGENT, "Traits argument is null or does not contain valid token.");
                if (traits != null) {
                    traits.setError(errorText);
                } else {
                    traits = new ConnectionTraits(errorText);
                }
                return null;                
            }

            HttpClient agentsClient = new DefaultHttpClient();
            HttpGet agentsRequest = new HttpGet(EnterpriseBrowserActivity.CLOUD_CONNECTION_HOST_PREFIX + "user/agents");

            agentsRequest.addHeader("X-Bhut-AuthN-Token", traits.token);

            BasicHttpResponse response = null;
            response = (BasicHttpResponse) agentsClient.execute(agentsRequest);

            HttpEntity responseEntity = response.getEntity();
            InputStream responseStream = null;
            JSONObject responseObject = null;
            JSONArray agentsArray = null;

            responseStream = responseEntity.getContent();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            StringBuffer actualResponse = new StringBuffer();
            while ((line = responseReader.readLine()) != null) {
                actualResponse.append(line);
                actualResponse.append('\r');
            }

            responseObject = new JSONObject(actualResponse.toString());
            agentsArray = responseObject.getJSONArray(JSON_AGENTS_KEY);

            if(agentsArray == null || agentsArray.length() <= 0) {
                traits.setError(String.format(ERROR_AGENT, "No connectors found."));
            } else {
                return agentsArray;
            }            
        } catch (final Exception e) {
            logError(e, Router.class.getSimpleName() + ".obtainConnectors() Failed");
            traits.setError(String.format(ERROR_AGENT, "Connectors retrieval failed."));
        }

        return null;
    }

    /**
     * Performs HTTP POST request and obtains session ID.
     * 
     * @param traits Connection traits.
     * @param agentIdJSON Selected proxy agent ID (in JSON string format) that will be used to route through all the requests.
     * 
     * @return Session ID. Returns <code>null</code> if exception is caught. Always provides error description if error occurs.
     */
    private static String obtainSession(ConnectionTraits traits, String agentIdJSON) {
        try {
            if (traits == null || TextUtils.isEmpty(traits.token)) {
                String errorText = String.format(ERROR_SESSION, "Traits argument is null or does not contain valid token.");
                if (traits != null) {
                    traits.setError(errorText);
                } else {
                    traits = new ConnectionTraits(errorText);
                }
                return null;
            }

            HttpClient sessionClient = new DefaultHttpClient();
            HttpPost sessionRequest = new HttpPost(EnterpriseBrowserActivity.CLOUD_CONNECTION_HOST_PREFIX + "user/session");

            sessionRequest.addHeader("x-bhut-authN-token", traits.token);

            StringEntity requestBody = null;
            requestBody = new StringEntity(agentIdJSON);

            requestBody.setContentType("application/json");
            sessionRequest.setEntity(requestBody);

            BasicHttpResponse response = null;
            response = (BasicHttpResponse) sessionClient.execute(sessionRequest);

            HttpEntity responseEntity = response.getEntity();
            InputStream responseStream = null;
            String result = null;

            responseStream = responseEntity.getContent();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            StringBuffer actualResponse = new StringBuffer();
            while ((line = responseReader.readLine()) != null) {
                actualResponse.append(line);
                actualResponse.append('\r');
            }
            result = actualResponse.toString();

            JSONObject session = null;
            session = new JSONObject(result);
            result = session.getString(JSON_SESSION_ID_KEY);
            
            if(TextUtils.isEmpty(result)) {
                traits.setError(String.format(ERROR_SESSION, "Session is null or empty."));
            } else {
                traits.setSession(result);
                return result;
            }
        } catch (final Exception e) {
            logError(e, Router.class.getSimpleName() + ".obtainSession() Failed");
            traits.setError(String.format(ERROR_SESSION, "Session retrieval failed."));
        }
        return null;
    }

    /**
     * Logs error to the provided string object.
     * 
     * @param e Exception.
     * @param message Message to add to a default one.
     * 
     * @return Composed error string.
     */
    private static void logError(Exception e, String message) {
        String error = EXCEPTION_OCCURRED + (message == null ? "" : message) + ": " + (e == null ? "" : e.toString());
        Log.e(Router.class.getSimpleName(), error);
    }

    /**
     * Implements asynchronous calls to authentication operations.
     */
    private static class AgentsAsyncTask extends AsyncTask<Object, Void, Object> {

        /**
         * Action code, performed by the task.
         */
        private int mOperation;

        /**
         * Operation listener delegate.
         */
        private OnOperationExecutionListener mListener; 

        /**
         * Activity to reach the thread to run the delegate on.
         */
        private Activity mActivity;
        
        /**
         * Default constructor.
         * 
         * @param operation Action type to be processed.
         * @param listener Operation listener.
         * 
         * @see #ACTION_OBTAIN_AGENTS
         * @see #ACTION_OBTAIN_AGENT
         * @see #ACTION_OBTAIN_SESSION
         * @see #ACTION_OBTAIN_TOKEN
         */
        AgentsAsyncTask(int operation, OnOperationExecutionListener listener, Activity activity) {
            super();
            mOperation = operation;
            mListener = listener;
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mActivity == null) {
            mListener.onBeforeExecution(mOperation);
            } else {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onBeforeExecution(mOperation);
                    }
                });
            }
        }

        @Override
        protected Object doInBackground(Object... arguments) {
            Object result = null;
            try {
                switch (mOperation) {
                    case ACTION_OBTAIN_TOKEN: {
                        Credentials credentials = (Credentials)arguments[0];
                        result = Router.obtainToken(credentials);
                        break;
                    }                    
                    case ACTION_OBTAIN_AGENTS: {
                        ConnectionTraits traits = (ConnectionTraits)arguments[0];
                        result = new Object[]{ Router.obtainAgents(traits), traits};
                        break;
                    }
                    case ACTION_OBTAIN_AGENT: {
                        ConnectionTraits traits = (ConnectionTraits)arguments[0];
                        result = new Object[]{ Router.obtainAgent(traits), traits};
                        break;
                    }
                    case ACTION_OBTAIN_SESSION: {
                        ConnectionTraits traits = (ConnectionTraits)arguments[0];
                        result = new Object[]{ Router.obtainSession(traits, traits.agent.getAgentIdJSON()), traits};
                        break;
                    }
                    case ACTION_AUTHENTICATE: {
                        try {
                            Credentials credentials = (Credentials) arguments[0];

                            ConnectionTraits traits = Router.obtainToken(credentials);
                            if (traits.isError()) return new Object[]{traits};

                            AgentEntity agent = Router.obtainAgent(traits);
                            if (traits.isError()) return new Object[]{traits};

                            String session = Router.obtainSession(traits, agent.getAgentIdJSON());
                            if (traits.isError()) return new Object[]{traits};

                            traits.sessionID = session;
                            traits.agent = agent;

                            AuthPreferences.storeCredentials(credentials);
                            result = new Object[]{traits};
                        } catch (Exception ex) {}
                        break;
                    }                    
                }
                return result;
            } catch (Exception e) {
                Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Object result) {
            try {
                if(mActivity == null) { 
                mListener.onExecutionComplete(mOperation, (Object[])result);
                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onExecutionComplete(mOperation, (Object[])result);
                        }
                    });
                }                
            } catch (final Exception e) {
                Utility.showAlertDialog(Router.class.getSimpleName() + ".onPostExecute(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
            }
        }
    }    
}
