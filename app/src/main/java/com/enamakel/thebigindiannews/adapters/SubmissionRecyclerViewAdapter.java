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

package com.enamakel.thebigindiannews.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.activities.ThreadPreviewActivity;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.widgets.SubmissionViewHolder;

public class SubmissionRecyclerViewAdapter extends ItemRecyclerViewAdapter<SubmissionViewHolder> {
    final StoryModel[] mItems;


    public SubmissionRecyclerViewAdapter(ItemManager itemManager, @NonNull StoryModel[] items) {
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
    protected StoryModel getItem(int position) {
        return mItems[position];
    }


    @Override
    protected void bind(final SubmissionViewHolder holder, final StoryModel item) {
        super.bind(holder, item);
        if (item == null) return;

        final boolean isComment = false; //TextUtils.equals(item.getType(), ItemManager.Item.COMMENT_TYPE);
        holder.postedTextView.setText(item.getDisplayedTime(context, false, false));
        if (isComment) {
            holder.titleTextView.setText(null);
            holder.commentButton.setText(R.string.view_thread);
        } else {
//            holder.postedTextView.append(" - ");
//            holder.postedTextView.append(context.getResources()
//                    .getQuantityString(R.plurals.score, item.getScore(), item.getScore()));
            holder.titleTextView.setText(item.getTitle());
            holder.commentButton.setText(R.string.view_story);
        }
        holder.titleTextView.setVisibility(holder.titleTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        holder.contentTextView.setVisibility(holder.contentTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        holder.commentButton.setVisibility(item.isDeleted() ? View.GONE : View.VISIBLE);
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isComment) openPreview(item);
                else openItem(item);
            }
        });
    }


    void openItem(StoryModel item) {
        context.startActivity(new Intent(context, SingleStoryActivity.class)
                .putExtra(SingleStoryActivity.EXTRA_ITEM, item));
    }


    void openPreview(StoryModel item) {
        context.startActivity(new Intent(context, ThreadPreviewActivity.class)
                .putExtra(ThreadPreviewActivity.EXTRA_ITEM, item));
    }
}
