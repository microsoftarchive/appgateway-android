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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.msopentech.applicationgateway.AgentsActivity;
import com.msopentech.applicationgateway.R;
import com.msopentech.applicationgateway.data.URLInfo;

/**
 * Implements adapter for {@link SimpleAdapter} that is used do tisplay bookmarks and history items within {@link AgentsActivity}.
 */
public class BookmarksAdapter extends ArrayAdapter<URLInfo> {

    /**
     * Inflater to get it once for further usage
     */
    private LayoutInflater mInflater;

    /**
     * Resource for the item to process
     */
    private int mItemResource;

    public BookmarksAdapter(Context context, List<URLInfo> data, int resource) {
        super(context, resource, data);
        try {
            mInflater = LayoutInflater.from(context);
            mItemResource = resource;
        } catch (Exception e) {}
    }

    public void setViewText(TextView view, String text) {
        try {
            if (view != null && text != null) {
                view.setText(text);
            }
        } catch (Exception e) {}
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(mItemResource, null);

                holder = new ViewHolder();
                holder.url = (TextView) convertView.findViewById(R.id.bookmarks_item_url);
                holder.title = (TextView) convertView.findViewById(R.id.bookmarks_item_text);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            URLInfo item = (URLInfo) getItem(position);
            if (item != null) {
                setViewText(holder.url, item.getUrlAddress());
                setViewText(holder.title, item.getPageName());
            }
        } catch (Exception e) {}
        return convertView;
    }

    /**
     * Stores inflated view components to prevent repetitive reinflation each time a view is requested <br/>
     * It will be stored in a view tag
     */
    static class ViewHolder {
        TextView title;
        TextView url;
    }
}
