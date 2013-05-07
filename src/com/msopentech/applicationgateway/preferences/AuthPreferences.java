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
package com.msopentech.applicationgateway.preferences;

import com.msopentech.applicationgateway.EnterpriseBrowserActivity;
import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.data.AgentEntity;
import com.msopentech.applicationgateway.data.Credentials;
import com.msopentech.applicationgateway.utils.Utility;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Implements authentication preferences.
 */
public class AuthPreferences {

    /**
     * Preference key to get first run.
     */
    private static String FIRST_RUN = "first_run";	
	
    /**
     * Preference key to get user name.
     */
    private static String ROUTER_URL = "router_url";
    
    /**
     * Preference key to get user name.
     */
    private static String USERNAME_KEY = "username";

    /**
     * Preference key to get user password.
     */    
    private static String PASSWORD_KEY = "password";

    /**
     * Preference key to get agent id.
     */
    private static String AGENT_ID_KEY = "agent_id";

    /**
     * Preference key to get agent display name.
     */	
    private static String AGENT_DISPLAY_NAME_KEY = "display_name";

    /**
     * Preference key to get router url.
     */	
    private static String ROUTER_URL_KEY = "router_url";
    
    /**
     * Preference key to get boolean for whether we are using smart browser.
     */	
    private static String USE_SMART_BROWSER_KEY = "use_smart_browser";
    
    /**
     * Static default shared preferences instance.
     */
    private static final SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationGateway.getAppContext());

    /**
     * Stores first run into SharedPreferences.
     * 
     * @param first run.
     */
    public static void storeFirstRun(boolean firstRun){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(FIRST_RUN, firstRun);
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storeFirstRun(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns first run from SharedPreferences.
     * 
     * @return first run. In case of exception returns <code>null</code>. 
     */
    public static boolean loadFirstRun(){
        try {
        	return mPreferences.getBoolean(FIRST_RUN, true);
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadFirstRun(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return true;
    }
    
    /**
     * Stores router url into SharedPreferences.
     * 
     * @param router url.
     */
    public static void storeRouterURL(String routerURL){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(ROUTER_URL, routerURL);
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storeRouterURL(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns router url from SharedPreferences.
     * 
     * @return router url. In case of exception returns <code>null</code>. 
     */
    public static String loadRouterURL(){
        try {
        	String routerUrl = mPreferences.getString(ROUTER_URL, null);
        	if (routerUrl == null || routerUrl.isEmpty()) {
        	    routerUrl = EnterpriseBrowserActivity.CLOUD_CONNECTION_HOST_PREFIX;
        	}
            return routerUrl;
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadRouterURL(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return null;
    }
    
    /**
     * Stores user {@linkplain Credentials} into SharedPreferences.
     * 
     * @param credentials User's name and password.
     */
    public static void storeCredentials(Credentials credentials){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(USERNAME_KEY, credentials.getUsername());
            editor.putString(PASSWORD_KEY, credentials.getPassword());
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storeCredentials(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns user {@linkplain Credentials} from SharedPreferences.
     * 
     * @return Initialized {@linkplain Credentials} instance. In case of exception returns <code>null</code>. 
     */
    public static Credentials loadCredentials(){
        try {
            return new Credentials(mPreferences.getString(USERNAME_KEY, null), mPreferences.getString(PASSWORD_KEY, null));
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadCredentials(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return null;
    }

    /**
     * Stores preferred the agent's id and its display name into SharedPreferences.
     * 
     * @param agent Agent-specific data.
     */
    public static void storePreferredAgent(AgentEntity agent){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(AGENT_ID_KEY, agent.getAgentId());
            editor.putString(AGENT_DISPLAY_NAME_KEY, agent.getDisplayName());
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storePreferredAgent(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns preferred agent from from SharedPreferences.
     * 
     * @return Initialized AgentEntity instance. In case of exception returns <code>null</code>.
     */
    public static AgentEntity loadPreferredAgent(){
        try {
            return new AgentEntity(mPreferences.getString(AGENT_ID_KEY, null), mPreferences.getString(AGENT_DISPLAY_NAME_KEY, null));
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadPreferredAgent(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return null;
    }

    /**
     * Stores preferred router url into SharedPreferences.
     * 
     * @param string router url.
     */
    public static void storePreferredRouter(String routerURL){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(ROUTER_URL_KEY, routerURL);
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storePreferredRouter(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns preferred router url from SharedPreferences.
     * 
     * @return router url string. In case of exception returns <code>null</code>.
     */
    public static String loadPreferredRouter(){
        try {
            return mPreferences.getString(ROUTER_URL_KEY, null);
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadPreferredRouter(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return null;
    }
    
    /**
     * Stores preferred indicator as to whether or not we are using smart browser.
     * 
     * @param boolean use smart browser.
     */
    public static void storeUseSmartBrowser(Boolean useSmartBrowser){
        try {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(USE_SMART_BROWSER_KEY, useSmartBrowser);
            editor.commit();
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storeUseSmartBrowser(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns preferred indicator as to whether or not we are using smart browser.
     * 
     * @return boolean use smart browser.
     */
    public static Boolean loadUseSmartBrowser(){
        try {
            return mPreferences.getBoolean(USE_SMART_BROWSER_KEY, false);
        } catch (final Exception e) {
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadUseSmartBrowser(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return false;
    }
    
    /**
     * Private constructor to prevent creating new instance of the class.
     */
    private AuthPreferences(){
    }
}
