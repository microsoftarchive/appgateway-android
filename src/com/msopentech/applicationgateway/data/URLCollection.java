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
 * Implements wrapper over a collection of {@linkplain URLInfo} items.
 */
public class URLCollection implements Serializable {

    /**
     * Unique storage UUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Array of stored URLs.
     */
    private URLInfo mData[];

    /**
     * Default constructor.
     * 
     * @param data Array of {@linkplain URLInfo} items.
     */
    public URLCollection(URLInfo data[]) {
        mData = data;
    }

    /**
     * Returns an array of {@linkplain URLInfo} items.
     * 
     * @return Array of {@linkplain URLInfo} items.
     */
    public URLInfo[] getData(){
        return mData;
    }

}
