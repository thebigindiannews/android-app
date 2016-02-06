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


import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.base.BaseStoriesActivity;
import com.enamakel.thebigindiannews.data.providers.managers.ItemManager;
import com.enamakel.thebigindiannews.data.clients.FetchMode;


public class ListActivity extends BaseStoriesActivity {
    @Override
    protected String getDefaultTitle() {
        return getString(R.string.title_activity_list);
    }


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        // Fork off a process to check the version of the app
    }


    @NonNull
    @Override
    protected String getFetchMode() {
        return ItemManager.TOP_FETCH_MODE;
    }


    @Override
    protected FetchMode getMode() {
        return FetchMode.TOP_STORIES;
    }


    @Override
    protected String getTrackingName() {
        return "Top Stories List";
    }
}
