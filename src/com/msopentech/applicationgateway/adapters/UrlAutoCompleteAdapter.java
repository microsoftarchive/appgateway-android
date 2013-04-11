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

package com.msopentech.applicationgateway.adapters;

import java.util.Vector;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/**
 * A custom array adapter that handles mSuggestions for URLs.
 */
public class UrlAutoCompleteAdapter extends ArrayAdapter<String> {
    
    /**
     * Provides a way to track custom logic behind owner View lifecycle.
     */
    public interface IPublishResultsNotifiable {
        /**
         * Indicates whether adapter should publish results or not.
         * 
         * @return <code>True</code> if results should be published, <code>false</code> otherwise.
         */
        boolean shouldPublishResults();
    }
    
    /**
     * A vector that contains an actual set of mSuggestions. It is updated each time the content of mUrlEditTextView is changed.
     */
    private Vector<String> mSuggestions;
    /**
     * A data reference which is used to populate mSuggestions. It is never allocated internally and always refers to the merged storage of
     * PersistenceManager.
     */
    private Vector<String> mOriginalData;

    /**
     * View that utilizes this adapter.
     */
    private IPublishResultsNotifiable mOwner;

    /**
     * A class constructor.
     */
    public UrlAutoCompleteAdapter(Context context, int textViewResourceId, Vector<String> objects, IPublishResultsNotifiable owner) {
        super(context, textViewResourceId, objects);
        mOriginalData = objects;
        mSuggestions = new Vector<String>();
        mOwner = owner;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * An instance of Filter for handling mSuggestions.
     */
    Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null && mOwner != null && mOwner.shouldPublishResults()) {
                mSuggestions.clear();
                for (String url : mOriginalData) {
                    if (url.contains(constraint)) {
                        mSuggestions.add(url);
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mSuggestions;
                filterResults.count = mSuggestions.size();

                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
			Vector<String> res = (Vector<String>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (String c : res) {
                    add(c);
                }

                if(mOwner != null && mOwner.shouldPublishResults()) {
                    notifyDataSetChanged();
                }
            }
        }
    };
}
