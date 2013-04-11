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

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.data.URLCollection;
import com.msopentech.applicationgateway.utils.LocalPersistence;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements bookmarks preferences.
 */
public class BookmarksPreferences {

    /**
     * The name of the file where the bookmarks are saved.
     */
    private static final String FILENAME = "com.msopentech.applicationgateway.bookmarks";

    /**
     * Stores a bookmarks {@linkplain URLCollection} in a file.
     * 
     * @param boormarks A {@linkplain URLCollection} which contains bookmarks.
     * @param agentId Proxy Agent ID.
     */
    public static void storeBookmarks(URLCollection agentBookmarks, String agentId){
        try{
            if(agentBookmarks == null || TextUtils.isEmpty(agentId)) return;

            Map<String, URLCollection> bookmarks = loadBookmarks();
            if(bookmarks == null) bookmarks = new HashMap<String, URLCollection>();
            bookmarks.put(agentId, agentBookmarks);            
            LocalPersistence.writeObjectToFile(ApplicationGateway.getAppContext(), bookmarks, FILENAME);
        } catch(Exception e) {
            Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns a bookmarks {@linkplain URLCollection} for a specific Agent from from a file.
     * 
     * @param agentId Proxy Agent ID.
     * 
     * @return Initialized {@linkplain URLCollection} instance. In case of exception returns <code>null</code>.
     */
    public static URLCollection loadBookmarks(String agentId) {
        URLCollection agentBookmarks = null;
        try{
            if(TextUtils.isEmpty(agentId)) return null;

            Map<String, URLCollection> bookmarks = loadBookmarks();
            if(bookmarks != null && !bookmarks.isEmpty()) {
                agentBookmarks = bookmarks.get(agentId);
            }
        } catch(Exception e) {
            Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
        }
        return agentBookmarks;
    }

    /**
     * Returns a bookmarks {@linkplain Map} from from a file.
     * 
     * @return Initialized {@linkplain Map} instance. In case of exception returns <code>null</code>.
     */    
    @SuppressWarnings("unchecked")
    public static Map<String, URLCollection> loadBookmarks() {
        Map<String, URLCollection> bookmarks = null;
        try{
            bookmarks = (Map<String, URLCollection>) LocalPersistence.readObjectFromFile(ApplicationGateway.getAppContext(), FILENAME);
        } catch(Exception e) {
            Utility.showAlertDialog(e.toString(), ApplicationGateway.getAppContext());
        }
        return bookmarks;
    }

    /**
     *  A private constructor to prevent creating new instance of the class.
     */
    private BookmarksPreferences(){}
}
