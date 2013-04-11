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

import java.io.InputStream;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.msopentech.applicationgateway.adapters.UrlAutoCompleteAdapter;
import com.msopentech.applicationgateway.connection.OnOperationExecutionListener;
import com.msopentech.applicationgateway.connection.Router;
import com.msopentech.applicationgateway.data.ConnectionTraits;
import com.msopentech.applicationgateway.data.TabInfo;
import com.msopentech.applicationgateway.data.URLInfo;
import com.msopentech.applicationgateway.preferences.AuthPreferences;
import com.msopentech.applicationgateway.preferences.PersistenceManager;
import com.msopentech.applicationgateway.preferences.PersistenceManager.PersistenceObserver;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements main activity containing enterprise browser.
 */
public class EnterpriseBrowserActivity extends Activity implements PersistenceObserver {
    /**
     * Default "http" prefix.
     */
    static final String HTTP_TOKEN_ATTRIBUTE = "http";

    /**
     * Default "https" prefix.
     */
    static final String HTTPS_TOKEN_ATTRIBUTE = "https";

    /**
     * Default "http://" protocol prefix.
     */
    static final String HTTP_PREFIX_ATTRIBUTE = "http://";

    /**
     * Default "https://" protocol prefix.
     */
    static final String HTTPS_PREFIX_ATTRIBUTE = "https://";

    /**
     * Host prefix for cloud server. 
     */
    public static final String DEFAULT_CLOUD_CONNECTION_HOST_PREFIX = HTTPS_PREFIX_ATTRIBUTE + "appgateway.windows.net/";

    /**
     * Host prefix for cloud server.
     */
    public static String CLOUD_CONNECTION_HOST_PREFIX = DEFAULT_CLOUD_CONNECTION_HOST_PREFIX;

    /**
     * URL postfix to be appended {@link #CLOUD_CONNECTION_HOST_PREFIX} to 'cloudify' URLs via cloud proxy.
     */
    static final String CLOUD_CONNECTION_HOST_BROWSER_POSTFIX = "connect/browser/";

    /**
     * URL postfix to be appended {@link #CLOUD_CONNECTION_HOST_PREFIX} to SMART'cloudify' URLs via cloud proxy.
     */
    static final String CLOUD_CONNECTION_HOST_SMARTBROWSER_POSTFIX = "connect/smartbrowser/";    
    
    /**
     * URL to be combined with user URL to connect via cloud proxy.
     */
    static String CLOUD_BROWSER_URL = CLOUD_CONNECTION_HOST_PREFIX + CLOUD_CONNECTION_HOST_BROWSER_POSTFIX;

    /**
     * Attribute to check for when asserting session validity. Session is espired when it is present in the URL.
     */
    static final String SESSION_EXPIRED_ATTRIBUTE = "session-expired/?orig_url=";

    /**
     * Agents activity ID.
     */
    public static final int ACTIVITY_AGENTS = 1;

    /**
     * Bookmarks activity ID.
     */
    public static final int ACTIVITY_BOOKMARKS_AND_HISTORY = 2;

    /**
     * Sign-In activity ID.
     */
    public static final int ACTIVITY_SIGN_IN = 3;

    /**
     * Advanced Router Settings activity ID.
     */
    public static final int ACTIVITY_ADVANCED_ROUTER_SETTINGS = 4;

    /**
     * ClientStatusAndDiagnostics activity ID.
     */
    public static final int ACTIVITY_CLIENT_STATUS_AND_DIAGNOSTICS = 5;

    /**
     * Extras key to get connection traits.
     */
    public static String EXTRAS_TRAITS_KEY = "connection_traits";

    /**
     * Extras key to get session ID.
     */
    public static String EXTRAS_URL_KEY = "browse_to_url";

    /**
     * Extras key to browse to router system page.
     */
    public static String EXTRAS_BROWSE_TO_ROUTER_SYSTEM_PAGE_KEY = "browse_to_router_system_page";    
    
    /**
     * Extras key to get agent name.
     */
    public static String EXTRAS_SMART_BROWSER_ON = "smart_browser_on";

    /**
     * Extras key to clear cookies.
     */
    public static String EXTRAS_CLEAR_COOKIES_ON = "clear_cookies_on";
    
    /**
     * Sign in indicator.
     */
    private boolean mIsSigninRequired = true;

    /**
     * Boolean to indicate if we are using smart browser.
     */
    private boolean mUseSmartBrowser = false;    
    
    /**
     * Current connection traits.
     */
    public static ConnectionTraits mTraits;

    /**
     * User-entered original URI
     */
    private String mUserOriginalURI;

    /**
     * Stored edit view for the user's URL.
     */
    private AutoCompleteTextView mUrlEditTextView;

    /**
     * Stored back button view.
     */
    private ImageButton mBackButtonView;

    /**
     * Stored forward button view.
     */
    private ImageButton mForwardButtonView;

    /**
     * Stored reload button view.
     */
    private ImageButton mReloadButtonView;

    /**
     * Stored status button.
     */
    private ImageButton mStatusButtonView;
    /**
     * Stored WebView.
     */
    private WebView mActiveWebView;

    /**
     * Stored progress bar view.
     */
    private ProgressBar mProgressBarView;

    /**
     * Stored add tab button.
     */
    private ImageButton mAddButtonView;

    /**
     * A web view client shared among all tabs.
     */
    private MyWebViewClient mWebViewClient;
    
    /**
     * A stored instance of {@linkplain CustomTabHost} to interact with tabs.
     */
    private CustomTabHost mCustomTabHost;
    
    /**
     * Adapter for autocompletion.
     */
    private UrlAutoCompleteAdapter mAutoCompleteAdapter;

    /**
     * Flag to track if session recovery is in progress.
     */
    boolean isInSessionExpiryRecovery = false;

        /**
     * Non-cloudified URL that was originally requested to be loaded by the user.
         */
    String mOriginalUrl = null;

        /**
     * Custom web client to handle page loading requests.
     */
    private class MyWebViewClient extends WebViewClient {
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                Log.d(EnterpriseBrowserActivity.class.getSimpleName(), "onPageStarted() for url = " + url);
                
                if (mOriginalUrl == null) {
                     mOriginalUrl = convertCloudUrlToNormal(url);
                }

                //Handle session expiry.
                if (url.contains(SESSION_EXPIRED_ATTRIBUTE)) {
                    mActiveWebView.stopLoading();
                    if (isInSessionExpiryRecovery) {
                    return;
                }
                    isInSessionExpiryRecovery = true;

                        // Listener for the 2nd attempt when full authentication is required.
                    	final OnOperationExecutionListener authListener = new OnOperationExecutionListener() {
                    		@Override
                    		public void onExecutionComplete(int operation, final Object[] result) {
                    			try {
                    				ConnectionTraits traits = (ConnectionTraits) result[0];
                    				if(!traits.isError()) {
                    					// Updating connection traits after full auth is successful.
                    					mTraits = traits;
                    					// Switch to agent specific stored data.
                    					PersistenceManager.initialize(mTraits.agent.getAgentId());
                                        isInSessionExpiryRecovery = false;
                    					// Continue url load process started by the user.
                                        goToUrl(mOriginalUrl);
                    				} else {
                    mIsSigninRequired = true;
                        showSignIn(getResources().getString(R.string.sign_in_session_expiry_alert_text), false);	
                    }
                    			} catch (final Exception e) {}
                    		}
                    		@Override
                    		public void onBeforeExecution(int operation) {}
                    	};
                        // Listener for the 1st attempt when only session recovery is required.
                        OnOperationExecutionListener sessionRecoveryListener = new OnOperationExecutionListener() {
                            @Override
                            public void onExecutionComplete(int operation, final Object[] result) {
                                try {
                                    ConnectionTraits traits = (ConnectionTraits) result[1];
                                    if(!traits.isError()) {
                                        // Updating session after successful session request.
                                        mTraits.setSession(traits.sessionID);
                                        isInSessionExpiryRecovery = false;
                                        goToUrl(mOriginalUrl);
                                    } else {
                                        // Starting attempt 2 - full authentication.
                                        Router.performRequest(Router.ACTION_AUTHENTICATE, new Object[]{ AuthPreferences.loadCredentials() }, authListener, EnterpriseBrowserActivity.this);
                                    }
                                } catch (final Exception e) {}
                            }
                            @Override
                            public void onBeforeExecution(int operation) {}
                        };
                        // Starting attempt 1 - session recovery.
                        Router.performRequest(Router.ACTION_OBTAIN_SESSION, new Object[]{ mTraits }, sessionRecoveryListener, EnterpriseBrowserActivity.this);
                    return;
                }

                String displayUrl = EnterpriseBrowserActivity.convertCloudUrlToNormal(url);

                TabInfo tab = mCustomTabHost.getTabInfoForWebView(view);
                if(tab == null) {
                    return;
                }
                
                tab.setPageLoadingInProgress(true);

                //We can change the toolbar here only if the current tab is active.
                if(mCustomTabHost.isTabActive(tab)) {
                    String displayedUrl = mUrlEditTextView.getText().toString();
                    if (!displayedUrl.contentEquals(displayUrl)) {
                        mUrlEditTextView.setText(displayUrl);
                    }
                    mUrlEditTextView.setEnabled(false);
                    mForwardButtonView.setEnabled(false);
                    mBackButtonView.setEnabled(false);
                    mReloadButtonView.setImageResource(R.drawable.cancel);
                    mReloadButtonView.setEnabled(true);
                }
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onPageStarted(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            try {
                Log.d(EnterpriseBrowserActivity.class.getSimpleName(), "onPageFinished() for url = " + url);

                TabInfo tab = mCustomTabHost.getTabInfoForWebView(view);
                
                //The tab must have been closed while loading a page and is dead now. Do nothing in this case.
                if(tab == null) {
                    return;
                }
                
                tab.setPageLoadingInProgress(false);

                //Update the label of the tab.
                tab.getLabel().setText(view.getTitle());

                //We can change the toolbar here only if the current tab is active.
                if(mCustomTabHost.isTabActive(tab)) {
                    mForwardButtonView.setEnabled(mActiveWebView.canGoForward());
                    mBackButtonView.setEnabled(mActiveWebView.canGoBack());
                    mUrlEditTextView.setEnabled(true);
                    mReloadButtonView.setImageResource(R.drawable.reload_button);
                }

                String realUrl = convertCloudUrlToNormal(url);
                PersistenceManager.addRecord(PersistenceManager.ContentType.HISTORY, new URLInfo(realUrl, view.getTitle()));
                mProgressBarView.setProgress(0);
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onPageFinished(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        	if (!mUseSmartBrowser) {
        		return null;
        	} else if (url.startsWith(CLOUD_BROWSER_URL)) {
            	return null;	
            } else if (url.contentEquals(CLOUD_CONNECTION_HOST_PREFIX + "system")) {
            	return null;
            } else {
            	try {
                    HttpClient agentsClient = new DefaultHttpClient();
                    String fixupUrl = EnterpriseBrowserActivity.convertNormalUrlToCloud(url, mTraits.sessionID, mUserOriginalURI);
                    HttpGet agentsRequest = new HttpGet(fixupUrl);
                    BasicHttpResponse response = (BasicHttpResponse) agentsClient.execute(agentsRequest);

                    StatusLine status = response.getStatusLine();
                    if (status.getStatusCode() != 200 ||
                  		response.getFirstHeader("Content-Type") == null	|| 
                  		response.getFirstHeader("Transfer-Encoding") == null) {
                    	return null;
                    }
                    
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                    	return null;
                    }
                    
                    InputStream responseStream = entity.getContent();
                    if (responseStream == null) {
                    	return null;
                    }
                    	
                    return new WebResourceResponse(response.getFirstHeader("Content-Type").getValue().toString(), response.getFirstHeader("Transfer-Encoding").getValue().toString(), responseStream);
 
    		} catch (Exception e) {
                    Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onPageFinished(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
                    return null;
            	}
            }
        }    
    }

    /**
     * This delegate function is called each time a new item is added into the merged storage of PersistenceManager.
     */
    public void onSuggestionsListChanged() {
        mAutoCompleteAdapter.notifyDataSetChanged();
    }

    /**
     * Converts regular/normal URL to a 'cloudified' one i.e. with cloud prefix and session ID. Converts only non-cloudified URLs and only
     * if we have active session ID, otherwise returns incoming URL without changes.
     * 
     * @param url URL to be converted.
     * @param sessionID ID of the current active session.
     * @param originalUrl Initial URL.
     * 
     * @return URL converted to a cloud format. Returns incoming URL without changes if it already has cloud prefix or if session ID equals
     *         <code>null</code> or if exception is caught.
     */
    private static String convertNormalUrlToCloud(String url, String sessionID, String originalUrl) {
        try {
            if (!url.startsWith(CLOUD_BROWSER_URL) && sessionID != null) {
                boolean http = true;

                if (url.startsWith(CLOUD_CONNECTION_HOST_PREFIX)) { // Special case for JS and other possible returned URLs which we need to fixup
                	url = url.substring(CLOUD_CONNECTION_HOST_PREFIX.length());
                	if (!originalUrl.endsWith("/"))
                		url = originalUrl + '/' + url;
                	else
                		url = originalUrl + url;
                }
                
                if (url.startsWith(HTTPS_PREFIX_ATTRIBUTE)) {
                    http = false;
                }

                if (url.startsWith(HTTPS_PREFIX_ATTRIBUTE)) {
                    url = url.substring(HTTPS_PREFIX_ATTRIBUTE.length());
                } else if (url.startsWith(HTTP_PREFIX_ATTRIBUTE)) {
                    url = url.substring(HTTP_PREFIX_ATTRIBUTE.length());
                }

                if (http) {
                    url = CLOUD_BROWSER_URL + sessionID + "/" + HTTP_TOKEN_ATTRIBUTE + "/" + url;
                } else {
                    url = CLOUD_BROWSER_URL + sessionID + "/" + HTTPS_TOKEN_ATTRIBUTE + "/" + url;
                }
            }
            return url;
        } catch (final Exception e) {
            Log.d(EnterpriseBrowserActivity.class.getSimpleName() + ".convertNormalUrlToCloud()", "Failed.");
        }
        return url;
    }

    /**
     * Takes a cloud URL with {@linkplain #CLOUD_BROWSER_URL} prefix and converts it info a regular URL.
     * 
     * @param url URL to convert.
     * @return Regular URL. Returns String provided as an argument if exception occurs. 
     */
    private static String convertCloudUrlToNormal(String url) {
        try {
            if (!url.startsWith(CLOUD_BROWSER_URL)) {
                return url;
            }

            int charactersToCutOff = CLOUD_BROWSER_URL.length() + 1;

            String str = url.substring(charactersToCutOff);

            int realAddressIndex = str.indexOf('/');
            if (realAddressIndex == -1) {
                return url;
            }

            realAddressIndex++;

            if (realAddressIndex > str.length()) return null;

            str = str.substring(realAddressIndex);

            return str.replaceFirst("/", "://");
        } catch (final Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".convertCloudUrlToNormal(): Failed. " + e.toString(), ApplicationGateway.getAppContext());            
        }

        return url;
    }

    /**
     * Opens provided URL within {@linkplain WebView}
     * 
     * @param normalUrl URL to be opened.
     * @return <code>True</code> if URL can be safely opened i.e. if we have a valid session ID, <code>false</code> otherwise (redirects
     *         user to perform sign-in operation).
     */
    private boolean goToUrl(String normalUrl) {
        Log.d(EnterpriseBrowserActivity.class.getSimpleName(), "goToUrl() for url = " + normalUrl);
        if (mTraits == null || mTraits.sessionID == null) {
            mIsSigninRequired = true;
            //TODO:
            showSignIn(null, false);
            return false;
        } else {
            String fixupUrl = EnterpriseBrowserActivity.convertNormalUrlToCloud(normalUrl, mTraits.sessionID, mUserOriginalURI);
            mActiveWebView.loadUrl(fixupUrl);
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.main);

            String preferredRouter = AuthPreferences.loadPreferredRouter();
            if (preferredRouter != null && !preferredRouter.isEmpty()) {
            	CLOUD_CONNECTION_HOST_PREFIX = preferredRouter;
            }
            
            Boolean useSmartBrowser = AuthPreferences.loadUseSmartBrowser();
            if (useSmartBrowser) {
            	CLOUD_BROWSER_URL = CLOUD_CONNECTION_HOST_PREFIX + CLOUD_CONNECTION_HOST_SMARTBROWSER_POSTFIX;
            } else {
            	CLOUD_BROWSER_URL = CLOUD_CONNECTION_HOST_PREFIX + CLOUD_CONNECTION_HOST_BROWSER_POSTFIX;
            }
            
            mWebViewClient = new MyWebViewClient();

            mBackButtonView = (ImageButton) findViewById(R.id.main_back);
            mForwardButtonView = (ImageButton) findViewById(R.id.main_go);
            mAddButtonView = (ImageButton) findViewById(R.id.main_add_tab);
            mStatusButtonView = (ImageButton) findViewById(R.id.main_status);
            mProgressBarView = (ProgressBar) findViewById(R.id.main_progress_bar);
            mReloadButtonView = (ImageButton) findViewById(R.id.main_reload);
            mUrlEditTextView = (AutoCompleteTextView) findViewById(R.id.main_url);

            // The following is present to fix a bug found on devices with hard keyboards,
            // not soft keyboards.  Hard keyboards have physical keys versus a soft
            // keyboard which uses a displayed keyboard.  With hard keyboards and EditText boxes 
            // on activities with TabHosts, entering certain keys on the hard keyboard 
            // causes the focus to shift from an EditText to another item on the activity.
            // This manifested itself when running the browser on an oDriod device and no
            // text could be entered into mUrlEditTextView.  
            //
            // The following work-around appears to solve this problem (found on forums) 
            // without causing any regressions.
            View.OnTouchListener focusHandler = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.requestFocusFromTouch();
                    return false;
                }
            };
            mUrlEditTextView.setOnTouchListener(focusHandler);
            
            mCustomTabHost = new CustomTabHost();
            
            PersistenceManager.registerObserver(this);

            mAutoCompleteAdapter = new UrlAutoCompleteAdapter(this, android.R.layout.simple_list_item_1, PersistenceManager.getAllMergedRecords(), mCustomTabHost);
            mUrlEditTextView.setAdapter(mAutoCompleteAdapter);

            View.OnClickListener listener = new View.OnClickListener() {
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.main_go: {
                            if (mActiveWebView.canGoForward()) mActiveWebView.goForward();
                            break;
                        }
                        case R.id.main_back: {
                            if (mActiveWebView.canGoBack()) mActiveWebView.goBack();
                            break;
                        }
                        case R.id.main_reload: {
                            TabInfo tab = mCustomTabHost.getTabInfoForWebView(mActiveWebView);
                            if (tab.isPageLoadingInProgress()) {
                                mActiveWebView.stopLoading();
                            } else {
                                String url = mActiveWebView.getUrl();
                                if (url != null && !mIsSigninRequired) {
                                    //The user could have got a new session. 
                                    //We can neither clear the current url in WebView, 
                                    //nor use its reload() function since the url inside it is "cloudified" using the previous SessionID.
                                    //However, we can safely extract it from the WebView, normalize it, and then "cloudify" it with the right SessionID.
                                    //This implementation allows us to keep the reload button always enabled during the lifetime of a tab after the first URL has been entered.
                                    goToUrl(convertCloudUrlToNormal(url));
                                }
                            }
                            break;
                        }
                        case R.id.main_add_tab: {
                            mCustomTabHost.createNewTab();
                            break;
                        }
                    }
                }
            };

            mAddButtonView.setOnClickListener(listener);
            mForwardButtonView.setOnClickListener(listener);
            mBackButtonView.setOnClickListener(listener);
            mReloadButtonView.setOnClickListener(listener);

            mForwardButtonView.setEnabled(false);
            mBackButtonView.setEnabled(false);

            mUrlEditTextView.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if(mUrlEditTextView.isPopupShowing()) {
                            mUrlEditTextView.dismissDropDown();
                        }

                        String uri = mUrlEditTextView.getText().toString();

                        if (!uri.startsWith(HTTP_TOKEN_ATTRIBUTE) && !uri.startsWith(HTTPS_TOKEN_ATTRIBUTE)) {
                            uri = HTTP_PREFIX_ATTRIBUTE + uri;
                            mUrlEditTextView.setText(uri);
                        }
                        mUserOriginalURI = uri;
                        if (goToUrl(mUserOriginalURI)) return true;
                    }
                    return false;
                }
            });

            mTraits = null;
            showSignIn(null, false);
        } catch (final Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    public void showSettingsPopup(View v) {
        try {
            PopupMenu popup = new PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_connection: {
                            showAgentsList();
                            return true;
                        }
                        case R.id.menu_sign_in_or_out: {
                            if(mIsSigninRequired) {
                                // Menu item in sign in mode.
                                //TODO: 
                                showSignIn(null, true);
                                return true;
                            }

                            // Clean up the session.
                            mTraits.sessionID = null;
                            mTraits.token = null;
                            mTraits.agent = null;

                            // Indicate the condition.
                            mIsSigninRequired = true;

                            //Remove all tabs except one.
                            mCustomTabHost.resetTabs();

                            mUrlEditTextView.setText("");

                            mProgressBarView.setProgress(0);

                            // Disable the reload button and set the right image for it.
                            mReloadButtonView.setEnabled(false);
                            mReloadButtonView.setImageResource(R.drawable.reload_button);

                            mStatusButtonView.setImageResource(R.drawable.connection_red);

                            // Since it's the user's intention to sign out, his
                            // history must be dropped.
                            PersistenceManager.dropContent(PersistenceManager.ContentType.HISTORY);

                            return true;
                        }
// Removed from RELEASE version. Should be active for development ONLY.                       
//                        case R.id.menu_advanced_router_settings: {
//                            showAdvancedRouterSettings(null);
//                        }
                        default: {
                            return false;
                        }
                    }
                }
            });
            inflater.inflate(R.menu.settings_menu, popup.getMenu());

            if(mIsSigninRequired) {
                MenuItem signItem = popup.getMenu().findItem(R.id.menu_sign_in_or_out);
                signItem.setTitle(getResources().getString(R.string.browser_menu_item_sign_in));
            }

            // Assumes that agent always has a display name. If display name is not present agent is considered to be not connected.
            MenuItem agentItem = popup.getMenu().findItem(R.id.menu_connection);
            String status = getResources().getString(R.string.browser_menu_item_choose_connection_not_connected);
            if(mTraits != null && mTraits.agent != null && !TextUtils.isEmpty(mTraits.agent.getDisplayName())) {
                status = mTraits.agent.getDisplayName();
            }
            Spannable span = new SpannableString(getResources().getString(R.string.browser_menu_item_choose_connection, status));
            span.setSpan(new RelativeSizeSpan(0.8f), 18, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.GRAY), 18, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            agentItem.setTitle(span);

            popup.show();
        } catch (final Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showSettingsPopup(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    /**
     * Invokes bookmarks and history activity.
     * 
     * @param v View this method is attached to.
     */
    public void onShowBookmarks(View v) {
        try {
            showBookmarksList();
        } catch (final Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onShowBookmarks(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }
      
    /**
     * Shows dialog enabling user to bookmark current (by default) or any other page.
     * 
     * @param v View this method is attached to.
     */
    public void showAddBookmarkDialog(View v) {
        try {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.create_bookmark_dialog);
            dialog.setTitle(getString(R.string.bookmark_dialog_title));
            dialog.setCanceledOnTouchOutside(false);

            TextView cancelButton = (TextView) dialog.findViewById(R.id.create_bookmark_cancel);
            TextView saveButton = (TextView) dialog.findViewById(R.id.create_bookmark_ok);

            OnClickListener listener = new OnClickListener() {
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.create_bookmark_cancel: {
                            dialog.dismiss();
                            break;
                        }
                        case R.id.create_bookmark_ok: {
                            String name = ((EditText) dialog.findViewById(R.id.create_bookmark_name)).getText().toString();
                            String address = ((EditText) dialog.findViewById(R.id.create_bookmark_url)).getText().toString();

                            if (null != name && !(name.contentEquals("")) && null != address && !(address.contentEquals(""))) {
                                PersistenceManager.addRecord(PersistenceManager.ContentType.BOOKMARKS, new URLInfo(address, name));
                            }

                            dialog.dismiss();
                            break;
                        }
                    }
                }
            };

            cancelButton.setOnClickListener(listener);
            saveButton.setOnClickListener(listener);

            EditText name = (EditText) dialog.findViewById(R.id.create_bookmark_name);
            EditText address = (EditText) dialog.findViewById(R.id.create_bookmark_url);

            name.setText(mActiveWebView.getTitle());
            address.setText(mUrlEditTextView.getText());

            dialog.show();
        } catch (final Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showAddBookmarkDialog(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    /**
     * Opens bookmarks & history activity.
     */
    public void showBookmarksList() {
        try {
            Intent intent = new Intent(this, BookmarksActivity.class);
            startActivityForResult(intent, ACTIVITY_BOOKMARKS_AND_HISTORY);
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showBookmarksList(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    /**
     * Opens proxy agents list. Redirects user to Sign In activity in case current token is invalid (equals <code>null</code>).
     */
    public void showAgentsList() {
        try {
            if (mTraits == null || mTraits.token == null) {
                showSignIn(getResources().getString(R.string.sign_in_connection_choose_alert_text), false);
            } else {
                // We need agent and token from traits.
                Intent intent = new Intent(this, AgentsActivity.class).putExtra(EnterpriseBrowserActivity.EXTRAS_TRAITS_KEY, mTraits);
                startActivityForResult(intent, ACTIVITY_AGENTS);
            }
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showAgentsList(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    /**
     * Opens Advanced Router Settings activity.
     */
    public void showAdvancedRouterSettings(String message) {
        try {
            Intent intent = new Intent(this, AdvancedRouterSettingsActivity.class);
            startActivityForResult(intent, ACTIVITY_ADVANCED_ROUTER_SETTINGS);
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showAdvancedRouterSettings(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }    

    /**
     * Opens Sign In activity.
     * 
     * @param message Optional message (can be <code>null</code>) to be displayed within Sign In activity to explain user the reason why it
     *            was opened.
     * @param uiPrompt Flag defining if opening sign in page was a user action otr not.
     */
    public void showSignIn(String message, boolean uiPrompt) {
        try {
            Intent intent = new Intent(this, SignInActivity.class);
            if (message != null) intent.putExtra(SignInActivity.ALERT_MESSAGE_KEY, message);
            intent.putExtra(SignInActivity.UI_PROMPT, Boolean.toString(uiPrompt));
            startActivityForResult(intent, ACTIVITY_SIGN_IN);
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showSignInList(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    /**
     * Opens status and diagnostics activity.
     */
    public void showClientStatusAndDiagnostics(View v) {
        try {
            Intent intent = new Intent(this, ClientStatusAndDiagnosticsActivity.class);
            startActivityForResult(intent, ACTIVITY_CLIENT_STATUS_AND_DIAGNOSTICS);
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".showClientStatusAndDiagnostics(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            // The data argument can be null if the user simply exited the
            // activity by clicking back or cancel.
            if (data == null) {
                return;
            }

            Bundle extras = data.getExtras();

            if (extras == null) {
                return;
            }

            switch (requestCode) {
                case ACTIVITY_SIGN_IN: {
                    if (resultCode != RESULT_CANCELED) {
                        mCustomTabHost.clearAllHistory();

                        mTraits = (ConnectionTraits) data.getSerializableExtra(EnterpriseBrowserActivity.EXTRAS_TRAITS_KEY);

                        if (mTraits.sessionID == null && mTraits.token == null && mTraits.agent.getAgentId() == null) {
                            // Authentication failed. Have to make user make another attempt while informing of error reason.
                            showSignIn(null, true);
                        } else {
                            mStatusButtonView.setImageResource(R.drawable.connection_green);

                            if (mIsSigninRequired) {
                                mIsSigninRequired = false;
                            }
                            // Switch to agent specific stored data.
                            PersistenceManager.initialize(mTraits.agent.getAgentId());
                        }
                    } else {
                        if(mTraits == null || mTraits.isError()) {
                            // Will get here if error was received on signIn screen and user pressed 'cancel' there.
                            mIsSigninRequired = true;
                            if(mTraits != null) {
                                mTraits.sessionID = null;
                                mTraits.token = null;
                            }
                	}
                     }
                    break;
                }

                case ACTIVITY_ADVANCED_ROUTER_SETTINGS: {
                    String url = data.getStringExtra(CLOUD_CONNECTION_HOST_PREFIX);
                    Boolean useSmartBrowser = data.getBooleanExtra(EXTRAS_SMART_BROWSER_ON, false);
                    
                    if (!CLOUD_CONNECTION_HOST_PREFIX.contentEquals(url) || mUseSmartBrowser != useSmartBrowser) {
                        CLOUD_CONNECTION_HOST_PREFIX = url;
                        CLOUD_BROWSER_URL = CLOUD_CONNECTION_HOST_PREFIX;

                        mUseSmartBrowser = useSmartBrowser;
                        AuthPreferences.storeUseSmartBrowser(mUseSmartBrowser);
	                    
                        if (useSmartBrowser) {
                            CLOUD_BROWSER_URL = CLOUD_BROWSER_URL + CLOUD_CONNECTION_HOST_SMARTBROWSER_POSTFIX;
                        } else {
                            CLOUD_BROWSER_URL = CLOUD_BROWSER_URL + CLOUD_CONNECTION_HOST_BROWSER_POSTFIX;
                        }
                    
                        AuthPreferences.storePreferredRouter(url);
                        mStatusButtonView.setImageResource(R.drawable.connection_red);
                        mCustomTabHost.clearAllHistory();
                        showSignIn(null, false);
                    }

                    Boolean clearCookies = data.getBooleanExtra(EXTRAS_CLEAR_COOKIES_ON, false);
                    if (clearCookies) {
                    	CookieManager.getInstance().removeAllCookie();
                    }
                    
                    break;
                }

                case ACTIVITY_BOOKMARKS_AND_HISTORY: {
                    String url = data.getStringExtra(EXTRAS_URL_KEY);
                    if (!TextUtils.isEmpty(url)) {
                        goToUrl(url);
                    }
                    break;
                }

                case ACTIVITY_AGENTS: {
                    // Since the agent has changed, we can no longer use the old
                    // history in WebViewClient with the old SessionID.
                    mCustomTabHost.clearAllHistory();
                    mReloadButtonView.setEnabled(false);

                    ConnectionTraits traits = (ConnectionTraits) data.getSerializableExtra(EXTRAS_TRAITS_KEY);

                    if (traits != null && traits.sessionID != null) {
                        mTraits.sessionID = traits.sessionID;
                        mStatusButtonView.setImageResource(R.drawable.connection_green);
                        // Should never be null
                        if (traits.agent != null) {
                            if (traits.agent.getAgentId() != null) {
                                mTraits.agent.setAgentId(traits.agent.getAgentId());
                                // Switch to agent specific stored data.
                                PersistenceManager.dropContent(PersistenceManager.ContentType.HISTORY);
                                PersistenceManager.initialize(mTraits.agent.getAgentId());                                
                            }
                            if (traits.agent.getDisplayName() != null) {
                                mTraits.agent.setDisplayName(traits.agent.getDisplayName());
                        }
                        }
                    }
                    break;
                    }
                
                case ACTIVITY_CLIENT_STATUS_AND_DIAGNOSTICS: {
                	String browse = data.getStringExtra(EXTRAS_BROWSE_TO_ROUTER_SYSTEM_PAGE_KEY);
                	if (!(browse == null || browse.isEmpty()))
                        mActiveWebView.loadUrl(CLOUD_CONNECTION_HOST_PREFIX + "system");
                	
                    break;
                }
            }
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onActivityResult(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                if (mActiveWebView.canGoBack()) {
                    mActiveWebView.goBack();
                    return true;
                }
            }
        } catch (Exception e) {
            Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".onKeyDown(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Implements a wrapper for tabs.
     */
    class CustomTabHost implements Runnable, UrlAutoCompleteAdapter.IPublishResultsNotifiable {
        /**
         * The progress bar updater, shared among the tabs.
         */
        private ProgressUpdater mProgressBar;

        /**
         * A new tab label.
         */
        private static final String NEW_TAB_LABEL = "New tab";

        /**
         * Since we can't delete a single tab from TabHost, we need this vector to keep track of all TabSpecs.
         */
        private Vector<TabInfo> mTabStorage;

        /**
         * Stored TabHost instance.
         */
        private TabHost mTabHost;

        /**
         * A factory object for creating new tabs' content.
         */
        private TabHost.TabContentFactory mTabFactory;

        /**
         * A stored HorizontalScrollView.
         */
        private HorizontalScrollView mTabScrollView;
        
        /**
         * This method is used to asynchronously scroll the tab toolbar right when a new tab is created.
         */
        public void run() {
            mTabScrollView.fullScroll(ScrollView.FOCUS_RIGHT);
        }
        
        /**
         * A class constructor.
         */
        public CustomTabHost() {
            try {
                mTabFactory = new TabHost.TabContentFactory() {
                    public View createTabContent(String tag) {
                        return mActiveWebView;
                    }
                };
                mProgressBar = new ProgressUpdater();
                
                mTabScrollView = (HorizontalScrollView)findViewById(R.id.scroll);
                mTabHost = (TabHost)findViewById(android.R.id.tabhost);
                mTabHost.setup();

                OnTabChangeListener listener = new OnTabChangeListener() {
                    public void onTabChanged(String tabId) {
                        mActiveWebView = (WebView) mTabHost.getCurrentView();

                        //Set the right condition for the back and forward buttons.
                        mForwardButtonView.setEnabled(mActiveWebView.canGoForward());
                        mBackButtonView.setEnabled(mActiveWebView.canGoBack());

                        TabInfo tab = getTabInfoForWebView(mActiveWebView);
                        updateTabsUI();

                        //Change the state of the progress bar.
                        mProgressBarView.setProgress(tab.getProgress());

                        if(tab.isPageLoadingInProgress()) {
                            mUrlEditTextView.setEnabled(false);
                            mReloadButtonView.setImageResource(R.drawable.cancel);
                            mReloadButtonView.setEnabled(true);
                        } else {
                            mUrlEditTextView.setEnabled(true);
                            mReloadButtonView.setImageResource(R.drawable.reload_button);

                            if(mActiveWebView.getUrl() != null) { //If getUrl is null, then the user hasn't entered any url for this tab yet.
                                mReloadButtonView.setEnabled(true);
                            } else {
                                mReloadButtonView.setEnabled(false);
                            }
                        }

                        String pageUrl = mActiveWebView.getUrl();
                        if(pageUrl != null) {
                            mUrlEditTextView.setText(convertCloudUrlToNormal(pageUrl));
                        } else {
                            mUrlEditTextView.setText("");
                        }
                    }};

                    mTabHost.setOnTabChangedListener(listener);
                    mTabStorage = new Vector<TabInfo>();
                    createNewTab();
            } catch (Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".listener.onTabChanged(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }
        }
        
        /**
         * Creates a new tab and makes it active.
         */
        private void createNewTab() {
            try {
                TabSpec tab = mTabHost.newTabSpec(NEW_TAB_LABEL);

                LayoutInflater mInflater = LayoutInflater.from(EnterpriseBrowserActivity.this);
                mActiveWebView = (WebView) mInflater.inflate(R.layout.web_view, null);

                mActiveWebView.setWebChromeClient(mProgressBar);
                mActiveWebView.setWebViewClient(mWebViewClient);
                mActiveWebView.getSettings().setJavaScriptEnabled(true);
                mActiveWebView.getSettings().setBuiltInZoomControls(true);
                mActiveWebView.getSettings().setSupportZoom(true);
                mActiveWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                mActiveWebView.getSettings().setUseWideViewPort(true);

                tab.setContent(mTabFactory);

                View tabIndicator = LayoutInflater.from(EnterpriseBrowserActivity.this).inflate(R.layout.tab_widget_layout, mTabHost.getTabWidget(), false);
                TextView title = (TextView) tabIndicator.findViewById(R.id.tab_title);
                title.setText(NEW_TAB_LABEL);

                ImageButton closeTabButton = (ImageButton) tabIndicator.findViewById(R.id.close_tab_button);
                closeTabButton.setOnClickListener(mTabDeletionListener);

                tab.setIndicator(tabIndicator);

                TabInfo tabInfo = new TabInfo(tab, closeTabButton, mActiveWebView, title, (ImageView) tabIndicator.findViewById(R.id.tab_left_edge), (ImageView) tabIndicator.findViewById(R.id.tab_right_edge));

                if(mTabStorage.isEmpty()) { //If we created the first tab the cross must be gone.
                    tabInfo.getCloseButton().setVisibility(View.GONE); 
                } else if (mTabStorage.size() == 1) { //We must return the cross for the first tab, if we're creating the second one now.
                    mTabStorage.elementAt(0).getCloseButton().setVisibility(View.VISIBLE);
                }

                mTabStorage.add(tabInfo);

                int newTabIndex = mTabHost.getTabWidget().getTabCount();
                mTabHost.addTab(tab);
                mTabHost.setCurrentTab(newTabIndex);

                mUrlEditTextView.setText("");
                //Scroll right the tab toolbar.
                mTabScrollView.post(this);
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".createNewTab(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }            
        }
        
        private void updateTabsUI() {
            TabInfo tab = getTabInfoForWebView(mActiveWebView);
            //Display tab edges correctly.
            int tabIndex = mTabHost.getCurrentTab();
            int tabNumber = mTabStorage.size();
            TabInfo element;
            for(int i = 0; i < tabNumber; i++) {
                element = mTabStorage.elementAt(i);
                if(element == tab) {
                    element.getLeftEdge().setImageResource(R.drawable.tab_active_left);
                    element.getRightEdge().setImageResource(R.drawable.tab_active_right);
                } else {
                    if(i < tabIndex) {
                        element.getLeftEdge().setImageResource(R.drawable.tab_inactive_left);
                        element.getRightEdge().setImageResource(R.drawable.tab_inactive_filling);  
                    } else { 
                        element.getLeftEdge().setImageResource(R.drawable.tab_inactive_filling);
                        element.getRightEdge().setImageResource(R.drawable.tab_inactive_right);
                    }
                }
            }
        }
        
        /**
         * A listener that handles tab closing. It implements the only safe way to delete a tab.
         */
        private OnClickListener mTabDeletionListener = new OnClickListener() {
            public void onClick(View view) {
                try {
                    if(mTabStorage.size() == 1) { //There is only one tab, user can't close it.
                        return;
                    }

                    int activeTabIndex = mTabHost.getCurrentTab();
                    mTabHost.clearAllTabs();
                    TabInfo tabInfo;
                    int elementToDeleteIndex = -1;
                    for(int i = 0; i < mTabStorage.size(); i++) {
                        tabInfo = mTabStorage.elementAt(i);
                        if(tabInfo.getCloseButton() != view) {
                            mTabHost.addTab(tabInfo.getTabSpec());
                        } else {
                            elementToDeleteIndex = i;
                        }
                    }

                    if(elementToDeleteIndex == -1) return;

                    //Halt any activity in the webview in case it is.
                    tabInfo = mTabStorage.elementAt(elementToDeleteIndex);
                    if(tabInfo.isPageLoadingInProgress())
                        tabInfo.getWebView().stopLoading();
                    
                    //Now it is safe to remove the corresponding tabInfo from the storage.
                    mTabStorage.remove(elementToDeleteIndex);

                    //If there is only one tab left, we don't need to set the right active tab, but 'close' button must be made invisible.
                    if(mTabStorage.size() == 1) {
                        mTabStorage.elementAt(0).getCloseButton().setVisibility(View.GONE);
                        return;
                    }

                    //Since we know that indices in the vector correspond to the right indices in the TabHost, we can calculate the position of the active tab.
                    if(activeTabIndex < elementToDeleteIndex) { //The deleted tab followed the active tab.
                        mTabHost.setCurrentTab(activeTabIndex);
                    } else {                                     
                        if(activeTabIndex == elementToDeleteIndex) { //The deleted tab was active.
                            //If the deleted tab was not the first one, no correction is required.
                            if(activeTabIndex != 0) activeTabIndex--;
                            mTabHost.setCurrentTab(activeTabIndex);
                        } else {
                            mTabHost.setCurrentTab(--activeTabIndex); //The deleted tab preceded the active one. The index must be adjusted.
                        }
                    }
                } catch (final Exception e) {
                    Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".tabDeletionListener.onClick(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
                }                    
            }
        };

        /**
         * Stops all tabs and brings the activity to its initial state.
         */
        private void resetTabs() {
            try {
                for(int i = 0; i < mTabStorage.size(); i++) {
                    TabInfo tab = mTabStorage.elementAt(i);
                    tab.setPageLoadingInProgress(false);
                    tab.getWebView().stopLoading();
                }

                mTabHost.clearAllTabs();
                mTabStorage.clear();
                createNewTab();
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".resetTabs(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            } 
        }

        /**
         * Returns TabInfo object for the specified WebView.
         * 
         * @param webView {@linkplain WebView} instance to be associated with requested {@linkplain TabInfo} data object.
         * 
         * @return TabInfo object for the specified view.
         */
        private TabInfo getTabInfoForWebView(WebView webView) {
            try {
                for(int i = 0; i != mTabStorage.size(); i++) {
                    TabInfo tab = mTabStorage.elementAt(i);
                    if(tab.getWebView() == webView) {
                        return tab;
                    }
                }
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".getTabInfoForWebView(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }

            return null;
        }

        /**
         * Helps to identify whether the given TabInfo object belongs to the active tab.
         * 
         * @param tab Recipient data object.
         * 
         * @return True if the object belongs to the active tab, false otherwise.
         */
        boolean isTabActive(final TabInfo tab) {
            if(tab == mTabStorage.elementAt(mTabHost.getCurrentTab())) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Cleans {@linkplain WebView} history in all tabs. Disables 'back' and 'forward' toolbar buttons.
         */
        public void clearAllHistory() {
            try {
                for(int i = 0; i != mTabStorage.size(); i++) {
                    mTabStorage.elementAt(i).getWebView().clearCache(true);
                    mTabStorage.elementAt(i).getWebView().clearHistory();
                }

                mForwardButtonView.setEnabled(false);
                mBackButtonView.setEnabled(false);
            } catch (final Exception e) {
                Utility.showAlertDialog(EnterpriseBrowserActivity.class.getSimpleName() + ".clearAllHistory(): Failed. " + e.toString(), EnterpriseBrowserActivity.this);
            }            
        }
        
        @Override
        public boolean shouldPublishResults() {
            return !mTabStorage.elementAt(mTabHost.getCurrentTab()).isPageLoadingInProgress();
        }        

        /**
         * Simple page loading progress handler.
         */
        private class ProgressUpdater extends WebChromeClient {
            public void onProgressChanged(WebView view, int progress) {
                try {
                    if (progress == 100) {
                        if (!isInSessionExpiryRecovery){
                            mOriginalUrl = null;
                        }
                        progress = 0;
                    }

                    TabInfo tab = getTabInfoForWebView(view);

                    // With the use of smartbrowser and shouldInterceptRequest, this is occassionally called after the initial page load
                    // finishes. This is an unfortunate hack.
                    if (!mUrlEditTextView.isEnabled()) {
                    tab.setProgress(progress);

                    if(isTabActive(tab)) {
                            mUrlEditTextView.dismissDropDown();
                        mProgressBarView.setProgress(progress);
                    }
                    }
                } catch (final Exception e) {
                    // Log.d() used intentionally to prevent ubiquitous dialogs in case of error.
                    Log.d(ProgressUpdater.class.getSimpleName(), ".onProgressChanged(): Failed. " + e.toString());
                }
            }
        }
    }
}