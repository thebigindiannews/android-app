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
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.enamakel.thebigindiannews.util.MenuTintDelegate;
import com.enamakel.thebigindiannews.util.Preferences;

public abstract class ThemedActivity extends AppCompatActivity {
    private final MenuTintDelegate mMenuTintDelegate = new MenuTintDelegate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences.Theme.apply(this, isDialogTheme());
        super.onCreate(savedInstanceState);
        mMenuTintDelegate.onActivityCreated(this);
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenuTintDelegate.onOptionsMenuCreated(menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected boolean isDialogTheme() {
        return false;
    }
}
