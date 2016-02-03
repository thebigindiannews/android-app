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


import android.content.Intent;
import android.support.annotation.NonNull;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.base.BaseStoriesActivity;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.clients.FetchMode;
import com.enamakel.thebigindiannews.fragments.ListFragment;


public class NewActivity extends BaseStoriesActivity {
    public static final String EXTRA_REFRESH = NewActivity.class.getName() + ".EXTRA_REFRESH";


    @Override
    protected FetchMode getMode() {
        return FetchMode.LATEST_STORIES;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_REFRESH, false)) {
            // triggered by new submission from user, refresh list
            ListFragment listFragment = ((ListFragment) getSupportFragmentManager()
                    .findFragmentByTag(LIST_FRAGMENT_TAG));
            if (listFragment != null) {
                listFragment.filter(getFetchMode());
            }
        }
    }


    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_new);
    }


    @NonNull
    @Override
    protected String getFetchMode() {
        return ItemManager.NEW_FETCH_MODE;
    }


    @Override
    protected String getTrackingName() {
        return "New Story Page";
    }
}
