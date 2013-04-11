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

/**
 *  Implements a wrapper for the URL data.
 */
public class URLInfo implements Serializable{

    /**
     * UID required for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stored URL address.
     */
    private String mUrlAddress;

    /**
     * Stored page name;
     */
    private String mPageName;

    /**
     * A class constructor.
     * 
     * @param urlAddress URL address.
     * @param pageName Page name.
     */
    public URLInfo(String urlAddress, String pageName) {
        mUrlAddress = urlAddress;
        mPageName = pageName;
    }

    /**
     * Converts the object into a string, returning the stored address.
     */
    public String toString() {
        return mUrlAddress;
    }

    /**
     * Returns URL address.
     * 
     * @return URL address.
     */
    public String getUrlAddress() {
        return mUrlAddress;
    }

    /**
     * Returns page title.
     * 
     * @return Page title.
     */
    public String getPageName() {
        return mPageName;
    }

    /**
     * Sets URL address.
     * 
     * @param url Address to set.
     */
    public void setUrlAddress(String url) {
        mUrlAddress = url;
    }
}
