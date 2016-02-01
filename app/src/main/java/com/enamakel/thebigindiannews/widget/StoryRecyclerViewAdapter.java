/*
 * Copyright (c) 2016 Ha Duy Trung
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

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.activities.UserActivity;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.MaterialisticProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;

public class StoryRecyclerViewAdapter extends
        ListRecyclerViewAdapter<ListRecyclerViewAdapter.ItemViewHolder, StoryModel> {
    private static final String STATE_ITEMS = "state:items";
    private static final String STATE_UPDATED = "state:updated";
    private static final String STATE_PROMOTED = "state:promoted";
    private static final String STATE_SHOW_ALL = "state:showAll";
    private static final String STATE_HIGHLIGHT_UPDATED = "state:isHighlightUpdated";
    private static final String STATE_FAVORITE_REVISION = "state:favoriteRevision";
    private static final String STATE_USERNAME = "state:username";

    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (FavoriteManager.isCleared(uri)) {
                favoriteRevision++; // invalidate all favorite statuses
                notifyDataSetChanged();
                return;
            }

//            String position = itemPositions.get(Long.valueOf(uri.getLastPathSegment()));
//            if (position == null) return;

//            ItemManager.Item item = items.get(position);
//            if (FavoriteManager.isAdded(uri)) {
//                item.setFavorite(true);
//                item.setLocalRevision(favoriteRevision);
//            } else if (FavoriteManager.isRemoved(uri)) {
//                item.setFavorite(false);
//                item.setLocalRevision(favoriteRevision);
//            } else item.setIsViewed(true);
//
//            notifyItemChanged(position);
        }
    };

    @Inject @Named(ActivityModule.HN) ItemManager itemManager;
    private @Getter ArrayList<StoryModel> items;
    private ArrayList<StoryModel> updatedItems = new ArrayList<>();
    private ArrayList<String> promotedList = new ArrayList<>();
    //    private final LongSparseArray<String> itemPositions = new LongSparseArray<>();
    //    private final LongSparseArray<String> updatedPositions = new LongSparseArray<>();
    private final HashMap<String, Integer> itemPositions = new HashMap<>();
    private final HashMap<String, Integer> updatedPositions = new HashMap<>();
    private int favoriteRevision = -1;
    private @Setter String username;
    private boolean isHighlightUpdated = true;
    private boolean shouldShowAll = true;


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        ContentResolver cr = recyclerView.getContext().getContentResolver();
        cr.registerContentObserver(MaterialisticProvider.URI_VIEWED, true, contentObserver);
        cr.registerContentObserver(MaterialisticProvider.URI_FAVORITE, true, contentObserver);
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.getContext().getContentResolver().unregisterContentObserver(contentObserver);
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(R.layout.item_story, parent, false));
    }


    @Override
    public int getItemCount() {
        if (shouldShowAll) return itemPositions.size();
        else return updatedPositions.size();
    }


    @Override
    public Bundle saveState() {
        Bundle savedState = super.saveState();
        savedState.putParcelableArrayList(STATE_ITEMS, items);
        savedState.putParcelableArrayList(STATE_UPDATED, updatedItems);
        savedState.putStringArrayList(STATE_PROMOTED, promotedList);
        savedState.putBoolean(STATE_SHOW_ALL, shouldShowAll);
        savedState.putBoolean(STATE_HIGHLIGHT_UPDATED, isHighlightUpdated);
        savedState.putInt(STATE_FAVORITE_REVISION, favoriteRevision);
        savedState.putString(STATE_USERNAME, username);
        return savedState;
    }


    @Override
    public void restoreState(Bundle savedState) {
        if (savedState == null) return;

        super.restoreState(savedState);
        ArrayList<StoryModel> savedItems = savedState.getParcelableArrayList(STATE_ITEMS);
        setItemsInternal(savedItems);
        updatedItems = savedState.getParcelableArrayList(STATE_UPDATED);

        if (updatedItems != null)
            for (int i = 0; i < updatedItems.size(); i++)
                updatedPositions.put(updatedItems.get(i).get_id(), i);


        promotedList = savedState.getStringArrayList(STATE_PROMOTED);
        shouldShowAll = savedState.getBoolean(STATE_SHOW_ALL, true);
        isHighlightUpdated = savedState.getBoolean(STATE_HIGHLIGHT_UPDATED, true);
        favoriteRevision = savedState.getInt(STATE_FAVORITE_REVISION);
        username = savedState.getString(STATE_USERNAME);
    }


    public void setItems(ArrayList<StoryModel> items) {
        setUpdated(items);
        setItemsInternal(items);
        notifyDataSetChanged();
    }


    public void setHighlightUpdated(boolean highlightUpdated) {
        this.isHighlightUpdated = highlightUpdated;
    }


    public void setShowAll(boolean showAll) {
        shouldShowAll = showAll;
    }


    @Override
    protected void loadItem(final int adapterPosition) {
        StoryModel item = getItem(adapterPosition);
        itemManager.getItem(item.getId(), new ItemResponseListener(this, item));
    }


    @Override
    protected void bindItem(final ItemViewHolder holder) {
        final StoryModel story = getItem(holder.getAdapterPosition());
        bindItemUpdated(holder, story);
        highlightUserPost(holder, story);
        holder.storyView.setViewed(false);
        holder.storyView.setViewed(story.isViewed());
//        if (story.getLocalRevision() < favoriteRevision) story.setFavorite(false);

        holder.storyView.setFavorite(story.isFavorite());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMoreOptions(holder.storyView.getMoreOptions(), story, holder);
                return true;
            }
        });
//        holder.storyView.getMoreOptions().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showMoreOptions(v, story, holder);
//            }
//        });
    }


    @Override
    protected boolean isItemAvailable(StoryModel item) {
        return item != null && !TextUtils.isEmpty(item.getTitle());
    }


    @Override
    protected StoryModel getItem(int position) {
        if (shouldShowAll) return items.get(position);
        else return updatedItems.get(position);
    }


    private void setItemsInternal(ArrayList<StoryModel> items) {
        this.items = items;
        itemPositions.clear();

        if (items != null)
            for (int i = 0; i < items.size(); i++) itemPositions.put(items.get(i).get_id(), i);
    }


    private void setUpdated(ArrayList<StoryModel> stories) {
        if (!isHighlightUpdated || getItems() == null) return;

        updatedItems.clear();
        updatedPositions.clear();
        promotedList.clear();

        for (StoryModel story : stories) {
            Integer position = itemPositions.get(story.get_id());

            if (position == null) {
                updatedItems.add(story);
                updatedPositions.put(story.get_id(), updatedItems.size() - 1);
            } else {
                StoryModel currentRevision = this.items.get(position);
//                item.setLastKidCount(currentRevision.getLastKidCount());
            }
        }

        if (!updatedItems.isEmpty()) notifyUpdated();
    }


    private void notifyUpdated() {
        if (shouldShowAll) {
            Snackbar.make(recyclerView,
                    context.getResources().getQuantityString(R.plurals.new_stories_count,
                            updatedItems.size(), updatedItems.size()),
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.show_me, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setShowAll(false);
                            notifyUpdated();
                            notifyDataSetChanged();
                        }
                    })
                    .show();
        } else {
            final Snackbar snackbar = Snackbar.make(recyclerView,
                    context.getResources().getQuantityString(R.plurals.showing_new_stories,
                            updatedItems.size(), updatedItems.size()),
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.show_all, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    updatedItems.clear();
                    setShowAll(true);
                    notifyDataSetChanged();
                }
            }).show();
        }
    }


    private void onItemLoaded(StoryModel item) {
        Integer position = shouldShowAll ? itemPositions.get(item.get_id()) :
                updatedPositions.get(item.get_id());

        // ignore changes if item was invalidated by refresh / filter
        if (position != null && position >= 0 && position < getItemCount()) {
            notifyItemChanged(position);
        }
    }


    private void bindItemUpdated(ItemViewHolder holder, StoryModel story) {
        if (isHighlightUpdated) {
//            boolean a = updatedPositions.indexOfKey(story.getLongId()) >= 0;
            boolean a = false;
            holder.storyView.setUpdated(story,
                    a,
                    promotedList.contains(story.get_id()));
        }
    }


    private void showMoreOptions(View view, final StoryModel story, final ItemViewHolder holder) {
        popupMenu.create(context, view, Gravity.NO_GRAVITY);
        popupMenu.inflate(R.menu.menu_contextual_story);

        popupMenu.getMenu().findItem(R.id.menu_contextual_save)
                .setTitle(story.isFavorite() ? R.string.unsave : R.string.save);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_contextual_save) {
                    toggleSave(story);
                    return true;
                }

                if (item.getItemId() == R.id.menu_contextual_vote) {
                    readStory(story, holder);
                    return true;
                }

                if (item.getItemId() == R.id.menu_contextual_comment) {
                    context.startActivity(new Intent(context, ComposeActivity.class)
                            .putExtra(ComposeActivity.EXTRA_PARENT_ID, story.get_id())
                            .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, story.getTitle()));
                    return true;
                }

                if (item.getItemId() == R.id.menu_contextual_profile) {
                    context.startActivity(new Intent(context, UserActivity.class)
                            .putExtra(UserActivity.EXTRA_USERNAME, story.getCreated_by()));
                    return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }


    private void toggleSave(final StoryModel story) {
        final int toastMessageResId;

        if (!story.isFavorite()) {
            favoriteManager.add(context, story);
            toastMessageResId = R.string.toast_saved;
        } else {
            favoriteManager.remove(context, story);
            toastMessageResId = R.string.toast_removed;
        }

        Snackbar.make(recyclerView, toastMessageResId, Snackbar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleSave(story);
                    }
                })
                .show();
    }


    private void readStory(final StoryModel story, final ItemViewHolder holder) {
        userServices.voteUp(context, story.get_id(),
                new StoryReadCallback(this, holder.getAdapterPosition(), story));
    }


    private void onVoted(int position, Boolean successful) {
        if (successful == null) {
            Toast.makeText(context, R.string.vote_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Toast.makeText(context, R.string.voted, Toast.LENGTH_SHORT).show();
            if (position < getItemCount()) notifyItemChanged(position);
        } else {
            AppUtils.showLogin(context, alertDialogBuilder);
        }
    }


    private void highlightUserPost(ItemViewHolder holder, StoryModel story) {
        holder.storyView.setChecked(isSelected(story.get_id()) ||
                !TextUtils.isEmpty(username) && TextUtils.equals(username, story.getCreated_by()));
    }


    private static class ItemResponseListener implements ResponseListener<StoryModel> {
        private final WeakReference<StoryRecyclerViewAdapter> adapter;
        private final StoryModel partialItem;


        public ItemResponseListener(StoryRecyclerViewAdapter adapter, StoryModel partialItem) {
            this.adapter = new WeakReference<>(adapter);
            this.partialItem = partialItem;
        }


        @Override
        public void onResponse(StoryModel response) {
            if (adapter.get() != null && adapter.get().isAttached() && response != null) {
                partialItem.populate(response);
                adapter.get().onItemLoaded(partialItem);
            }
        }


        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }

    private static class StoryReadCallback extends UserServices.Callback {
        private final WeakReference<StoryRecyclerViewAdapter> adapter;
        private final int position;
        private final StoryModel storyModel;


        public StoryReadCallback(StoryRecyclerViewAdapter adapter, int position,
                                 StoryModel item) {
            this.adapter = new WeakReference<>(adapter);
            this.position = position;
            storyModel = item;
        }


        @Override
        public void onDone(boolean successful) {
            // TODO update locally only, as API does not update instantly
            storyModel.setClicks_count(storyModel.getClicks_count() + 1);

            if (adapter.get() != null && adapter.get().isAttached())
                adapter.get().onVoted(position, successful);
        }


        @Override
        public void onError() {
            if (adapter.get() != null && adapter.get().isAttached())
                adapter.get().onVoted(position, null);
        }
    }
}