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

package com.enamakel.thebigindiannews.views.preference;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.widgets.AsteriskSpan;

public class HelpListView extends ScrollView {
    public HelpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(LayoutInflater.from(context).inflate(R.layout.include_help_list_view, this, false));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ((TextView) findViewById(R.id.item_new).findViewById(R.id.rank))
                .append(makeAsteriskSpan());
        ((TextView) findViewById(R.id.item_promoted).findViewById(R.id.rank))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.greenA700));
        Button comments = (Button) findViewById(R.id.item_new_comments).findViewById(R.id.comment);
        SpannableStringBuilder sb = new SpannableStringBuilder(comments.getText());
        sb.append(makeAsteriskSpan());
        comments.setText(sb);
    }

    Spannable makeAsteriskSpan() {
        SpannableString sb = new SpannableString("*");
        sb.setSpan(new AsteriskSpan(getContext()), sb.length() - 1, sb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}
