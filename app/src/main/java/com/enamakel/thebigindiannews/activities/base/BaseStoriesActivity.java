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

package com.enamakel.thebigindiannews.activities.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.clients.HackerNewsClient;
import com.enamakel.thebigindiannews.fragments.ListFragment;

public abstract class BaseStoriesActivity extends BaseListActivity
        implements ListFragment.RefreshCallback {
    static final String STATE_LAST_UPDATED = "state:lastUpdated";
    Long lastUpdated;

    final Runnable lastUpdateTask = new Runnable() {
        @Override
        public void run() {
            if (lastUpdated == null) return;
            getSupportActionBar().setSubtitle(getString(R.string.last_updated,
                    DateUtils.getRelativeTimeSpanString(lastUpdated,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL)));
            handler.postAtTime(this, SystemClock.uptimeMillis() + DateUtils.MINUTE_IN_MILLIS);
        }
    };

    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        if (instanceState != null) lastUpdated = instanceState.getLong(STATE_LAST_UPDATED);
    }


    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(lastUpdateTask);
        handler.post(lastUpdateTask);
    }


    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(lastUpdateTask);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastUpdated != null) outState.putLong(STATE_LAST_UPDATED, lastUpdated);
    }


    @Override
    public void onRefreshed() {
        onItemSelected(null);
        lastUpdated = System.currentTimeMillis();
        handler.removeCallbacks(lastUpdateTask);
        handler.post(lastUpdateTask);
    }


    @NonNull
    @ItemManager.FetchMode
    protected abstract String getFetchMode();


    @Override
    protected Fragment instantiateListFragment() {
        Bundle args = new Bundle();
        args.putString(ListFragment.EXTRA_ITEM_MANAGER, HackerNewsClient.class.getName());
        args.putString(ListFragment.EXTRA_FILTER, getFetchMode());
        return Fragment.instantiate(this, ListFragment.class.getName(), args);
    }
}