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


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.adapters.ListRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.StoryRecyclerViewAdapter;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.clients.BigIndianClient;
import com.enamakel.thebigindiannews.data.clients.FetchMode;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.fragments.base.BaseListFragment;
import com.enamakel.thebigindiannews.util.Preferences;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class ListFragment extends BaseListFragment {
    public static final String EXTRA_ITEM_MANAGER = ListFragment.class.getName() + ".EXTRA_ITEM_MANAGER";
    public static final String EXTRA_FILTER = ListFragment.class.getName() + ".EXTRA_FILTER";
    public static final String EXTRA_FILTER2 = ListFragment.class.getName() + ".EXTRA_FILTER2";

    static final String STATE_FILTER = "state:filter";
    final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(getActivity().getString(R.string.pref_highlight_updated))) {
                        adapter.setHighlightUpdated(sharedPreferences.getBoolean(key, true));
                        adapter.notifyDataSetChanged();
                    } else if (key.equals(getActivity().getString(R.string.pref_username))) {
                        adapter.setUsername(Preferences.getUsername(getActivity()));
                        adapter.notifyDataSetChanged();
                    }
                }
            };
    final StoryRecyclerViewAdapter adapter = new StoryRecyclerViewAdapter();
    SwipeRefreshLayout swipeRefreshLayout;

    View errorView;
    View emptyView;
    RefreshCallback refreshCallback;
    String filter;
    FetchMode fetchMode = FetchMode.TOP_STORIES;

    boolean hasCompleted = false;
    boolean isLoading = false;
    int currentPage = 1;

    @Inject BigIndianClient bigIndianClient;

    public interface RefreshCallback {
        void onRefreshed();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RefreshCallback) refreshCallback = (RefreshCallback) context;
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(preferenceListener);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) filter = savedInstanceState.getString(STATE_FILTER);
        else {
            filter = getArguments().getString(EXTRA_FILTER);
            fetchMode = FetchMode.valueOf(getArguments().getString(EXTRA_FILTER2));
            if (fetchMode == null) fetchMode = FetchMode.TOP_STORIES;

            adapter.setHighlightUpdated(Preferences.highlightUpdatedEnabled(getActivity()));
            adapter.setUsername(Preferences.getUsername(getActivity()));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_list, container, false);
        errorView = view.findViewById(R.id.empty);
        emptyView = view.findViewById(R.id.empty_search);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);

        // set colors for the little refresh circle
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(
                AppUtils.getThemedResId(getActivity(), R.attr.colorAccent));

        if (savedInstanceState == null)
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (adapter.getItems() != null) adapter.notifyDataSetChanged();
        else refresh();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_FILTER, filter);
    }


    @Override
    public void onDetach() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(preferenceListener);
        refreshCallback = null;
        recyclerView.setAdapter(null); // force adapter detach
        super.onDetach();
    }


    public void filter(String filter) {
        this.filter = filter;
        adapter.setHighlightUpdated(false);
        swipeRefreshLayout.setRefreshing(true);
        refresh();
    }


    @Override
    protected ListRecyclerViewAdapter getAdapter() {
        return adapter;
    }


    private void refresh() {
        adapter.setShowAll(true);
        currentPage = 1;
        bigIndianClient.getStories(fetchMode, currentPage, new RefreshListener(this));
    }


    @Override
    protected void loadNextPage() {
        if (hasCompleted || isLoading) return;

        isLoading = true;
        currentPage++;
        bigIndianClient.getStories(fetchMode, currentPage, new NextPageListener(this));
    }


    private void onItemsLoaded(ArrayList<StoryModel> items) {
        if (!isAttached()) return;

        if (items == null) {
            swipeRefreshLayout.setRefreshing(false);

            if (adapter.getItems() == null || adapter.getItems().isEmpty()) {
                // TODO make refreshing indicator visible in error view
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
                errorView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), getString(R.string.connection_error),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            adapter.setItems(items);

            if (items.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            errorView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);

            if (refreshCallback != null) refreshCallback.onRefreshed();
        }
    }


    class RefreshListener implements ResponseListener<List<StoryModel>> {
        private final WeakReference<ListFragment> listFragment;


        public RefreshListener(ListFragment listFragment) {
            this.listFragment = new WeakReference<>(listFragment);
        }


        @Override
        public void onResponse(final List<StoryModel> response) {
            if (listFragment.get() != null && listFragment.get().isAttached())
                listFragment.get().onItemsLoaded(new ArrayList(response));
        }


        @Override
        public void onError(String errorMessage) {
            if (listFragment.get() != null && listFragment.get().isAttached())
                listFragment.get().onItemsLoaded(null);
        }
    }


    class NextPageListener implements ResponseListener<List<StoryModel>> {
        final WeakReference<ListFragment> listFragment;


        public NextPageListener(ListFragment listFragment) {
            this.listFragment = new WeakReference<>(listFragment);
        }


        @Override
        public void onResponse(final List<StoryModel> response) {
            isLoading = false;
            if (listFragment.get() != null && listFragment.get().isAttached()) {
                if (response != null && response.size() > 0) adapter.addItems(response);
                else hasCompleted = true;
            }
        }


        @Override
        public void onError(String errorMessage) {
            isLoading = false;
        }
    }
}