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

package com.enamakel.thebigindiannews.activities.base;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.enamakel.thebigindiannews.ActionViewResolver;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.SearchActivity;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.adapters.StoryPagerAdapter;
import com.enamakel.thebigindiannews.data.managers.SessionManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.util.MultiPaneListener;
import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.util.Scrollable;

import javax.inject.Inject;


/**
 * List activity that renders alternative layouts for portrait/landscape
 */
public abstract class BaseListActivity extends DrawerActivity implements MultiPaneListener {
    protected static final String LIST_FRAGMENT_TAG = BaseListActivity.class.getName() +
            ".LIST_FRAGMENT_TAG";
    static final String STATE_SELECTED_ITEM = "state:selectedItem";
    static final String STATE_STORY_VIEW_MODE = "state:storyViewMode";
    static final String STATE_EXTERNAL_BROWSER = "state:useExternalBrowser";

    @Inject ActionViewResolver actionViewResolver;
    @Inject AlertDialogBuilder alertDialogBuilder;
    @Inject SessionManager sessionManager;

    boolean isMultiPane;
    boolean useExternalBrowser;
    Preferences.StoryViewMode storyViewMode;
    ViewPager viewPager;
    TabLayout tabLayout;
    FloatingActionButton replyButton;

    final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(key, getString(R.string.pref_external)))
                useExternalBrowser = Preferences.externalBrowserEnabled(BaseListActivity.this);

            else if (TextUtils.equals(key, getString(R.string.pref_story_display)))
                storyViewMode = Preferences.getDefaultStoryView(BaseListActivity.this);
        }
    };


    protected StoryModel selectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setTitle(getDefaultTitle());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);
                if (fragment instanceof Scrollable) ((Scrollable) fragment).scrollToTop();
            }
        });

        isMultiPane = getResources().getBoolean(R.bool.multi_pane);
        if (isMultiPane) {
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            viewPager = (ViewPager) findViewById(R.id.content);
            tabLayout.setVisibility(View.GONE);
            viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.divider));
            viewPager.setPageMarginDrawable(R.color.blackT12);
            viewPager.setVisibility(View.GONE);
            replyButton = (FloatingActionButton) findViewById(R.id.reply_button);
            AppUtils.toggleFab(replyButton, false);
        }

        if (savedInstanceState == null) {
            storyViewMode = Preferences.getDefaultStoryView(this);
            useExternalBrowser = Preferences.externalBrowserEnabled(this);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.list, instantiateListFragment(), LIST_FRAGMENT_TAG)
                    .commit();
        } else {
            storyViewMode = Preferences.StoryViewMode.values()[
                    savedInstanceState.getInt(STATE_STORY_VIEW_MODE, 0)
                    ];
            useExternalBrowser = savedInstanceState.getBoolean(STATE_EXTERNAL_BROWSER);
            selectedItem = savedInstanceState.getParcelable(STATE_SELECTED_ITEM);

            if (isMultiPane) openMultiPaneItem(selectedItem);
            else unbindViewPager();
        }

        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMultiPane) getMenuInflater().inflate(R.menu.menu_item, menu);

        if (isSearchable()) {
            getMenuInflater().inflate(R.menu.menu_search, menu);
            MenuItem menuSearch = menu.findItem(R.id.menu_search);
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) actionViewResolver.getActionView(menuSearch);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(
                    new ComponentName(this, SearchActivity.class)));
            searchView.setIconified(true);
            searchView.setQuery("", false);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isMultiPane) {
            menu.findItem(R.id.menu_share).setVisible(selectedItem != null);
            menu.findItem(R.id.menu_external).setVisible(selectedItem != null);
        }

        return isSearchable() || super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
//            AppUtils.share(BaseListActivity.this, alertDialogBuilder, selectedItem);
            return true;
        }

        if (item.getItemId() == R.id.menu_external) {
//            AppUtils.openExternal(BaseListActivity.this, alertDialogBuilder, selectedItem);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_SELECTED_ITEM, selectedItem);
        outState.putInt(STATE_STORY_VIEW_MODE, storyViewMode.ordinal());
        outState.putBoolean(STATE_EXTERNAL_BROWSER, useExternalBrowser);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    @NonNull
    @Override
    public ActionBar getSupportActionBar() {
        // noinspection ConstantConditions
        return super.getSupportActionBar();
    }


    @Override
    public void onItemSelected(@Nullable StoryModel item) {
        if (isMultiPane) {
            StoryModel previousItem = selectedItem;

            if (previousItem != null && item != null &&
                    TextUtils.equals(item.getId(), previousItem.getId())) return;


            if (previousItem == null && item != null ||
                    previousItem != null && item == null) supportInvalidateOptionsMenu();

            openMultiPaneItem(item);
        } else if (item != null) openSinglePaneItem(item);
        selectedItem = item;
    }


    @Override
    public StoryModel getSelectedItem() {
        return selectedItem;
    }


    @Override
    public boolean isMultiPane() {
        return isMultiPane;
    }


    /**
     * Checks if activity should have search view
     *
     * @return true if is searchable, false otherwise
     */
    protected boolean isSearchable() {
        return false;
    }


    /**
     * Gets default title to be displayed in list-only layout
     *
     * @return displayed title
     */
    protected abstract String getDefaultTitle();


    /**
     * Creates list fragment to host list data
     *
     * @return list fragment
     */
    protected abstract Fragment instantiateListFragment();


    void openSinglePaneItem(BaseCardModel item) {
        if (item instanceof StoryModel) {
            if (false && useExternalBrowser) {
                StoryModel story = (StoryModel) item;
                AppUtils.openWebUrlExternal(this, story.getTitle(), story.getUrl());
            } else {
                startActivity(new Intent(this, SingleStoryActivity.class)
                        .putExtra(SingleStoryActivity.EXTRA_ITEM, item));
            }
        }
    }


    void openMultiPaneItem(final StoryModel item) {
        if (item == null) {
            setTitle(getDefaultTitle());
            findViewById(R.id.empty_selection).setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            viewPager.setAdapter(null);
            AppUtils.toggleFab(replyButton, false);
        } else {
//            setTitle(item.getDisplayedTitle());
            findViewById(R.id.empty_selection).setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            AppUtils.toggleFab(replyButton, true);
//            replyButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startActivity(new Intent(BaseListActivity.this, ComposeActivity.class)
//                            .putExtra(ComposeActivity.EXTRA_PARENT_ID, item.getId())
//                            .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, item.getDisplayedTitle()));
//                }
//            });
            bindViewPager(item);
            sessionManager.view(this, item.getId());
        }
    }


    void bindViewPager(StoryModel item) {
        final StoryPagerAdapter adapter = new StoryPagerAdapter(this,
                getSupportFragmentManager(), item, true);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment fragment = adapter.getItem(viewPager.getCurrentItem());
                if (fragment != null) {
                    ((Scrollable) fragment).scrollToTop();
                }
            }
        });
        switch (storyViewMode) {
            case Article:
                viewPager.setCurrentItem(1);
                break;
            case Readability:
                viewPager.setCurrentItem(2);
                break;
        }
    }


    void unbindViewPager() {
        // fragment manager always restores view pager fragments,
        // even when view pager no longer exists (e.g. after rotation),
        // so we have to explicitly remove those with view pager ID
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.getId() == R.id.content) {
                transaction.remove(fragment);
            }
        }
        transaction.commit();
    }
}