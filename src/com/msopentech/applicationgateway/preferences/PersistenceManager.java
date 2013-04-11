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

import java.util.Arrays;
import java.util.Vector;

import com.msopentech.applicationgateway.ApplicationGateway;
import com.msopentech.applicationgateway.data.URLCollection;
import com.msopentech.applicationgateway.data.URLInfo;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements a wrapper for the user's data. 
 */
public class PersistenceManager {
    /**
     * Enumerates the types of the user's content.
     */
    public enum ContentType {
        HISTORY, BOOKMARKS
    }

    /**
     * An interface that all observers must implement to be notified of any changes in sSuggestionsStorage.
     */
    public interface PersistenceObserver {
        void onSuggestionsListChanged();
    }

    /**
     * A memory storage for bookmarks.
     */
    private static Vector<URLInfo> sBookmarks;

    /**
     * A memory storage for history.
     */
    private static Vector<URLInfo> sHistory;

    /**
     * A composite storage for history and bookmarks used for suggestions.
     */
    private static Vector<String> sSuggestionsStorage = new Vector<String>();

    /**
     * An observing activity.
     */
    private static PersistenceObserver sHost;

    /**
     * An observing activity.
     */
    private static String sAgentId;    

    /**
     * A static initializer to instantiate variables that will store persisted data.
     */
    static {
        initialize(null);
    }

    /**
     * A static initializer to read the bookmarks stored in the file based on provided IUD of the Agent..
     * 
     * @param agentId Proxy Agent ID.
     */
    public static void initialize(String agentId) {
        try {
            sAgentId = agentId;
            URLCollection bookmarksContainer = BookmarksPreferences.loadBookmarks(sAgentId);
            URLCollection historyContainer = HistoryPreferences.loadHistory();
            if (bookmarksContainer == null) {
                sBookmarks = new Vector<URLInfo>();
            } else {
                sBookmarks = new Vector<URLInfo>(Arrays.asList(bookmarksContainer.getData()));
            }

            if (historyContainer == null) {
                sHistory = new Vector<URLInfo>();
            } else {
                sHistory = new Vector<URLInfo>(Arrays.asList(historyContainer.getData()));
            }

            if (!sBookmarks.isEmpty()) {
                for (int i = 0; i < sBookmarks.size(); i++) {
                    sSuggestionsStorage.add(sBookmarks.elementAt(i).getUrlAddress());
                }
            }

            if (!sHistory.isEmpty()) {
                String url;
                for (int i = 0; i < sHistory.size(); i++) {
                    url = sHistory.elementAt(i).getUrlAddress();
                    if (!sSuggestionsStorage.contains(url)) sSuggestionsStorage.add(url);
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".static block: Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * The incoming URL may have a prefix like "www", "wap" or some other. It must be deleted before adding the record. The worst case may
     * be like http://www.yandex.net.ru/. WebViewClient always adds '/' after the URL.
     * 
     * @param url Url to format.
     * @return Formatted url.
     */
    private static String handleUrlPrefix(String url) {
        try {
            int deleteFrom = url.indexOf("://");
            if (deleteFrom == -1) {
                return url;
            }

            deleteFrom += 3;

            // Ensures that no "www" will be deleted after the terminating '/'
            int deletionLimit = url.indexOf('/', deleteFrom);

            if (deletionLimit == -1) {
                return url;
            }

            int deleteTo = url.indexOf("www.", deleteFrom);

            // if the string has "www" after the limit or doesn't have it at all, the url will be left intact.
            if (deleteTo != deleteFrom || deleteTo >= deletionLimit) {
                return url;
            }

            // Now it is safe to replace "www" with nothing.
            return url.replaceFirst("www.", "");
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".handleUrlPrefix(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
            return url;
        }
    }

    /**
     * Adds a record into the specified logical storage.
     * 
     * @param what Specifies the type of data to be stored.
     * @param data The data to store.
     */
    public static void addRecord(ContentType what, URLInfo data) {
        try {
            switch (what) {
                case HISTORY: {
                    String urlToAdd = data.getUrlAddress();
                    if(urlToAdd == null || urlToAdd.isEmpty()) {
                        return;
                    }
        
                    String formattedUrl = handleUrlPrefix(urlToAdd);
        
                    if (!sHistory.isEmpty() && formattedUrl.equals(sHistory.elementAt(sHistory.size() - 1).getUrlAddress())) {
                        return;
                    }
                    
                    data.setUrlAddress(formattedUrl);
                    sHistory.add(data);
                    break;
                }
                case BOOKMARKS: {
                    sBookmarks.add(data);
                    break;
                }
                default: {
                    return;
                }
            }
            updateRecordsInFile(what);
            updateSuggestionsStorage(data.getUrlAddress(), false);
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".addRecord(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns all records from the specified logical storage.
     * 
     * @param what Specifies the type of data to return.
     * @return Requested data, converted into an array.
     */
    public static URLInfo[] getAllRecords(ContentType what) {
        URLInfo[] values = null;
        try {
            switch (what) {
                case HISTORY: {
                    values = new URLInfo[sHistory.size()];
                    sHistory.toArray(values);
                    break;
                }
                case BOOKMARKS: {
                    values = new URLInfo[sBookmarks.size()];
                    sBookmarks.toArray(values);
                    break;
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".getAllRecords(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
        return values;
    }

    /**
     * Updates the storage ensuring it always has unique items.
     * 
     * @param url A new url.
     * @param onDeletion false to add an item, true to delete it.
     */
    private static void updateSuggestionsStorage(String url, boolean onDeletion) {
        try {
            int elementIndex = sSuggestionsStorage.indexOf(url);

            if (onDeletion) {
                if (elementIndex != -1) {
                    sSuggestionsStorage.remove(elementIndex);
                    sHost.onSuggestionsListChanged();
                }
            } else {
                if (elementIndex == -1) {
                    sSuggestionsStorage.add(url);
                    sHost.onSuggestionsListChanged();
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".updateMergedStorage(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Returns merged records from both storages.
     * 
     * @return Requested data, converted into an array.
     */
    public static Vector<String> getAllMergedRecords() {
        return sSuggestionsStorage;
    }

    /**
     * Deletes a record from the specified storage.
     * 
     * @param what Specifies the logical storage that undergoes the operation.
     * @param url Object to delete.
     */
    public static void deleteRecordByUrl(ContentType what, URLInfo url) {
        try {
            switch (what) {
                case HISTORY: {
                    sHistory.remove(url);
                    break;
                }
                case BOOKMARKS: {
                    sBookmarks.remove(url);
                    break;
                }
                default: {
                    return;
                }
            }
            updateRecordsInFile(what);
            updateSuggestionsStorage(url.getUrlAddress(), true);
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".deleteRecordByUrl(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Stores an instance of observer.
     * 
     * @param host
     */
    public static void registerObserver(PersistenceObserver host) {
        sHost = host;
    }

    /**
     * Deletes all contents in the specified logical storage.
     * 
     * @param what Specifies the logical storage that undergoes the operation.
     */
    public static void dropContent(ContentType what) {
        try {
            Vector<URLInfo> affectedStorage;
            switch (what) {
                case HISTORY: {
                    affectedStorage = sHistory;
                    break;
                }
                case BOOKMARKS: {
                    affectedStorage = sBookmarks;
                    break;
                }
                default: {
                    return;
                }
            }

            String url;
            int elementIndex;
            for (int i = 0; i < affectedStorage.size(); i++) {
                url = affectedStorage.elementAt(i).getUrlAddress();
                elementIndex = sSuggestionsStorage.indexOf(url);
                if (elementIndex != -1) {
                    sSuggestionsStorage.remove(elementIndex);
                }
            }
            affectedStorage.clear();
            sHost.onSuggestionsListChanged();
            updateRecordsInFile(what);
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".dropContent(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }

    /**
     * Updates the data in the file when it becomes "dirty".
     */
    private static void updateRecordsInFile(ContentType what) {
        try {
            URLInfo[] values;
            switch (what) {
                case HISTORY: {
                    values = new URLInfo[sHistory.size()];
                    sHistory.toArray(values);
                    HistoryPreferences.storeHistory(new URLCollection(values));
                    break;
                }
                case BOOKMARKS: {
                    values = new URLInfo[sBookmarks.size()];
                    sBookmarks.toArray(values);
                    BookmarksPreferences.storeBookmarks(new URLCollection(values), sAgentId);
                    break;
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(PersistenceManager.class.getSimpleName() + ".updateRecordsInFile(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }
    }
}
