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

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.data.managers.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.util.Injectable;
import com.enamakel.thebigindiannews.widgets.PopupMenu;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public abstract class ItemRecyclerViewAdapter<VH extends MultiPageItemRecyclerViewAdapter.ItemViewHolder>
        extends RecyclerView.Adapter<VH> {
    static final String PROPERTY_MAX_LINES = "maxLines";
    static final int DURATION_PER_LINE_MILLIS = 20;
    LayoutInflater layoutInflater;
    ItemManager itemManager;
    int tertiaryTextColorResId;
    int secondaryTextColorResId;
    int cardBackgroundColorResId;
    int cardHighlightColorResId;
    int contentMaxLines = Integer.MAX_VALUE;
    String username;
    final Set<String> lineCounted = new HashSet<>();

    protected Context context;

    @Inject UserServices userServices;
    @Inject PopupMenu popupMenu;
    @Inject AlertDialogBuilder alertDialogBuilder;


    public ItemRecyclerViewAdapter(ItemManager itemManager) {
        this.itemManager = itemManager;
        setHasStableIds(true);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        if (context instanceof Injectable) {
            ((Injectable) context).inject(this);
        }
        layoutInflater = LayoutInflater.from(context);
        TypedArray ta = context.obtainStyledAttributes(new int[]{
                android.R.attr.textColorTertiary,
                android.R.attr.textColorSecondary,
                R.attr.colorCardBackground,
                R.attr.colorCardHighlight
        });
        tertiaryTextColorResId = ta.getInt(0, 0);
        secondaryTextColorResId = ta.getInt(1, 0);
        cardBackgroundColorResId = ta.getInt(2, 0);
        cardHighlightColorResId = ta.getInt(3, 0);
        ta.recycle();
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        context = null;
    }


    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final StoryModel item = getItem(position);
        if (item.getLocal_revision() < 0) {
            clear(holder);
            load(holder.getAdapterPosition(), item);
        } else bind(holder, item);
    }


    @Override
    public long getItemId(int position) {
        return getItem(position).getLongId();
    }


    public void setMaxLines(int maxLines) {
        contentMaxLines = maxLines;
        notifyDataSetChanged();
    }


    public void setHighlightUsername(String username) {
        this.username = username;
        notifyDataSetChanged();
    }


    public boolean isAttached() {
        return context != null;
    }


    protected abstract StoryModel getItem(int position);


    @CallSuper
    protected void bind(final VH holder, final StoryModel item) {
        if (item == null) return;

        highlightUserItem(holder, item);
        decorateDead(holder, item);
//        AppUtils.setTextWithLinks(holder.contentTextView, item.getText());
        if (lineCounted.contains(item.getId())) toggleCollapsibleContent(holder, item);
        else {
            holder.contentTextView.post(new Runnable() {
                @Override
                public void run() {
                    if (context == null) {
                        return;
                    }
                    toggleCollapsibleContent(holder, item);
                    lineCounted.add(item.getId());
                }
            });
        }
        bindActions(holder, item);
    }


    protected void clear(VH holder) {
        holder.commentButton.setVisibility(View.GONE);
        holder.postedTextView.setOnClickListener(null);
        holder.postedTextView.setText(R.string.loading_text);
        holder.contentTextView.setText(R.string.loading_text);
        holder.readMoreTextView.setVisibility(View.GONE);
    }


    void load(int adapterPosition, StoryModel item) {
        itemManager.getItem(item.getId(), new ItemResponseListener(this, adapterPosition, item));
    }


    protected void onItemLoaded(int position, StoryModel item) {
        if (position < getItemCount()) {
            notifyItemChanged(position);
        }
    }


    void highlightUserItem(VH holder, StoryModel item) {
        boolean highlight = !TextUtils.isEmpty(username) &&
                TextUtils.equals(username, item.getCreated_by());
        holder.contentView.setBackgroundColor(highlight ?
                cardHighlightColorResId : cardBackgroundColorResId);
    }


    void decorateDead(VH holder, StoryModel item) {
        holder.contentTextView.setTextColor(item.isDead() ?
                secondaryTextColorResId : tertiaryTextColorResId);
    }


    void toggleCollapsibleContent(final VH holder, final StoryModel item) {
        final int lineCount = holder.contentTextView.getLineCount();
//        if (item.isContentExpanded() || lineCount <= contentMaxLines) {
//            holder.contentTextView.setMaxLines(Integer.MAX_VALUE);
//            setTextIsSelectable(holder.contentTextView, true);
//            holder.readMoreTextView.setVisibility(View.GONE);
//            return;
//        }
//
//        holder.contentTextView.setMaxLines(contentMaxLines);
//        setTextIsSelectable(holder.contentTextView, false);
//        holder.readMoreTextView.setVisibility(View.VISIBLE);
//        holder.readMoreTextView.setText(context.getString(R.string.read_more, lineCount));
//        holder.readMoreTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                item.setContentExpanded(true);
//                v.setVisibility(View.GONE);
//                setTextIsSelectable(holder.contentTextView, true);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                    ObjectAnimator.ofInt(holder.contentTextView, PROPERTY_MAX_LINES, lineCount)
//                            .setDuration((lineCount - contentMaxLines) * DURATION_PER_LINE_MILLIS)
//                            .start();
//                } else holder.contentTextView.setMaxLines(Integer.MAX_VALUE);
//            }
//        });
    }


    void setTextIsSelectable(TextView textView, boolean isSelectable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            textView.setTextIsSelectable(isSelectable);
    }


    void bindActions(final VH holder, final StoryModel item) {
        if (item.isDead() || item.isDeleted()) {
            holder.moreButton.setVisibility(View.GONE);
            return;
        }

        holder.moreButton.setVisibility(View.VISIBLE);
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.create(context, holder.moreButton, Gravity.NO_GRAVITY);
                popupMenu.inflate(R.menu.menu_contextual_comment);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.menu_contextual_vote) {
//                            vote(item);
                            return true;
                        }
                        if (menuItem.getItemId() == R.id.menu_contextual_comment) {
                            context.startActivity(new Intent(context, ComposeActivity.class)
                                    .putExtra(ComposeActivity.EXTRA_PARENT_ID, item.getId())
                                    .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, item.getTitle()));
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }


    void vote(final StoryModel item) {
//        userServices.voteUp(context, item.getId(), new VoteCallback(this));
    }


    static class ItemResponseListener implements ResponseListener<StoryModel> {
        final WeakReference<ItemRecyclerViewAdapter> weakReference;
        final int position;
        final StoryModel partialItem;


        public ItemResponseListener(ItemRecyclerViewAdapter adapter, int position,
                                    StoryModel partialItem) {
            weakReference = new WeakReference<>(adapter);
            this.position = position;
            this.partialItem = partialItem;
        }


        @Override
        public void onResponse(StoryModel response) {
            if (weakReference.get() != null && weakReference.get().isAttached() && response != null) {
                partialItem.populate(response);
                partialItem.setLocal_revision(0);
                weakReference.get().onItemLoaded(position, partialItem);
            }
        }


        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }
}