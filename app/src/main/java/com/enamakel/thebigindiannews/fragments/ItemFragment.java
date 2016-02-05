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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.adapters.ItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.SinglePageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.data.managers.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.enamakel.thebigindiannews.fragments.base.LazyLoadFragment;
import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.util.Scrollable;
import com.enamakel.thebigindiannews.widgets.CommentItemDecoration;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;


public class ItemFragment extends LazyLoadFragment implements Scrollable {
    public static final String EXTRA_ITEM = ItemFragment.class.getName() + ".EXTRA_ITEM";
    static final String STATE_ITEM = "state:story";
    static final String STATE_ITEM_ID = "state:itemId";
    static final String STATE_ADAPTER_ITEMS = "state:adapterItems";
    static final String STATE_COLOR_CODED = "state:colorCoded";
    static final String STATE_DISPLAY_OPTION = "state:displayOption";
    static final String STATE_MAX_LINES = "state:maxLines";
    static final String STATE_USERNAME = "state:username";
    RecyclerView recyclerView;
    View emptyView;
    StoryModel story;
    String itemId;

    @Inject @Named(ActivityModule.HN) ItemManager itemManager;

    SwipeRefreshLayout swipeRefreshLayout;
    SinglePageItemRecyclerViewAdapter.SavedState adapterItems;
    ItemRecyclerViewAdapter adapter;
    boolean isColorCoded = true;
    String[] displayOptionValues;
    String[] maxLinesOptionValues;
    String displayOption;
    int maxLines;
    String username;

    final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (TextUtils.equals(key, getString(R.string.pref_color_code))) {
                        isColorCoded = Preferences.colorCodeEnabled(getActivity());
                        toggleColorCode();
                    } else if (TextUtils.equals(key, getString(R.string.pref_comment_display))) {
                        displayOption = Preferences.getCommentDisplayOption(getActivity());
                        eagerLoad();
                    } else if (TextUtils.equals(key, getString(R.string.pref_max_lines))) {
                        maxLines = Preferences.getCommentMaxLines(getActivity());
                        setMaxLines();
                    } else if (TextUtils.equals(key, getString(R.string.pref_username))) {
                        username = Preferences.getUsername(getActivity());
                        setHighlightUsername();
                    }
                }
            };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(preferenceListener);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            story = savedInstanceState.getParcelable(STATE_ITEM);
            itemId = savedInstanceState.getString(STATE_ITEM_ID);
            adapterItems = savedInstanceState.getParcelable(STATE_ADAPTER_ITEMS);
            isColorCoded = savedInstanceState.getBoolean(STATE_COLOR_CODED);
            displayOption = savedInstanceState.getString(STATE_DISPLAY_OPTION);
            maxLines = savedInstanceState.getInt(STATE_MAX_LINES, Integer.MAX_VALUE);
            username = savedInstanceState.getString(STATE_USERNAME);
        } else {
            BaseCardModel item = getArguments().getParcelable(EXTRA_ITEM);
            if (item instanceof StoryModel) story = (StoryModel) item;

            itemId = item != null ? item.getId() : null;
            isColorCoded = Preferences.colorCodeEnabled(getActivity());
            displayOption = Preferences.getCommentDisplayOption(getActivity());
            maxLines = Preferences.getCommentMaxLines(getActivity());
            username = Preferences.getUsername(getActivity());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_item, container, false);
        emptyView = view.findViewById(R.id.empty);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CommentItemDecoration(getActivity()));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.redA200);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (TextUtils.isEmpty(itemId)) return;

                loadKidData();
            }
        });
        return view;
    }


    @Override
    protected void createOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_view, menu);
        displayOptionValues = getResources().getStringArray(R.array.pref_comment_display_values);
        SubMenu subMenu = menu.findItem(R.id.menu_thread).getSubMenu();
        String[] options = getResources().getStringArray(R.array.pref_comment_display_options);

        for (int i = 0; i < options.length; i++)
            subMenu.add(R.id.menu_thread_group, Menu.NONE, i, options[i]);

        subMenu.setGroupCheckable(R.id.menu_thread_group, true, true);
        maxLinesOptionValues = getResources().getStringArray(R.array.comment_max_lines_values);
        subMenu = menu.findItem(R.id.menu_max_lines).getSubMenu();
        options = getResources().getStringArray(R.array.comment_max_lines_options);

        for (int i = 0; i < options.length; i++)
            subMenu.add(R.id.menu_max_lines_group, Menu.NONE, i, options[i]);

        subMenu.setGroupCheckable(R.id.menu_max_lines_group, true, true);
    }


    @Override
    protected void prepareOptionsMenu(Menu menu) {
        MenuItem itemColorCode = menu.findItem(R.id.menu_color_code);
        itemColorCode.setEnabled(adapter != null &&
                adapter instanceof SinglePageItemRecyclerViewAdapter);
        itemColorCode.setChecked(itemColorCode.isEnabled() && isColorCoded);
        SubMenu subMenuThread = menu.findItem(R.id.menu_thread).getSubMenu();

        for (int i = 0; i < displayOptionValues.length; i++)
            if (TextUtils.equals(displayOption, displayOptionValues[i]))
                subMenuThread.getItem(i).setChecked(true);

        SubMenu subMenuMaxLines = menu.findItem(R.id.menu_max_lines).getSubMenu();

        for (int i = 0; i < maxLinesOptionValues.length; i++) {
            int value = Integer.parseInt(maxLinesOptionValues[i]);

            if (value == -1) value = Integer.MAX_VALUE;
            if (maxLines == value) subMenuMaxLines.getItem(i).setChecked(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_color_code) {
            Preferences.setColorCodeEnabled(getActivity(), !isColorCoded);
            return true;
        }

        if (item.getGroupId() == R.id.menu_thread_group) {
            if (item.isChecked()) return true;

            Preferences.setCommentDisplayOption(getActivity(), displayOptionValues[item.getOrder()]);
            return true;
        }

        if (item.getGroupId() == R.id.menu_max_lines_group) {
            if (item.isChecked()) return true;

            Preferences.setCommentMaxLines(getActivity(), maxLinesOptionValues[item.getOrder()]);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ITEM, story);
        outState.putString(STATE_ITEM_ID, itemId);
        outState.putParcelable(STATE_ADAPTER_ITEMS, adapterItems);
        outState.putBoolean(STATE_COLOR_CODED, isColorCoded);
        outState.putString(STATE_DISPLAY_OPTION, displayOption);
        outState.putInt(STATE_MAX_LINES, maxLines);
        outState.putString(STATE_USERNAME, username);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        recyclerView.setAdapter(null);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(preferenceListener);
    }


    @Override
    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }


    @Override
    protected void load() {
        if (story != null) bindKidData();
        else if (!TextUtils.isEmpty(itemId)) loadKidData();
    }


    void loadKidData() {
//        itemManager.getItem(itemId, new StoryResponseListener(this));
    }


    void onItemLoaded(StoryModel item) {
        swipeRefreshLayout.setRefreshing(false);
        if (item != null) {
            adapterItems = null;
            this.story = item;
            bindKidData();
        }
    }


    void bindKidData() {
        if (story == null /*|| story.getKidCount() == 0*/) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        if (Preferences.isSinglePage(getActivity(), displayOption)) {
            boolean autoExpand = Preferences.isAutoExpand(getActivity(), displayOption);
            // if collapsed or no saved state then start a fresh (adapter items all collapsed)
            if (!autoExpand || adapterItems == null) {
//                adapterItems = new SinglePageItemRecyclerViewAdapter.SavedState(
//                        new ArrayList<>(Arrays.asList(story.getKidItems())));
            }

            adapter = new SinglePageItemRecyclerViewAdapter(itemManager, adapterItems, autoExpand);
            ((SinglePageItemRecyclerViewAdapter) adapter).toggleColorCode(isColorCoded);
        } else {
//            adapter = new MultiPageItemRecyclerViewAdapter(itemManager, story.getKidItems());
        }

        adapter.setMaxLines(maxLines);
        adapter.setHighlightUsername(username);
        invalidateOptionsMenu();
        recyclerView.setAdapter(adapter);
    }


    void toggleColorCode() {
        if (adapter == null || !(adapter instanceof SinglePageItemRecyclerViewAdapter)) return;

        invalidateOptionsMenu();
        ((SinglePageItemRecyclerViewAdapter) adapter).toggleColorCode(isColorCoded);
    }


    void setMaxLines() {
        if (adapter == null) return;
        invalidateOptionsMenu();
        adapter.setMaxLines(maxLines);
    }


    void setHighlightUsername() {
        if (adapter == null) return;
        adapter.setHighlightUsername(username);
    }


    void invalidateOptionsMenu() {
        if (!isAttached()) return;
        getActivity().supportInvalidateOptionsMenu();
    }


    static class ItemResponseListener implements ResponseListener<StoryModel> {
        WeakReference<ItemFragment> weakReference;


        public ItemResponseListener(ItemFragment itemFragment) {
            weakReference = new WeakReference<>(itemFragment);
        }


        @Override
        public void onResponse(StoryModel response) {
            if (weakReference.get() != null && weakReference.get().isAttached())
                weakReference.get().onItemLoaded(response);
        }


        @Override
        public void onError(String errorMessage) {
            if (weakReference.get() != null && weakReference.get().isAttached())
                weakReference.get().onItemLoaded(null);
        }
    }
}