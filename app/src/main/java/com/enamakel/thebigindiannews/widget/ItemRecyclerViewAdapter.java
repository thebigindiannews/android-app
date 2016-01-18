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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.enamakel.thebigindiannews.AlertDialogBuilder;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.ComposeActivity;
import com.enamakel.thebigindiannews.Injectable;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;

public abstract class ItemRecyclerViewAdapter<VH extends ItemRecyclerViewAdapter.ItemViewHolder>
        extends RecyclerView.Adapter<VH> {
    private static final String PROPERTY_MAX_LINES = "maxLines";
    private static final int DURATION_PER_LINE_MILLIS = 20;
    protected LayoutInflater mLayoutInflater;
    private ItemManager mItemManager;
    @Inject UserServices mUserServices;
    @Inject PopupMenu mPopupMenu;
    @Inject AlertDialogBuilder mAlertDialogBuilder;
    protected Context mContext;
    private int mTertiaryTextColorResId;
    private int mSecondaryTextColorResId;
    private int mCardBackgroundColorResId;
    private int mCardHighlightColorResId;
    private int mContentMaxLines = Integer.MAX_VALUE;
    private String mUsername;
    private final Set<String> mLineCounted = new HashSet<>();

    public ItemRecyclerViewAdapter(ItemManager itemManager) {
        mItemManager = itemManager;
        setHasStableIds(true);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
        if (mContext instanceof Injectable) {
            ((Injectable) mContext).inject(this);
        }
        mLayoutInflater = LayoutInflater.from(mContext);
        TypedArray ta = mContext.obtainStyledAttributes(new int[]{
                android.R.attr.textColorTertiary,
                android.R.attr.textColorSecondary,
                R.attr.colorCardBackground,
                R.attr.colorCardHighlight
        });
        mTertiaryTextColorResId = ta.getInt(0, 0);
        mSecondaryTextColorResId = ta.getInt(1, 0);
        mCardBackgroundColorResId = ta.getInt(2, 0);
        mCardHighlightColorResId = ta.getInt(3, 0);
        ta.recycle();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mContext = null;
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final ItemManager.Item item = getItem(position);
        if (item.getLocalRevision() < 0) {
            clear(holder);
            load(holder.getAdapterPosition(), item);
        } else {
            bind(holder, item);
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getLongId();
    }

    public void setMaxLines(int maxLines) {
        mContentMaxLines = maxLines;
        notifyDataSetChanged();
    }

    public void setHighlightUsername(String username) {
        mUsername = username;
        notifyDataSetChanged();
    }

    public boolean isAttached() {
        return mContext != null;
    }

    protected abstract ItemManager.Item getItem(int position);

    @CallSuper
    protected void bind(final VH holder, final ItemManager.Item item) {
        if (item == null) {
            return;
        }
        highlightUserItem(holder, item);
        decorateDead(holder, item);
        AppUtils.setTextWithLinks(holder.mContentTextView, item.getText());
        if (mLineCounted.contains(item.getId())) {
            toggleCollapsibleContent(holder, item);
        } else {
        holder.mContentTextView.post(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) {
                    return;
                }
                toggleCollapsibleContent(holder, item);
                mLineCounted.add(item.getId());
            }
        });
        }
        bindActions(holder, item);
    }

    protected void clear(VH holder) {
        holder.mCommentButton.setVisibility(View.GONE);
        holder.mPostedTextView.setOnClickListener(null);
        holder.mPostedTextView.setText(R.string.loading_text);
        holder.mContentTextView.setText(R.string.loading_text);
        holder.mReadMoreTextView.setVisibility(View.GONE);
    }

    private void load(int adapterPosition, ItemManager.Item item) {
        mItemManager.getItem(item.getId(), new ItemResponseListener(this, adapterPosition, item));
    }

    protected void onItemLoaded(int position, ItemManager.Item item) {
        if (position < getItemCount()) {
            notifyItemChanged(position);
        }
    }

    private void highlightUserItem(VH holder, ItemManager.Item item) {
        boolean highlight = !TextUtils.isEmpty(mUsername) &&
                TextUtils.equals(mUsername, item.getBy());
        holder.mContentView.setBackgroundColor(highlight ?
                mCardHighlightColorResId : mCardBackgroundColorResId);
    }

    private void decorateDead(VH holder, ItemManager.Item item) {
        holder.mContentTextView.setTextColor(item.isDead() ?
                mSecondaryTextColorResId : mTertiaryTextColorResId);
    }

    private void toggleCollapsibleContent(final VH holder, final ItemManager.Item item) {
        final int lineCount = holder.mContentTextView.getLineCount();
        if (item.isContentExpanded() || lineCount <= mContentMaxLines) {
            holder.mContentTextView.setMaxLines(Integer.MAX_VALUE);
            setTextIsSelectable(holder.mContentTextView, true);
            holder.mReadMoreTextView.setVisibility(View.GONE);
            return;
        }
        holder.mContentTextView.setMaxLines(mContentMaxLines);
        setTextIsSelectable(holder.mContentTextView, false);
        holder.mReadMoreTextView.setVisibility(View.VISIBLE);
        holder.mReadMoreTextView.setText(mContext.getString(R.string.read_more, lineCount));
        holder.mReadMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                item.setContentExpanded(true);
                v.setVisibility(View.GONE);
                setTextIsSelectable(holder.mContentTextView, true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    ObjectAnimator.ofInt(holder.mContentTextView, PROPERTY_MAX_LINES, lineCount)
                            .setDuration((lineCount - mContentMaxLines) * DURATION_PER_LINE_MILLIS)
                            .start();
                } else {
                    holder.mContentTextView.setMaxLines(Integer.MAX_VALUE);
                }
            }
        });
    }

    private void setTextIsSelectable(TextView textView, boolean isSelectable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            textView.setTextIsSelectable(isSelectable);
        }
    }

    private void bindActions(final VH holder, final ItemManager.Item item) {
        if (item.isDead() || item.isDeleted()) {
            holder.mMoreButton.setVisibility(View.GONE);
            return;
        }
        holder.mMoreButton.setVisibility(View.VISIBLE);
        holder.mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupMenu.create(mContext, holder.mMoreButton, Gravity.NO_GRAVITY);
                mPopupMenu.inflate(R.menu.menu_contextual_comment);
                mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.menu_contextual_vote) {
                            vote(item);
                            return true;
                        }
                        if (menuItem.getItemId() == R.id.menu_contextual_comment) {
                            mContext.startActivity(new Intent(mContext, ComposeActivity.class)
                                    .putExtra(ComposeActivity.EXTRA_PARENT_ID, item.getId())
                                    .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, item.getText()));
                            return true;
                        }
                        return false;
                    }
                });
                mPopupMenu.show();
            }
        });
    }

    private void vote(final ItemManager.Item item) {
        mUserServices.voteUp(mContext, item.getId(), new VoteCallback(this));
    }

    private void onVoted(Boolean successful) {
        if (successful == null) {
            Toast.makeText(mContext, R.string.vote_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Toast.makeText(mContext, R.string.voted, Toast.LENGTH_SHORT).show();
        } else {
            AppUtils.showLogin(mContext, mAlertDialogBuilder);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        final TextView mPostedTextView;
        final TextView mContentTextView;
        final TextView mReadMoreTextView;
        final Button mCommentButton;
        final View mMoreButton;
        final View mContentView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mPostedTextView = (TextView) itemView.findViewById(R.id.posted);
            mPostedTextView.setMovementMethod(LinkMovementMethod.getInstance());
            mContentTextView = (TextView) itemView.findViewById(R.id.text);
            mReadMoreTextView = (TextView) itemView.findViewById(R.id.more);
            mCommentButton = (Button) itemView.findViewById(R.id.comment);
            mCommentButton.setVisibility(View.GONE);
            mMoreButton = itemView.findViewById(R.id.button_more);
            mContentView = itemView.findViewById(R.id.content);
        }
    }

    private static class ItemResponseListener implements ResponseListener<ItemManager.Item> {
        private final WeakReference<ItemRecyclerViewAdapter> mAdapter;
        private final int mPosition;
        private final ItemManager.Item mPartialItem;

        public ItemResponseListener(ItemRecyclerViewAdapter adapter, int position,
                                    ItemManager.Item partialItem) {
            mAdapter = new WeakReference<>(adapter);
            mPosition = position;
            mPartialItem = partialItem;
        }

        @Override
        public void onResponse(ItemManager.Item response) {
            if (mAdapter.get() != null && mAdapter.get().isAttached() && response != null) {
                mPartialItem.populate(response);
                mPartialItem.setLocalRevision(0);
                mAdapter.get().onItemLoaded(mPosition, mPartialItem);
            }
        }

        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }

    private static class VoteCallback extends UserServices.Callback {
        private final WeakReference<ItemRecyclerViewAdapter> mAdapter;

        public VoteCallback(ItemRecyclerViewAdapter adapter) {
            mAdapter = new WeakReference<>(adapter);
        }

        @Override
        public void onDone(boolean successful) {
            if (mAdapter.get() != null && mAdapter.get().isAttached()) {
                mAdapter.get().onVoted(successful);
            }
        }

        @Override
        public void onError() {
            if (mAdapter.get() != null && mAdapter.get().isAttached()) {
                mAdapter.get().onVoted(null);
            }
        }
    }
}