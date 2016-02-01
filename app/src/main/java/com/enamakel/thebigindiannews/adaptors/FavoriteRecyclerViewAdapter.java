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

package com.enamakel.thebigindiannews.adaptors;

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
import android.widget.Toast;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.data.Favorite;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.util.MenuTintDelegate;
import com.enamakel.thebigindiannews.widgets.PopupMenu;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteRecyclerViewAdapter extends ListRecyclerViewAdapter
        <ListRecyclerViewAdapter.ItemViewHolder, Favorite> {

    public interface ActionModeDelegate {

        boolean startActionMode(ActionMode.Callback callback);


        boolean isInActionMode();


        void stopActionMode();
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        private boolean mPendingClear;


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_favorite_action, menu);
            mMenuTintDelegate.onOptionsMenuCreated(menu);
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
                                        mPendingClear = true;
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
            if (!isAttached()) {
                return;
            }
            mActionModeDelegate.stopActionMode();
            if (mPendingClear) {
                mPendingClear = false;
            } else {
                mSelected.clear();
            }
            notifyDataSetChanged();
        }
    };
    private ActionModeDelegate mActionModeDelegate;
    private MenuTintDelegate mMenuTintDelegate;
    private FavoriteManager.Cursor mCursor;
    private ArrayMap<Integer, String> mSelected = new ArrayMap<>();
    private int mPendingAdd = -1;


    public FavoriteRecyclerViewAdapter(ActionModeDelegate actionModeDelegate) {
        super();
        mActionModeDelegate = actionModeDelegate;
    }


    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mMenuTintDelegate = new MenuTintDelegate();
        mMenuTintDelegate.onActivityCreated(context);
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
                if (mActionModeDelegate.isInActionMode()) {
                    return 0;
                }
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
        mActionModeDelegate = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(inflater.inflate(R.layout.item_favorite, parent, false));
    }


    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }


    @Override
    protected void bindItem(final ItemViewHolder holder) {
        final Favorite favorite = getItem(holder.getAdapterPosition());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mActionModeDelegate.startActionMode(mActionModeCallback)) {
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
    protected boolean isItemAvailable(Favorite item) {
        return item != null;
    }


    @Override
    protected void handleItemClick(Favorite item, ItemViewHolder holder) {
        if (!mActionModeDelegate.isInActionMode()) super.handleItemClick(item, holder);
        else toggle(item.getId(), holder.getLayoutPosition());

    }


    @Override
    protected Favorite getItem(int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) return null;
        return mCursor.getFavorite();
    }


    @Override
    protected boolean isSelected(String itemId) {
        return super.isSelected(itemId) || mSelected.containsValue(itemId);
    }


    public void setCursor(FavoriteManager.Cursor cursor) {
        mCursor = cursor;
        if (cursor == null) {
            notifyDataSetChanged();
            return;
        }
        if (!mSelected.isEmpty()) { // has pending removals, notify removed
            List<Integer> positions = new ArrayList<>(mSelected.keySet());
            Collections.sort(positions);
            mSelected.clear();
            for (int i = positions.size() - 1; i >= 0; i--) {
                notifyItemRemoved(positions.get(i));
            }
        } else if (mPendingAdd >= 0) { // has pending insertion, notify inserted
            notifyItemInserted(mPendingAdd);
            mPendingAdd = -1;
        } else { // no pending changes, simply refresh list
            notifyDataSetChanged();
        }
    }


    private void removeSelection() {
        favoriteManager.remove(context, mSelected.values());
    }


    private void dismiss(final int position) {
        final Favorite item = getItem(position);
        mSelected.put(position, item.getId());
        favoriteManager.remove(context, mSelected.values());
        Snackbar.make(recyclerView, R.string.toast_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPendingAdd = position;
//                        favoriteManager.add(context, item);
                    }
                })
                .show();
    }


    private void toggle(String itemId, int position) {
        if (mSelected.containsValue(itemId)) {
            mSelected.remove(position);
        } else {
            mSelected.put(position, itemId);
        }
        notifyItemChanged(position);
    }


    private void showMoreOptions(View v, final Favorite item) {
        popupMenu.create(context, v, Gravity.NO_GRAVITY);
        popupMenu.inflate(R.menu.menu_contextual_favorite);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_contextual_vote) {
                    vote(item);
                    return true;
                }

                if (menuItem.getItemId() == R.id.menu_contextual_comment) {
                    context.startActivity(new Intent(context, ComposeActivity.class)
                            .putExtra(ComposeActivity.EXTRA_PARENT_ID, item.getId())
                            .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, item.getDisplayedTitle()));
                    return true;
                }

                return false;
            }
        });
        popupMenu.show();
    }


    private void vote(final Favorite item) {
        userServices.voteUp(context, item.getId(), new VoteCallback(this));
    }


    private void onVoted(Boolean successful) {
        if (successful == null) {
            Toast.makeText(context, R.string.vote_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Toast.makeText(context, R.string.voted, Toast.LENGTH_SHORT).show();
        } else {
            AppUtils.showLogin(context, alertDialogBuilder);
        }
    }


    private static class VoteCallback extends UserServices.Callback {
        private final WeakReference<FavoriteRecyclerViewAdapter> adapter;


        public VoteCallback(FavoriteRecyclerViewAdapter adapter) {
            this.adapter = new WeakReference<>(adapter);
        }


        @Override
        public void onDone(boolean successful) {
            if (adapter.get() != null && adapter.get().isAttached())
                adapter.get().onVoted(successful);
        }


        @Override
        public void onError() {
            if (adapter.get() != null && adapter.get().isAttached()) adapter.get().onVoted(null);
        }
    }
}
