/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enamakel.thebigindiannews.activities;


import android.app.SearchManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.base.BaseListActivity;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.fragments.FavoriteFragment;


public class FavoriteActivity extends BaseListActivity {
    public static final String EMPTY_QUERY = BigIndianProvider.class.getName();

    static final String TAG = FavoriteActivity.class.getName();
    static final String STATE_FILTER = "state:filter";

    final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG + ":contentObserver", uri.toString());

            if (FavoriteManager.isRemoved(uri)) {
                StoryModel storyModel = getSelectedItem();

                if (storyModel != null &&
                        TextUtils.equals(storyModel.getId(), uri.getLastPathSegment()))
                    onItemSelected(null);

            } else if (FavoriteManager.isCleared(uri)) onItemSelected(null);
        }
    };

    String filter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(STATE_FILTER);
            getSupportActionBar().setSubtitle(filter);
        }

        getContentResolver().registerContentObserver(BigIndianProvider.URI_FAVORITE,
                true, contentObserver);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(SearchManager.QUERY)) return;

        onItemSelected(null);
        filter = intent.getStringExtra(SearchManager.QUERY);

        if (TextUtils.equals(filter, EMPTY_QUERY)) filter = null;

        getSupportActionBar().setSubtitle(filter);
        FavoriteFragment fragment = (FavoriteFragment) getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAGMENT_TAG);

        if (fragment != null) fragment.filter(filter);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contentObserver);
    }


    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_favorite);
    }


    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(FavoriteFragment.EXTRA_FILTER, filter);
        return Fragment.instantiate(this, FavoriteFragment.class.getName(), args);
    }


    @Override
    protected boolean isSearchable() {
        return false;
    }


    @Override
    protected String getTrackingName() {
        return "Favoirites page";
    }
}