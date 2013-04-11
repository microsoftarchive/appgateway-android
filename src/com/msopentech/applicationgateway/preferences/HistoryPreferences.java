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

import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.data.URLCollection;
import com.msopentech.applicationgateway.utils.LocalPersistence;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements history preferences.
 */
public class HistoryPreferences {

    /**
     * The name of the file where the history is saved.
     */
    private static final String FILENAME = "com.msopentech.applicationgateway.history";

    /**
     * Stores a history {@linkplain URLCollection} in a file.
     * 
     * @param boormarks A {@linkplain URLCollection} which contains history.
     */
    public static void storeHistory(URLCollection bookmarks){
        try{
            LocalPersistence.writeObjectToFile(ApplicationGateway.getAppContext(), bookmarks, FILENAME);
        } catch(Exception e) {
            Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns a history {@linkplain URLCollection} from from a file.
     * 
     * @return Initialized {@linkplain URLCollection} instance. In case of exception returns <code>null</code>.
     */
    public static URLCollection loadHistory() {
        URLCollection history = null;
        try{
            history = (URLCollection)LocalPersistence.readObjectFromFile(ApplicationGateway.getAppContext(), FILENAME);
        } catch(Exception e) {
            Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
        }
        return history;
    }

    /**
     *  A private constructor to prevent creating new instance of the class.
     */
    private HistoryPreferences(){}
}
