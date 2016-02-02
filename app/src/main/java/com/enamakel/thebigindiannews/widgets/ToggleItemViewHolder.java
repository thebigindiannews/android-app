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

import android.view.View;
import android.widget.TextView;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.adapters.MultiPageItemRecyclerViewAdapter;

public class ToggleItemViewHolder extends MultiPageItemRecyclerViewAdapter.ItemViewHolder {
    public final TextView toggle;
    public final View level;
    public final TextView parent;


    public ToggleItemViewHolder(View itemView) {
        super(itemView);
        toggle = (TextView) itemView.findViewById(R.id.toggle);
        level = itemView.findViewById(R.id.level);
        parent = (TextView) itemView.findViewById(R.id.parent);
        AppUtils.setHtmlText(parent, parent.getResources().getString(R.string.parent));
    }
}
