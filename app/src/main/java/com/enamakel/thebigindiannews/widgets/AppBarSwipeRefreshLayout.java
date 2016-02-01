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

package com.enamakel.thebigindiannews.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.enamakel.thebigindiannews.R;


/**
 * Minor extension to {@link SwipeRefreshLayout} that is appbar-aware, only enabling when appbar
 * has been fully expanded.
 *
 * This class assumes activity layout contains an {@link AppBarLayout} with {@link R.id#appbar}
 */
public class AppBarSwipeRefreshLayout extends SwipeRefreshLayout
        implements AppBarLayout.OnOffsetChangedListener {
    AppBarLayout appBar;


    public AppBarSwipeRefreshLayout(Context context) {
        super(context);
    }


    public AppBarSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getContext() instanceof Activity) {
            appBar = (AppBarLayout) ((Activity) getContext()).findViewById(R.id.appbar);
            if (appBar != null) appBar.addOnOffsetChangedListener(this);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        if (appBar != null) {
            appBar.removeOnOffsetChangedListener(this);
            appBar = null;
        }

        super.onDetachedFromWindow();
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        this.setEnabled(i == 0);
    }
}