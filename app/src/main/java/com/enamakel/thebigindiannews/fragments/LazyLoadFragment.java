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

package com.enamakel.thebigindiannews.fragments;

import android.os.Bundle;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.Preferences;

/**
 * Base fragment that controls load timing depends on WIFI and visibility
 */
public abstract class LazyLoadFragment extends BaseFragment {
    private static final String STATE_EAGER_LOAD = "state:eagerLoad";
    private boolean mEagerLoad;
    private boolean mActivityCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mEagerLoad = savedInstanceState.getBoolean(STATE_EAGER_LOAD);
        } else {
            mEagerLoad = !Preferences.shouldLazyLoad(getActivity()) && AppUtils.isOnWiFi(getContext());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !mEagerLoad) {
            mEagerLoad = true;
            eagerLoad();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivityCreated = true;
        eagerLoad();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_EAGER_LOAD, mEagerLoad);
    }

    /**
     * Load data after fragment becomes visible or if WIFI is enabled
     */
    protected abstract void load();

    protected final void eagerLoad() {
        if (!mEagerLoad) {
            return;
        }
        if (!mActivityCreated) {
            return;
        }
        load();
    }
}
