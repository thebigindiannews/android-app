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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.ItemActivity;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.util.Injectable;
import com.enamakel.thebigindiannews.util.MultiPaneListener;

import javax.inject.Inject;

/**
 * Base {@link android.support.v7.widget.RecyclerView.Adapter} class for list items
 *
 * @param <VH> view holder type, should contain title, posted, source and comment views
 * @param <T>  item type, should provide title, posted, source
 */
public abstract class ListRecyclerViewAdapter
        <VH extends ListRecyclerViewAdapter.ItemViewHolder, T extends BaseCardModel>
        extends RecyclerView.Adapter<VH> {
    static final String STATE_LAST_SELECTION_POSITION = "state:lastSelectedPosition";
    static final String STATE_CARD_VIEW_ENABLED = "state:cardViewEnabled";

    MultiPaneListener multiPaneListener;
    int lastSelectedPosition = -1;
    int cardElevation;
    int cardRadius;
    boolean isCardViewEnabled = true;

    protected Context context;
    protected RecyclerView recyclerView;
    protected LayoutInflater inflater;

    @Inject PopupMenu popupMenu;
    @Inject AlertDialogBuilder alertDialogBuilder;
    @Inject UserServices userServices;
    @Inject FavoriteManager favoriteManager;


    public ListRecyclerViewAdapter() {
        setHasStableIds(true);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        context = recyclerView.getContext();
        inflater = LayoutInflater.from(context);
        ((Injectable) context).inject(this);
        multiPaneListener = (MultiPaneListener) context;
        cardElevation = context.getResources()
                .getDimensionPixelSize(R.dimen.cardview_default_elevation);
        cardRadius = context.getResources()
                .getDimensionPixelSize(R.dimen.cardview_default_radius);
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        context = null;
        multiPaneListener = null;
        this.recyclerView = null;
    }


    @Override
    public final void onBindViewHolder(final VH holder, int position) {
        final T item = getItem(position);
        if (isCardViewEnabled) {
            holder.cardView.setCardElevation(cardElevation);
            holder.cardView.setRadius(cardRadius);
            holder.cardView.setUseCompatPadding(true);
        } else {
            holder.cardView.setCardElevation(0);
            holder.cardView.setRadius(0);
            holder.cardView.setUseCompatPadding(false);
        }

        if (!isItemAvailable(item)) {
            clearViewHolder(holder);
            loadItem(holder.getAdapterPosition());
            return;
        }

        if (item instanceof StoryModel) {
            StoryModel story = (StoryModel) item;
            holder.storyView.setStory(story);
            holder.storyView.setChecked(isSelected(item.get_id()));
            holder.storyView.setOnCommentClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openItem(item);
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleItemClick(item, holder);
            }
        });

        bindItem(holder);
    }


    @Override
    public final long getItemId(int position) {
        return getItem(position).getLongId();
    }


    public final boolean isCardViewEnabled() {
        return isCardViewEnabled;
    }


    public final void setCardViewEnabled(boolean cardViewEnabled) {
        this.isCardViewEnabled = cardViewEnabled;
    }


    public Bundle saveState() {
        Bundle savedState = new Bundle();
        savedState.putInt(STATE_LAST_SELECTION_POSITION, lastSelectedPosition);
        savedState.putBoolean(STATE_CARD_VIEW_ENABLED, isCardViewEnabled);
        return savedState;
    }


    public void restoreState(Bundle savedState) {
        if (savedState == null) return;

        isCardViewEnabled = savedState.getBoolean(STATE_CARD_VIEW_ENABLED, true);
        lastSelectedPosition = savedState.getInt(STATE_LAST_SELECTION_POSITION);
    }


    public final boolean isAttached() {
        return context != null;
    }


    protected void loadItem(int adapterPosition) {
        // override to load item if needed
    }


    protected abstract void bindItem(VH holder);


    protected abstract boolean isItemAvailable(T item);


    /**
     * Clears previously bind data from given view holder
     *
     * @param holder view holder to clear
     */
    protected final void clearViewHolder(VH holder) {
        holder.storyView.reset();
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);
    }


    /**
     * Checks if item with given ID has been selected
     *
     * @param itemId item ID to check
     * @return true if selected, false otherwise or if selection is disabled
     */
    protected boolean isSelected(String itemId) {
        return multiPaneListener.isMultiPane() &&
                multiPaneListener.getSelectedItem() != null &&
                itemId.equals(multiPaneListener.getSelectedItem().getId());
    }


    /**
     * Gets item at position
     *
     * @param position item position
     * @return item at given position or null
     */
    protected abstract T getItem(int position);


    /**
     * Handles item click
     *
     * @param item   clicked item
     * @param holder clicked item view holder
     */
    protected void handleItemClick(T item, VH holder) {
        multiPaneListener.onItemSelected(item);

        if (isSelected(item.get_id())) {
            notifyItemChanged(holder.getAdapterPosition());
            if (lastSelectedPosition >= 0) notifyItemChanged(lastSelectedPosition);
            lastSelectedPosition = holder.getAdapterPosition();
        }
    }


    private void openItem(T item) {
        context.startActivity(new Intent(context, ItemActivity.class)
                .putExtra(ItemActivity.EXTRA_ITEM, item)
                .putExtra(ItemActivity.EXTRA_OPEN_COMMENTS, true));
    }


    /**
     * Base {@link android.support.v7.widget.RecyclerView.ViewHolder} class for list item view
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final StoryView_ storyView;
        public final CardView cardView;


        public ItemViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            storyView = (StoryView_) itemView.findViewById(R.id.story_view);
        }
    }
}