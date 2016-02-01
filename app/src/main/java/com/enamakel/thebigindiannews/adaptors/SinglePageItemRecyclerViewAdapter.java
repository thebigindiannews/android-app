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

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.widgets.ToggleItemViewHolder;

import java.util.ArrayList;

public class SinglePageItemRecyclerViewAdapter
        extends ItemRecyclerViewAdapter<ToggleItemViewHolder> {
    private int levelIndicatorWidth = 0;
    private final boolean autoExpand;
    private boolean isColorCoded = true;
    private TypedArray colors;
    private RecyclerView recyclerView;
    private final @NonNull SavedState savedState;


    public SinglePageItemRecyclerViewAdapter(ItemManager itemManager,
                                             @NonNull SavedState state,
                                             boolean autoExpand) {
        super(itemManager);
        this.savedState = state;
        this.autoExpand = autoExpand;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        levelIndicatorWidth = AppUtils.getDimensionInDp(context, R.dimen.level_indicator_width);
        colors = context.getResources().obtainTypedArray(R.array.color_codes);
        this.recyclerView = recyclerView;
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }


    @Override
    public ToggleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ToggleItemViewHolder holder =
                new ToggleItemViewHolder(layoutInflater.inflate(R.layout.item_comment, parent, false));
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                holder.itemView.getLayoutParams();
        params.leftMargin = levelIndicatorWidth * viewType;
        holder.itemView.setLayoutParams(params);
        return holder;
    }


    @Override
    public void onBindViewHolder(ToggleItemViewHolder holder, int position) {
        if (isColorCoded && colors != null && colors.length() > 0) {
            holder.level.setVisibility(View.VISIBLE);
            holder.level.setBackgroundColor(colors.getColor(
                    getItemViewType(position) % colors.length(), 0));
        } else holder.level.setVisibility(View.GONE);

        super.onBindViewHolder(holder, position);
    }


    @Override
    public int getItemViewType(int position) {
        return 0; //getItem(position).getLevel() - 1;
    }


    @Override
    public int getItemCount() {
        return savedState.list.size();
    }


    public void toggleColorCode(boolean enabled) {
        isColorCoded = enabled;
        notifyDataSetChanged();
    }


    @Override
    protected StoryModel getItem(int position) {
//        return null;
        return savedState.list.get(position);
    }


    @Override
    protected void onItemLoaded(int position, StoryModel item) {
        // item position may already be shifted due to expansion, need to get new position
        int index = savedState.list.indexOf(item);
        if (index >= 0 && index < getItemCount()) {
            notifyItemChanged(index);
        }
    }


    @Override
    protected void clear(ToggleItemViewHolder holder) {
        super.clear(holder);
        holder.toggle.setVisibility(View.GONE);
    }


    @Override
    protected void bind(ToggleItemViewHolder holder, StoryModel item) {
        super.bind(holder, item);
        if (item == null) return;

        holder.postedTextView.setText(item.getDisplayedTime(context, false, true));
//        bindNavigation(holder, item);
//        toggleKids(holder, item);
    }


    private void bindNavigation(ToggleItemViewHolder holder, final ItemManager.Item item) {
        if (!savedState.expanded.containsKey(item.getParent())) {
            holder.parent.setVisibility(View.INVISIBLE);
            return;
        }
        holder.parent.setVisibility(View.VISIBLE);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemManager.Item parent = savedState.expanded.getParcelable(item.getParent());
                int position = savedState.list.indexOf(parent);
                recyclerView.smoothScrollToPosition(position);
            }
        });
    }


    private void toggleKids(final ToggleItemViewHolder holder, final ItemManager.Item item) {
        holder.toggle.setVisibility(item.getKidCount() > 0 ? View.VISIBLE : View.GONE);

        if (item.getKidCount() == 0) return;
//        if (!item.isCollapsed() && autoExpand) expand(item);

        bindToggle(holder, item, isExpanded(item));
        holder.toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expanded = isExpanded(item);
                bindToggle(holder, item, !expanded);
                item.setCollapsed(!item.isCollapsed());
                if (expanded) collapse(item);
//                else expand(item);
            }
        });
    }


    private void bindToggle(ToggleItemViewHolder holder, ItemManager.Item item, boolean expanded) {
        if (expanded) {
            holder.toggle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_expand_less_white_24dp, 0);
            holder.toggle.setText(context.getResources()
                    .getQuantityString(R.plurals.hide_comments, item.getKidCount(), item.getKidCount()));
        } else {
            holder.toggle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_expand_more_white_24dp, 0);
            holder.toggle.setText(context.getResources()
                    .getQuantityString(R.plurals.show_comments, item.getKidCount(), item.getKidCount()));
        }
    }


    private void expand(final StoryModel item) {
//        if (isExpanded(item)) return;

        savedState.expanded.putParcelable(item.getId(), item);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                int index = savedState.list.indexOf(item) + 1;
//                savedState.list.addAll(index, Arrays.asList(item.getKidItems())); // recursive
//                notifyItemRangeInserted(index, item.getKidCount());
            }
        });
    }


    private void collapse(final ItemManager.Item item) {
        int index = savedState.list.indexOf(item) + 1;
        int count = recursiveRemove(item);
        notifyItemRangeRemoved(index, count);
    }


    private int recursiveRemove(ItemManager.Item item) {
        if (!isExpanded(item)) {
            return 0;
        }
        // if item is already expanded, its kids must be added, so we need to remove them
        int count = item.getKidCount();
        savedState.expanded.remove(item.getId());
        for (ItemManager.Item kid : item.getKidItems()) {
            count += recursiveRemove(kid);
            savedState.list.remove(kid);
        }
        return count;
    }


    private boolean isExpanded(ItemManager.Item item) {
        return savedState.expanded.containsKey(item.getId());
    }


    public static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }


            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private ArrayList<StoryModel> list;
        private Bundle expanded;


        public SavedState(ArrayList<StoryModel> list) {
            this.list = list;
            expanded = new Bundle();
        }


        @SuppressWarnings("unchecked")
        private SavedState(Parcel source) {
            list = source.readArrayList(ItemManager.Item.class.getClassLoader());
            expanded = source.readBundle(list.isEmpty() ? null :
                    list.get(0).getClass().getClassLoader());
        }


        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(list);
            dest.writeBundle(expanded);
        }
    }
}