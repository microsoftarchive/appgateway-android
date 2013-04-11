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

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/**
 * Implements wrapper over tab associated data.
 */
public class TabInfo {
    /**
     * Required to recreate a tab.
     */
    private TabSpec tabSpec;

    /**
     * Close tab button, associated with the tab.
     */
    private View closeButton;

    /**
     * Tab label.
     */
    private TextView label;

    /**
     * WebView itself.
     */
    private WebView webView;

    /**
     * Indicates whether the page is being loaded right now.
     */
    private boolean isPageLoadingInProgress;

    /**
     * Progress bar status.
     */
    private int mProgress;

    /**
     * Left background image tile.
     */
    private ImageView mLeftEdge;

    /**
     * Right background image tile.
     */
    private ImageView mRightEdge;

    /**
     * Default constructor.
     * 
     * @param spec {@linkplain TabSpec} to create a tab.
     * @param cButton Close button view.
     * @param wView WebView instance which constitutes tab content.
     * @param tabLabel Label of a tab.
     * @param mLeftEdge Background left edge tile.
     * @param mRightEdge Background right edge tile.
     */
    public TabInfo(TabSpec spec, View cButton, WebView wView, TextView tabLabel, ImageView leftEdge, ImageView rightEdge) {
        tabSpec = spec;
        closeButton = cButton;
        webView = wView;
        label = tabLabel;
        isPageLoadingInProgress = false;
        mProgress = 0;
        mLeftEdge = leftEdge;
        mRightEdge = rightEdge;
    }

    /**
     * Retrieves TabSpec.
     * 
     * @return TabSpec instance.
     */
    public TabSpec getTabSpec() {
        return tabSpec;
    }

    /**
     * Retrieves close button view instance.
     * 
     * @return Close button view instance.
     */
    public View getCloseButton() {
        return closeButton;
    }

    /**
     * Retrieves tab label {@linkplain TextView} instance.
     * 
     * @return Tab label.
     */
    public TextView getLabel() {
        return label;
    }

    /**
     * Retrieves tab {@linkplain WebView} instance.
     * 
     * @return Tab webView.
     */
    public WebView getWebView() {
        return webView;
    }

    /**
     * Retrieves page loading status flag.
     * 
     * @return <code>True</code> if page is being loaded, <code>false</code> otherwise.
     */
    public boolean isPageLoadingInProgress() {
        return isPageLoadingInProgress;
    }

    /**
     * Sets page loading status flag.
     * 
     * @param isPageLoadingInProgress Flag defining if page is being loaded or not.
     */
    public void setPageLoadingInProgress(boolean isPageLoadingInProgress) {
        this.isPageLoadingInProgress = isPageLoadingInProgress;
    }

    /**
     * Retrieves progress numeric status.
     * 
     * @return Progress value (range 0-100).
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * Sets progress numeric value (range 0-100).
     * 
     * @param Progress value to set.
     */
    public void setProgress(int progress) {
        mProgress = progress;
    }

    /**
     * Retrieves tab background left edge tile.
     * 
     * @return Left edge {@linkplain ImageView} tile.
     */
    public ImageView getLeftEdge() {
        return mLeftEdge;
    }

    /**
     * Retrieves tab background right edge tile.
     * 
     * @return Right edge {@linkplain ImageView} tile.
     */
    public ImageView getRightEdge() {
        return mRightEdge;
    }
}