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
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.activities.ItemActivity;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.widget.SubmissionViewHolder;

public class ThreadPreviewRecyclerViewAdapter extends ItemRecyclerViewAdapter<SubmissionViewHolder> {
    private final List<ItemManager.Item> mItems = new ArrayList<>();
    private final List<String> mExpanded = new ArrayList<>();
    private int mLevelIndicatorWidth;
    private final String mUsername;

    public ThreadPreviewRecyclerViewAdapter(ItemManager itemManager, ItemManager.Item item) {
        super(itemManager);
        mItems.add(item);
        mUsername = item.getBy();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mLevelIndicatorWidth = AppUtils.getDimensionInDp(context, R.dimen.level_indicator_width);
    }

    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SubmissionViewHolder holder = new SubmissionViewHolder(layoutInflater
                .inflate(R.layout.item_submission, parent, false));
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                holder.itemView.getLayoutParams();
        params.leftMargin = mLevelIndicatorWidth * viewType;
        holder.itemView.setLayoutParams(params);
        holder.commentButton.setVisibility(View.GONE);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    protected void bind(SubmissionViewHolder holder, final ItemManager.Item item) {
        super.bind(holder, item);
        holder.postedTextView.setText(item.getDisplayedTime(context, false,
                !TextUtils.equals(item.getBy(), mUsername)));
        holder.moreButton.setVisibility(View.GONE);
        if (TextUtils.equals(item.getType(), ItemManager.Item.COMMENT_TYPE)) {
            holder.mTitleTextView.setText(null);
            holder.itemView.setOnClickListener(null);
            holder.commentButton.setVisibility(View.GONE);
        } else {
            holder.mTitleTextView.setText(item.getDisplayedTitle());
            holder.commentButton.setVisibility(View.VISIBLE);
            holder.commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openItem(item);
                }
            });
        }
        holder.mTitleTextView.setVisibility(holder.mTitleTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        holder.contentTextView.setVisibility(holder.contentTextView.length() > 0 ?
                View.VISIBLE : View.GONE);
        if (!mExpanded.contains(item.getId()) && item.getParentItem() != null) {
            mExpanded.add(item.getId());
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mItems.add(0, item.getParentItem()); // recursive
                    notifyItemRangeChanged(1, mItems.size());
                    notifyItemInserted(0);
                }
            });
        }
    }

    @Override
    protected ItemManager.Item getItem(int position) {
        return mItems.get(position);
    }

    private void openItem(ItemManager.Item item) {
        context.startActivity(new Intent(context, ItemActivity.class)
                .putExtra(ItemActivity.EXTRA_ITEM, item));
    }
}
