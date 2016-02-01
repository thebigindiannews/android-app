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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enamakel.thebigindiannews.Application;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.util.FontCache;


public class FontPreference extends SpinnerPreference {
    private final LayoutInflater layoutInflater;


    public FontPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public FontPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layoutInflater = LayoutInflater.from(getContext());
    }


    @Override
    protected View createDropDownView(int position, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
    }


    @Override
    protected void bindDropDownView(int position, View view) {
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTypeface(getFont(entryValues[position]));
        textView.setText(entries[position]);
    }


    @Override
    protected boolean persistString(String value) {
        Log.d("font", value);
        Application.TYPE_FACE = getFont(value);
        Application.TYPE_FACE_BOLD = getFont(value + "-bold");
        return super.persistString(value);
    }


    Typeface getFont(String name) {
        String fontName = name + ".tff";
        return FontCache.getInstance().get(getContext(), fontName);
    }
}
