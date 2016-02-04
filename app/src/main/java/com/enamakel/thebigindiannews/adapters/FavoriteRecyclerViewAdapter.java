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

package com.enamakel.thebigindiannews.adapters;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.MenuTintDelegate;
import com.enamakel.thebigindiannews.widgets.PopupMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FavoriteRecyclerViewAdapter extends ListRecyclerViewAdapter
        <ListRecyclerViewAdapter.ItemViewHolder, StoryModel> {
    static String TAG = FavoriteRecyclerViewAdapter.class.getSimpleName();

    public interface ActionModeDelegate {
        boolean startActionMode(ActionMode.Callback callback);


        boolean isInActionMode();


        void stopActionMode();
    }


    final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        boolean pendingClear;


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_favorite_action, menu);
            menuTintDelegate.onOptionsMenuCreated(menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }


        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.menu_clear) {
                alertDialogBuilder
                        .init(context)
                        .setMessage(R.string.confirm_clear_selected)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pendingClear = true;
                                        removeSelection();
                                        actionMode.finish();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
                return true;
            }

            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if (!isAttached()) return;

            actionModeDelegate.stopActionMode();
            if (pendingClear) pendingClear = false;
            else selected.clear();

            notifyDataSetChanged();
        }
    };

    ActionModeDelegate actionModeDelegate;
    MenuTintDelegate menuTintDelegate;
    FavoriteManager.Cursor cursor;
    ArrayMap<Integer, String> selected = new ArrayMap<>();
    int pendingAdd = -1;


    public FavoriteRecyclerViewAdapter(ActionModeDelegate actionModeDelegate) {
        super();
        this.actionModeDelegate = actionModeDelegate;
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        menuTintDelegate = new MenuTintDelegate();
        menuTintDelegate.onActivityCreated(context);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }


            @Override
            public int getSwipeDirs(RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder) {
                if (actionModeDelegate.isInActionMode()) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                dismiss(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }


    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        actionModeDelegate = null;

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(R.layout.item_favorite, parent, false));
    }


    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }


    @Override
    protected void bindItem(final ItemViewHolder holder) {
        final StoryModel favorite = getItem(holder.getAdapterPosition());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (actionModeDelegate.startActionMode(actionModeCallback)) {
                    toggle(favorite.getId(), holder.getAdapterPosition());
                    return true;
                }

                return false;
            }
        });
        holder.storyView.getMoreOptions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(v, favorite);
            }
        });
    }


    @Override
    protected boolean isItemAvailable(StoryModel item) {
        return item != null;
    }


    @Override
    protected void handleItemClick(StoryModel item, ItemViewHolder holder) {
        if (!actionModeDelegate.isInActionMode()) super.handleItemClick(item, holder);
        else toggle(item.getId(), holder.getLayoutPosition());
    }


    @Override
    protected StoryModel getItem(int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return null;
        return cursor.getFavorite();
    }


    @Override
    protected boolean isSelected(String itemId) {
        return super.isSelected(itemId) || selected.containsValue(itemId);
    }


    public void setCursor(FavoriteManager.Cursor cursor) {
        this.cursor = cursor;
        if (cursor == null) {
            notifyDataSetChanged();
            return;
        }

        if (!selected.isEmpty()) { // has pending removals, notify removed
            List<Integer> positions = new ArrayList<>(selected.keySet());
            Collections.sort(positions);
            selected.clear();

            for (int i = positions.size() - 1; i >= 0; i--)
                notifyItemRemoved(positions.get(i));

        } else if (pendingAdd >= 0) { // has pending insertion, notify inserted
            notifyItemInserted(pendingAdd);
            pendingAdd = -1;
        } else
            // no pending changes, simply refresh list
            notifyDataSetChanged();

    }


    void removeSelection() {
        favoriteManager.remove(context, selected.values());
    }


    void dismiss(final int position) {
        final StoryModel story = getItem(position);
        selected.put(position, story.getId());
        favoriteManager.remove(context, selected.values());
        Snackbar.make(recyclerView, R.string.toast_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pendingAdd = position;
                        favoriteManager.add(context, story);
                    }
                })
                .show();
    }


    void toggle(String itemId, int position) {
        if (selected.containsValue(itemId)) selected.remove(position);
        else selected.put(position, itemId);
        notifyItemChanged(position);
    }


    void showMoreOptions(View view, final StoryModel story) {
        popupMenu.create(context, view, Gravity.NO_GRAVITY);
        popupMenu.inflate(R.menu.menu_contextual_favorite);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_contextual_share:
                        AppUtils.share(FavoriteRecyclerViewAdapter.this.context, alertDialogBuilder, story);
                        return true;

                    case R.id.menu_contextual_open:
                        context.startActivity(
                                new Intent(context, SingleStoryActivity.class)
                                        .putExtra(SingleStoryActivity.EXTRA_ITEM, story)
                                        .putExtra(SingleStoryActivity.EXTRA_OPEN_COMMENTS, true));
                        return true;
                }

//                if (item.getItemId() == R.id.menu_contextual_comment) {
//                    context.startActivity(new Intent(context, ComposeActivity.class)
//                            .putExtra(ComposeActivity.EXTRA_PARENT_ID, story.get_id())
//                            .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, story.getTitle()));
//                    return true;
//                }
//
//                if (item.getItemId() == R.id.menu_contextual_profile) {
//                    context.startActivity(new Intent(context, UserActivity.class)
//                            .putExtra(UserActivity.EXTRA_USERNAME, story.getCreated_by()));
//                    return true;
//                }

                return false;
            }
        });
        popupMenu.show();
//        popupMenu.create(context, v, Gravity.NO_GRAVITY);
//        popupMenu.inflate(R.menu.menu_contextual_favorite);
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                if (menuItem.getItemId() == R.id.menu_contextual_comment) {
//                    context.startActivity(new Intent(context, ComposeActivity.class)
//                            .putExtra(ComposeActivity.EXTRA_PARENT_ID, item.getId())
//                            .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, item.getTitle()));
//                    return true;
//                }
//
//                return false;
//            }
//        });
//        popupMenu.show();
    }
}
