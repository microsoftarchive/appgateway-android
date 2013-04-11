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

import com.msopentech.applicationgateway.utils.Utility;
import android.app.Application;
import android.content.Context;

/**
 * The application class, keeps stored application context.
 */
public class ApplicationGateway extends Application {
	/**
	 * A stored application context.
	 */
    private static Context mContext;

    public void onCreate(){
    	try {
        super.onCreate();
    		ApplicationGateway.mContext = getApplicationContext();
        } catch (Exception e) {
            Utility.showAlertDialog(ApplicationGateway.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), ApplicationGateway.this);
    	}
    }

    /**
     * Returns the stored application context.
     * 
     * @return The application context.
     */
    public static Context getAppContext() {
        return ApplicationGateway.mContext;
    }
}
