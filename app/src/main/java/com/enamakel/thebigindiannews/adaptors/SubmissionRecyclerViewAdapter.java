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

package com.enamakel.thebigindiannews.adaptors;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.activities.ItemActivity;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.ThreadPreviewActivity;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.widget.SubmissionViewHolder;

public class SubmissionRecyclerViewAdapter extends ItemRecyclerViewAdapter<SubmissionViewHolder> {
    private final ItemManager.Item[] mItems;

    public SubmissionRecyclerViewAdapter(ItemManager itemManager, @NonNull ItemManager.Item[] items) {
        super(itemManager);
        mItems = items;
    }

    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SubmissionViewHolder(layoutInflater.inflate(R.layout.item_submission, parent, false));
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    @Override
    protected ItemManager.Item getItem(int position) {
        return mItems[position];
    }

    @Override
    protected void bind(final SubmissionViewHolder holder, final ItemManager.Item item) {
        super.bind(holder, item);
        if (item == null) {
            return;
        }
        final boolean isComment = TextUtils.equals(item.getType(), ItemManager.Item.COMMENT_TYPE);
        holder.postedTextView.setText(item.getDisplayedTime(context, false, false));
        if (isComment) {
            holder.mTitleTextView.setText(null);
            holder.commentButton.setText(R.string.view_thread);
        } else {
            holder.postedTextView.append(" - ");
            holder.postedTextView.append(context.getResources()
                    .getQuantityString(R.plurals.score, item.getScore(), item.getScore()));
            holder.mTitleTextView.setText(item.getDisplayedTitle());
            holder.commentButton.setText(R.string.view_story);
        }
        holder.mTitleTextView.setVisibility(holder.mTitleTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        holder.contentTextView.setVisibility(holder.contentTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        holder.commentButton.setVisibility(item.isDeleted() ? View.GONE : View.VISIBLE);
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isComment) {
                    openPreview(item);
                } else {
                    openItem(item);
                }
            }
        });
    }

    private void openItem(ItemManager.Item item) {
        context.startActivity(new Intent(context, ItemActivity.class)
                .putExtra(ItemActivity.EXTRA_ITEM, item));
    }

    private void openPreview(ItemManager.Item item) {
        context.startActivity(new Intent(context, ThreadPreviewActivity.class)
                .putExtra(ThreadPreviewActivity.EXTRA_ITEM, item));
    }
}
