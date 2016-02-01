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

package com.enamakel.thebigindiannews.preference;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceGroup;
import android.util.AttributeSet;

import com.enamakel.thebigindiannews.R;


public class PreferenceHelp extends PreferenceGroup {
    final int layoutResId;
    final String title;


    public PreferenceHelp(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.preferenceHelpStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferenceHelp);

        try {
            layoutResId = typedArray.getResourceId(R.styleable.PreferenceHelp_dialogLayout, 0);
            title = typedArray.getString(R.styleable.PreferenceHelp_dialogTitle);
        } finally {
            typedArray.recycle();
        }
    }


    @Override
    protected void onClick() {
        if (layoutResId == 0) return;
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(layoutResId)
                .setPositiveButton(R.string.got_it, null)
                .create()
                .show();
    }
}
