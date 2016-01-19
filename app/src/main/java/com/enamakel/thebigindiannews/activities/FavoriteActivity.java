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

import com.enamakel.thebigindiannews.activities.parent.BaseListActivity;
import com.enamakel.thebigindiannews.fragments.FavoriteFragment;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.providers.MaterialisticProvider;

public class FavoriteActivity extends BaseListActivity {
    public static final String EMPTY_QUERY = MaterialisticProvider.class.getName();
    private static final String STATE_FILTER = "state:filter";
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (FavoriteManager.isRemoved(uri)) {
                ItemManager.WebItem selected = getSelectedItem();
                if (selected != null &&
                        TextUtils.equals(selected.getId(), uri.getLastPathSegment())) {
                    onItemSelected(null);
                }
            } else if (FavoriteManager.isCleared(uri)) {
                onItemSelected(null);
            }
        }
    };
    private String mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilter = savedInstanceState.getString(STATE_FILTER);
            getSupportActionBar().setSubtitle(mFilter);
        }
        getContentResolver().registerContentObserver(MaterialisticProvider.URI_FAVORITE,
                true, mObserver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(SearchManager.QUERY)) {
            return;
        }
        onItemSelected(null);
        mFilter = intent.getStringExtra(SearchManager.QUERY);
        if (TextUtils.equals(mFilter, EMPTY_QUERY)) {
            mFilter = null;
        }
        getSupportActionBar().setSubtitle(mFilter);
        FavoriteFragment fragment = (FavoriteFragment) getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.filter(mFilter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_favorite);
    }

    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(FavoriteFragment.EXTRA_FILTER, mFilter);
        return Fragment.instantiate(this, FavoriteFragment.class.getName(), args);
    }

    @Override
    protected boolean isSearchable() {
        return false;
    }
}
