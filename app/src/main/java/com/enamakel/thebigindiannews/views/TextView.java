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

package com.enamakel.thebigindiannews.views;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.enamakel.thebigindiannews.NewsApplication;


public class TextView extends AppCompatTextView {
    public TextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (!isInEditMode()) setTypeface(NewsApplication.TYPE_FACE);
        try {
            int style = attrs.getAttributeIntValue(
                    "http://schemas.android.com/apk/res/android",
                    "textStyle",
                    Typeface.NORMAL);

            if (style == Typeface.BOLD) setTypeface(NewsApplication.TYPE_FACE_BOLD, Typeface.BOLD);
        } catch (Exception e) {

        }
    }
}
