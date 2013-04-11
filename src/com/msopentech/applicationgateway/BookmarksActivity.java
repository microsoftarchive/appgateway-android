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

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.msopentech.applicationgateway.adapters.BookmarksAdapter;
import com.msopentech.applicationgateway.data.URLInfo;
import com.msopentech.applicationgateway.preferences.PersistenceManager;
import com.msopentech.applicationgateway.utils.Utility;

/**
 * Implements bookmarks management activity.
 */
public class BookmarksActivity extends Activity {

    /**
     * List containing browser history.
     */
    ListView mHistoryListView  = null;

    /**
     * List containing user bookmarks.
     */	
    ListView mBookmarksListView  = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_list);
        try {
            this.display(true);
        } catch (final Exception e) {
            Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + ".onCreate(): Failed. " + e.toString(), this);		    
        }
    }

    /**
     * Finishes activity and provides selected URL as {@link EnterpriseBrowserActivity#EXTRAS_URL_KEY} String item within intent extras.
     * 
     * @param url URL string to be returned as an activity result.
     */
    private void finish(String url) {
        Intent resultIntent = getIntent();
        if (null != url && !url.contentEquals("")) {
            resultIntent.putExtra(EnterpriseBrowserActivity.EXTRAS_URL_KEY, url);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish(null);
        }
        return false; // Always allow...
    }	

    /**
     * Removes selected item from the favorites list. 
     * 
     * @param view Selected view.
     */
    public void onDeleteClick(View view) {
        try {
            int id = ((ViewGroup) (view.getParent()).getParent()).getId();

            switch (id) {
                case R.id.bookmark_history: {
                    int position = mHistoryListView.getPositionForView(view);
                    URLInfo toDelete = (URLInfo) mHistoryListView.getItemAtPosition(position);
                    PersistenceManager.deleteRecordByUrl(PersistenceManager.ContentType.HISTORY, toDelete);
                    break;
                }
                case R.id.bookmark_favorites: {
            int position = mBookmarksListView.getPositionForView(view);
            URLInfo toDelete = (URLInfo)mBookmarksListView.getItemAtPosition(position);
            PersistenceManager.deleteRecordByUrl(PersistenceManager.ContentType.BOOKMARKS, toDelete);
                    break;
                }
                default: {
                    return;
                }
            }
            display(false);
        } catch (final Exception e) {
            Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + ".onDeleteClick(): Failed. " + e.toString(), this);
        }
    }

    /**
     * Updates activity UI.
     * 
     * @param onCreate Flag to perform additional initialization in case UX is created for the first time (from onCreate).
     */
    private void display(boolean onCreate) {
        mHistoryListView = (ListView)this.findViewById(R.id.bookmark_history);
        mBookmarksListView = (ListView)this.findViewById(R.id.bookmark_favorites);

        if (onCreate) {
            RelativeLayout historyHeader = (RelativeLayout) findViewById(R.id.bookmark_history_header);
            RelativeLayout favoritesHeader = (RelativeLayout) findViewById(R.id.bookmark_bookmarks_header);

            TextView historyText = (TextView) historyHeader.findViewById(R.id.list_header);
            TextView bookmarksText = (TextView) favoritesHeader.findViewById(R.id.list_header);
            historyText.setText(getResources().getString(R.string.bookmarks_header_history));
            bookmarksText.setText(getResources().getString(R.string.bookmarks_header_favorites));

            TextView deleteHistoryTextButton = (TextView) historyHeader.findViewById(R.id.list_header_action_button);
            TextView deleteFavoritesTextButton = (TextView) favoritesHeader.findViewById(R.id.list_header_action_button);
            deleteHistoryTextButton.setVisibility(View.VISIBLE);
            deleteFavoritesTextButton.setVisibility(View.VISIBLE);

            TextView doneButton = (TextView) findViewById(R.id.bookmark_done_button);

            deleteHistoryTextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        PersistenceManager.dropContent(PersistenceManager.ContentType.HISTORY);
                        display(false);
                    } catch (final Exception e) {
                        Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + "deleteHistoryTextButton on click listener failed. " + e.toString(), BookmarksActivity.this);
                    }
                }
            });

            deleteFavoritesTextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        PersistenceManager.dropContent(PersistenceManager.ContentType.BOOKMARKS);
                        display(false);
                    } catch (final Exception e) {
                        Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + "deleteHistoryTextButton on click listener failed. " + e.toString(), BookmarksActivity.this);
                    }
                }
            });
            doneButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    try {
                        finish(null);
                    } catch (final Exception e) {
                        Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + "deleteHistoryTextButton on click listener failed. " + e.toString(), BookmarksActivity.this);
                    }
                }
            });
        }

        URLInfo bookmarks[] = PersistenceManager.getAllRecords(PersistenceManager.ContentType.BOOKMARKS);
        URLInfo history[] = PersistenceManager.getAllRecords(PersistenceManager.ContentType.HISTORY);

        BookmarksAdapter historyAdapter = new BookmarksAdapter(this, Arrays.asList(history), R.layout.favorites_list_item);
        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(this, Arrays.asList(bookmarks), R.layout.favorites_list_item);

        mHistoryListView.setAdapter(historyAdapter);   
        mBookmarksListView.setAdapter(bookmarksAdapter);   

        if (onCreate) {
            mHistoryListView.setClickable(true);
            mBookmarksListView.setClickable(true);

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {	
                    try {
                        TextView itemText = (TextView) ((RelativeLayout) view).findViewById(R.id.bookmarks_item_url);
                        finish(itemText.getText().toString());
                    } catch (Exception e) {
                        Utility.showAlertDialog(BookmarksActivity.class.getSimpleName() + ".onItemClick(): Failed. " + e.toString(), BookmarksActivity.this);		    
                    }
                }
            };

            mHistoryListView.setOnItemClickListener(listener);
            mBookmarksListView.setOnItemClickListener(listener);
        }
    }
}
