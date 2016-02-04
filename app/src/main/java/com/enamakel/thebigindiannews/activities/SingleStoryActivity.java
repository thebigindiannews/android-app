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

package com.enamakel.thebigindiannews.activities;


import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.base.InjectableActivity;
import com.enamakel.thebigindiannews.adapters.StoryPagerAdapter;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.data.clients.BigIndianClient;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.util.Scrollable;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;


public class SingleStoryActivity extends InjectableActivity implements Scrollable {
    public static final String EXTRA_ITEM = SingleStoryActivity.class.getName() + ".EXTRA_ITEM";
    public static final String EXTRA_OPEN_COMMENTS = SingleStoryActivity.class.getName() + ".EXTRA_OPEN_COMMENTS";

    static final String TAG = SingleStoryActivity.class.getSimpleName();
    static final String PARAM_ID = "id";
    static final String STATE_ITEM = "state:item";
    static final String STATE_ITEM_ID = "state:itemId";

    StoryModel story;
    String itemId = null;
    boolean externalBrowser;
    boolean bookmarkedUndo;
    Preferences.StoryViewMode storyViewMode;

    @Inject @Named(ActivityModule.HN) ItemManager mItemManager;
    @Inject FavoriteManager favoriteManager;
    @Inject AlertDialogBuilder alertDialogBuilder;
    @Inject UserServices mUserServices;
    @Inject SessionManager sessionManager;
    @Inject BigIndianClient bigIndianClient;

    ImageView bookmarked;
    TabLayout tabLayout;
    AppBarLayout appbar;
    CoordinatorLayout coordinatorLayout;
    ImageButton voteButton;
    FloatingActionButton replyButton;

    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (story == null) return;


            if (FavoriteManager.isCleared(uri)) {
                story.setFavorite(false);
                bindFavorite();
            } else if (TextUtils.equals(itemId, uri.getLastPathSegment())) {

                story.setFavorite(FavoriteManager.isAdded(uri));
                bindFavorite();
            }
        }
    };


    //    @AfterViews
    void initializeActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP);
    }


    //    @AfterViews
    void initializeFromPreferences() {
        externalBrowser = Preferences.externalBrowserEnabled(this);
        if (getIntent().getBooleanExtra(EXTRA_OPEN_COMMENTS, false))
            storyViewMode = Preferences.StoryViewMode.Comment;
        else storyViewMode = Preferences.getDefaultStoryView(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLocalClassName(), "onCreate()");
        externalBrowser = Preferences.externalBrowserEnabled(this);
        externalBrowser = false;

        if (getIntent().getBooleanExtra(EXTRA_OPEN_COMMENTS, false))
            storyViewMode = Preferences.StoryViewMode.Comment;
        else storyViewMode = Preferences.getDefaultStoryView(this);

        setContentView(R.layout.activity_item);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP);

        replyButton = (FloatingActionButton) findViewById(R.id.reply_button);
        voteButton = (ImageButton) findViewById(R.id.vote_button);
        bookmarked = (ImageView) findViewById(R.id.bookmarked);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.content_frame);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        final Intent intent = getIntent();
        getContentResolver().registerContentObserver(BigIndianProvider.URI_FAVORITE,
                true, contentObserver);

        if (savedInstanceState != null) {
            story = savedInstanceState.getParcelable(STATE_ITEM);
            itemId = savedInstanceState.getString(STATE_ITEM_ID);
        } else {
            if (Intent.ACTION_VIEW.equalsIgnoreCase(intent.getAction())) {
                if (intent.getData() != null)
                    if (TextUtils.equals(intent.getData().getScheme(), BuildConfig.APPLICATION_ID))
                        itemId = intent.getData().getLastPathSegment();
                    else itemId = intent.getData().getQueryParameter(PARAM_ID);
            } else if (intent.hasExtra(EXTRA_ITEM)) {
                story = intent.getParcelableExtra(EXTRA_ITEM);
                itemId = story.getId();
            }
        }

        if (story != null) bindData();
        else if (!TextUtils.isEmpty(itemId))
            bigIndianClient.getItem(itemId, new StoryResponseListener(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_share).setVisible(story != null);
        return story != null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_external) {
            AppUtils.openExternal(SingleStoryActivity.this, alertDialogBuilder, story);
            return true;
        }

        if (item.getItemId() == R.id.menu_share) {
            AppUtils.share(SingleStoryActivity.this, alertDialogBuilder, story);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ITEM, story);
        outState.putString(STATE_ITEM_ID, itemId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contentObserver);
    }


    @Override
    public void scrollToTop() {
        appbar.setExpanded(true, true);
    }


    private void onItemLoaded(StoryModel response) {
        story = response;
        supportInvalidateOptionsMenu();
        bindData();
    }


    private void bindFavorite() {
        if (story == null) return;

        bookmarked.setVisibility(View.VISIBLE);
        bookmarked.setOnClickListener(new View.OnClickListener() {
            boolean undo;


            @Override
            public void onClick(View v) {
                final int toastMessageResId;
                if (!story.isFavorite()) {
                    favoriteManager.add(SingleStoryActivity.this, story);
                    toastMessageResId = R.string.toast_saved;
                } else {
                    favoriteManager.remove(SingleStoryActivity.this, story);
                    toastMessageResId = R.string.toast_removed;
                }
                if (!undo) {
                    Snackbar.make(coordinatorLayout, toastMessageResId, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    undo = true;
                                    bookmarked.performClick();
                                }
                            })
                            .show();
                }
                undo = false;
            }
        });

        decorateFavorite(story.isFavorite());
    }


    private void bindData() {
        if (story == null) return;

        bigIndianClient.readStory(story);

        bindFavorite();
        sessionManager.view(this, story.getId());
//        voteButton.setVisibility(View.VISIBLE);

//        replyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(SingleStoryActivity.this, ComposeActivity.class)
//                        .putExtra(ComposeActivity.EXTRA_PARENT_ID, story.getId())
//                        .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, story.getText()));
//            }
//        });

        final TextView titleTextView = (TextView) findViewById(android.R.id.text2);

//        if (story.isStoryType()) {
        titleTextView.setText(story.getTitle());
        if (!TextUtils.isEmpty(story.getSource())) {
            TextView sourceTextView = (TextView) findViewById(R.id.source);
            sourceTextView.setText(story.getSource());
            sourceTextView.setVisibility(View.VISIBLE);
        }

//        } else AppUtils.setHtmlText(titleTextView, story.getDisplayedTitle());

        final TextView postedTextView = (TextView) findViewById(R.id.posted);
//        postedTextView.setText(story.getDisplayedTime(this, false, true));
//        postedTextView.setMovementMethod(LinkMovementMethod.getInstance());

        postedTextView.setText(String.format("read %d times · %s read", story.getClicks_count(),
                story.getReadtime()));

//        switch (story.getType()) {
//            case ItemManager.Item.JOB_TYPE:
//                postedTextView.setCompoundDrawablesWithIntrinsicBounds(
//                        R.drawable.ic_work_white_18dp, 0, 0, 0);
//                break;
//            case ItemManager.Item.POLL_TYPE:
//                postedTextView.setCompoundDrawablesWithIntrinsicBounds(
//                        R.drawable.ic_poll_white_18dp, 0, 0, 0);
//                break;
//        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.divider));
        viewPager.setPageMarginDrawable(R.color.blackT12);

        final StoryPagerAdapter adapter = new StoryPagerAdapter(this, getSupportFragmentManager(),
                story, !externalBrowser);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment activeFragment = adapter.getItem(viewPager.getCurrentItem());
                if (activeFragment != null) ((Scrollable) activeFragment).scrollToTop();
                scrollToTop();
            }
        });

        switch (storyViewMode) {
            case Article:
                if (viewPager.getAdapter().getCount() == 3) viewPager.setCurrentItem(1);
                break;
            case Readability:
                viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                break;
        }

        if (externalBrowser) {
            findViewById(R.id.header_card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppUtils.openWebUrlExternal(SingleStoryActivity.this,
                            story.getTitle(),
                            story.getUrl());
                }
            });
        } else findViewById(R.id.header_card_view).setClickable(false);
    }


    private void decorateFavorite(boolean isFavorite) {
        bookmarked.setImageResource(isFavorite ?
                R.drawable.ic_bookmark_white_24dp : R.drawable.ic_bookmark_border_white_24dp);
    }


    //    @Click
    void voteButtonClicked() {
        StoryModel story = this.story;
        mUserServices.voteUp(SingleStoryActivity.this, story.getId(), new VoteCallback(this));
    }


    //    @Click
    void replyButtonClicked() {
//        startActivity(new Intent(SingleStoryActivity.this, ComposeActivity.class)
//                .putExtra(ComposeActivity.EXTRA_PARENT_ID, story.getId())
//                .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, story.getText()));
    }


    private void onVoted(Boolean successful) {
        if (successful == null) {
            Toast.makeText(this, R.string.vote_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Drawable drawable = DrawableCompat.wrap(voteButton.getDrawable());
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.greenA700));
            Toast.makeText(this, R.string.voted, Toast.LENGTH_SHORT).show();
        } else AppUtils.showLogin(this, alertDialogBuilder);

    }


    private static class StoryResponseListener implements ResponseListener<StoryModel> {
        private final WeakReference<SingleStoryActivity> weakReference;


        public StoryResponseListener(SingleStoryActivity singleStoryActivity) {
            this.weakReference = new WeakReference<>(singleStoryActivity);
        }


        @Override
        public void onResponse(StoryModel response) {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed())
                weakReference.get().onItemLoaded(response);
        }


        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }


    private static class VoteCallback extends UserServices.Callback {
        private final WeakReference<SingleStoryActivity> weakReference;


        public VoteCallback(SingleStoryActivity singleStoryActivity) {
            weakReference = new WeakReference<>(singleStoryActivity);
        }


        @Override
        public void onDone(boolean successful) {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed()) {
                weakReference.get().onVoted(successful);
            }
        }


        @Override
        public void onError() {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed())
                weakReference.get().onVoted(null);
        }
    }
}
