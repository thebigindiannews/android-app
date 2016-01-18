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

package com.enamakel.thebigindiannews.widget;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.activities.ItemActivity;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;

public class MultiPageItemRecyclerViewAdapter
        extends ItemRecyclerViewAdapter<ItemRecyclerViewAdapter.ItemViewHolder> {
    private final ItemManager.Item[] mItems;

    public MultiPageItemRecyclerViewAdapter(ItemManager itemManager,
                                            ItemManager.Item[] items) {
        super(itemManager);
        this.mItems = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mLayoutInflater.inflate(R.layout.item_comment, parent, false));
    }

    @Override
    protected ItemManager.Item getItem(int position) {
        return mItems[position];
    }

    @Override
    protected void bind(final ItemViewHolder holder, final ItemManager.Item item) {
        super.bind(holder, item);
        if (item == null) {
            return;
        }
        holder.mPostedTextView.setText(item.getDisplayedTime(mContext, false, true));
        if (item.getKidCount() > 0) {
            holder.mCommentButton.setText(mContext.getResources()
                    .getQuantityString(R.plurals.comments_count, item.getKidCount(), item.getKidCount()));
            holder.mCommentButton.setVisibility(View.VISIBLE);
            holder.mCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openItem(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    private void openItem(ItemManager.Item item) {
        mContext.startActivity(new Intent(mContext, ItemActivity.class)
                .putExtra(ItemActivity.EXTRA_ITEM, item)
                .putExtra(ItemActivity.EXTRA_OPEN_COMMENTS, true));
    }
}
