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

package com.enamakel.thebigindiannews.fragments;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.ActionViewResolver;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.FavoriteActivity;
import com.enamakel.thebigindiannews.adapters.FavoriteRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.ListRecyclerViewAdapter;
import com.enamakel.thebigindiannews.data.managers.FavoriteManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.fragments.base.BaseListFragment;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;

import java.util.ArrayList;

import javax.inject.Inject;


public class FavoriteFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        FavoriteRecyclerViewAdapter.ActionModeDelegate {

    public static final String EXTRA_FILTER = FavoriteFragment.class.getName() + ".EXTRA_FILTER";

    static final String TAG = FavoriteFragment.class.getSimpleName();
    static final String STATE_FILTER = "state:filter";
    static final String STATE_SEARCH_VIEW_EXPANDED = "state:searchViewExpanded";

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<StoryModel> favorites =
                    intent.getParcelableArrayListExtra(FavoriteManager.ACTION_GET_EXTRA_DATA);
            export(favorites);
        }
    };
    final FavoriteRecyclerViewAdapter adapter = new FavoriteRecyclerViewAdapter(this);

    @Inject FavoriteManager favoriteManager;
    @Inject ActionViewResolver actionViewResolver;
    @Inject AlertDialogBuilder alertDialogBuilder;

    ProgressDialog progressDialog;
    ActionMode actionMode;
    String filter;
    boolean searchViewExpanded;
    View emptySearchView;
    View emptyView;


    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                FavoriteManager.makeGetIntentFilter());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(STATE_FILTER);
            searchViewExpanded = savedInstanceState.getBoolean(STATE_SEARCH_VIEW_EXPANDED);
        } else filter = getArguments().getString(EXTRA_FILTER);
    }


    @Override
    protected void loadNextPage() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater(savedInstanceState)
                .inflate(R.layout.fragment_favorite, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        emptySearchView = view.findViewById(R.id.empty_search);
        emptyView = view.findViewById(R.id.empty);
        emptyView.findViewById(R.id.header_card_view)
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        View bookmark = emptyView.findViewById(R.id.bookmarked);
                        bookmark.setVisibility(bookmark.getVisibility() == View.VISIBLE ?
                                View.INVISIBLE : View.VISIBLE);
                        return true;
                    }
                });
        emptyView.setVisibility(View.INVISIBLE);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(FavoriteManager.LOADER, null, this);
    }


    @Override
    protected void createOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        createSearchView(menu.findItem(R.id.menu_search));
        if (adapter.getItemCount() > 0) {
            inflater.inflate(R.menu.menu_favorite, menu);
            super.createOptionsMenu(menu, inflater);
        }
    }


    @Override
    protected void prepareOptionsMenu(Menu menu) {
        // allow clearing filter if empty, or filter if non-empty
        menu.findItem(R.id.menu_search).setVisible(!TextUtils.isEmpty(filter) ||
                adapter.getItemCount() > 0);

        if (adapter.getItemCount() > 0) super.prepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear) {
            clear();
            return true;
        }

        if (item.getItemId() == R.id.menu_email) {
            startExport();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, filter);
        outState.putBoolean(STATE_SEARCH_VIEW_EXPANDED, searchViewExpanded);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        recyclerView.setAdapter(null); // detach adapter
        if (actionMode != null) actionMode.finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (!TextUtils.isEmpty(filter))
            return new FavoriteManager.CursorLoader(getActivity(), filter);

        return new FavoriteManager.CursorLoader(getActivity());
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swapCursor(data == null ? null : new FavoriteManager.Cursor(data));
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swapCursor(null);
    }


    /**
     * Filters list data by given query
     *
     * @param query query used to filter data
     */
    public void filter(String query) {
        searchViewExpanded = false;
        filter = query;
        getLoaderManager().restartLoader(FavoriteManager.LOADER, null, this);
    }


    @Override
    protected ListRecyclerViewAdapter getAdapter() {
        return adapter;
    }


    @Override
    public boolean startActionMode(ActionMode.Callback callback) {
        if (searchViewExpanded) return false;

        if (actionMode == null)
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);

        return true;
    }


    @Override
    public boolean isInActionMode() {
        return actionMode != null && !searchViewExpanded;
    }


    @Override
    public void stopActionMode() {
        actionMode = null;
    }


    void swapCursor(FavoriteManager.Cursor cursor) {
        if (cursor != null)
            cursor.setNotificationUri(getContext().getContentResolver(),
                    BigIndianProvider.URI_FAVORITE);

        adapter.setCursor(cursor);

        if (!isDetached()) {
            toggleEmptyView(adapter.getItemCount() == 0, filter);
            getActivity().supportInvalidateOptionsMenu();
        }
    }


    void toggleEmptyView(boolean isEmpty, String filter) {
        if (isEmpty) {
            if (TextUtils.isEmpty(filter)) {
                emptySearchView.setVisibility(View.INVISIBLE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.bringToFront();
            } else {
                emptyView.setVisibility(View.INVISIBLE);
                emptySearchView.setVisibility(View.VISIBLE);
                emptySearchView.bringToFront();
            }
        } else {
            emptyView.setVisibility(View.INVISIBLE);
            emptySearchView.setVisibility(View.INVISIBLE);
        }
    }


    void createSearchView(MenuItem menuSearch) {
        final SearchView searchView = (SearchView) actionViewResolver.getActionView(menuSearch);
        searchView.setQueryHint(getString(R.string.hint_search_saved_stories));
        searchView.setSearchableInfo(((SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE))
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(!searchViewExpanded);
        searchView.setQuery(filter, false);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewExpanded = true;
                v.requestFocus();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // trigger a dummy empty search intent, as empty query does not build submitted
                searchView.setQuery(FavoriteActivity.EMPTY_QUERY, true);
                return false;
            }
        });
    }


    /**
     * Remove all the stories from the favorites
     */
    void clear() {
        alertDialogBuilder
                .init(getActivity())
                .setMessage(R.string.confirm_clear)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                favoriteManager.clear(getActivity(), filter);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }


    void startExport() {
        if (progressDialog == null)
            progressDialog = ProgressDialog.show(getActivity(), null,
                    getString(R.string.preparing), true, true);
        else progressDialog.show();

        favoriteManager.get(getActivity(), filter);
    }


    void export(ArrayList<StoryModel> favorites) {
        if (progressDialog != null) progressDialog.dismiss();

        final Intent intent = AppUtils.makeEmailIntent(
                getString(R.string.favorite_email_subject),
                makeEmailContent(favorites));

        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }


    String makeEmailContent(ArrayList<StoryModel> favorites) {
        return TextUtils.join("\n\n", favorites);
    }
}