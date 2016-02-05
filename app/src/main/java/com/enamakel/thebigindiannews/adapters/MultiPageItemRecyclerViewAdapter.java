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
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.data.managers.ItemManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;

public class MultiPageItemRecyclerViewAdapter
        extends ItemRecyclerViewAdapter<MultiPageItemRecyclerViewAdapter.ItemViewHolder> {
    final StoryModel[] mItems;


    public MultiPageItemRecyclerViewAdapter(ItemManager itemManager,
                                            StoryModel[] items) {
        super(itemManager);
        this.mItems = items;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(layoutInflater.inflate(R.layout.item_comment, parent, false));
    }


    @Override
    protected StoryModel getItem(int position) {
        return mItems[position];
    }


    @Override
    protected void bind(final ItemViewHolder holder, final StoryModel item) {
        super.bind(holder, item);
        if (item == null) return;

        holder.postedTextView.setText(item.getDisplayedTime(context, false, true));
//        if (item.getKidCount() > 0) {
//            holder.commentButton.setText(context.getResources()
//                    .getQuantityString(R.plurals.comments_count, item.getKidCount(), item.getKidCount()));
//            holder.commentButton.setVisibility(View.VISIBLE);
//            holder.commentButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openItem(item);
//                }
//            });
//        }
    }


    @Override
    public int getItemCount() {
        return mItems.length;
    }


    void openItem(ItemManager.Item item) {
        context.startActivity(new Intent(context, SingleStoryActivity.class)
                .putExtra(SingleStoryActivity.EXTRA_ITEM, item)
                .putExtra(SingleStoryActivity.EXTRA_OPEN_COMMENTS, true));
    }


    /**
     * Created by robert on 2/1/16.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        protected final android.widget.TextView postedTextView;
        protected final android.widget.TextView contentTextView;
        protected final android.widget.TextView readMoreTextView;
        protected final Button commentButton;
        protected final View moreButton;
        protected final View contentView;


        public ItemViewHolder(View itemView) {
            super(itemView);
            postedTextView = (android.widget.TextView) itemView.findViewById(R.id.posted);
            postedTextView.setMovementMethod(LinkMovementMethod.getInstance());
            contentTextView = (android.widget.TextView) itemView.findViewById(R.id.text);
            readMoreTextView = (android.widget.TextView) itemView.findViewById(R.id.more);
            commentButton = (Button) itemView.findViewById(R.id.comment);
            commentButton.setVisibility(View.GONE);
            moreButton = itemView.findViewById(R.id.button_more);
            contentView = itemView.findViewById(R.id.content);
        }
    }
}
