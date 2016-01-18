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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.util.Scrollable;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.MaterialisticProvider;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.widget.ItemPagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

@EActivity(R.layout.activity_item)
public class ItemActivity extends InjectableActivity implements Scrollable {
    public static final String EXTRA_ITEM = ItemActivity.class.getName() + ".EXTRA_ITEM";
    public static final String EXTRA_OPEN_COMMENTS = ItemActivity.class.getName() + ".EXTRA_OPEN_COMMENTS";
    private static final String PARAM_ID = "id";
    private static final String STATE_ITEM = "state:item";
    private static final String STATE_ITEM_ID = "state:itemId";

    private ItemManager.Item story;
    private String mItemId = null;
    private boolean mExternalBrowser;
    private Preferences.StoryViewMode mStoryViewMode;

    private boolean bookmarkedUndo;

    @Inject @Named(ActivityModule.HN) ItemManager mItemManager;
    @Inject FavoriteManager mFavoriteManager;
    @Inject AlertDialogBuilder mAlertDialogBuilder;
    @Inject UserServices mUserServices;
    @Inject SessionManager sessionManager;

    @ViewById ImageView bookmarked;
    @ViewById TabLayout tabLayout;
    @ViewById AppBarLayout appbar;
    @ViewById(R.id.content_frame) CoordinatorLayout coordinatorLayout;
    @ViewById ImageButton voteButton;
    @ViewById FloatingActionButton replyButton;

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (story == null) {
                return;
            }
            if (FavoriteManager.isCleared(uri)) {
                story.setFavorite(false);
                bindFavorite();
            } else if (TextUtils.equals(mItemId, uri.getLastPathSegment())) {
                story.setFavorite(FavoriteManager.isAdded(uri));
                bindFavorite();
            }
        }
    };


    @AfterViews
    void initializeActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP);
    }


    @AfterViews
    void initializeFromPreferences() {
        mExternalBrowser = Preferences.externalBrowserEnabled(this);
        if (getIntent().getBooleanExtra(EXTRA_OPEN_COMMENTS, false)) {
            mStoryViewMode = Preferences.StoryViewMode.Comment;
        } else {
            mStoryViewMode = Preferences.getDefaultStoryView(this);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        getContentResolver().registerContentObserver(MaterialisticProvider.URI_FAVORITE,
                true, mObserver);

        if (savedInstanceState != null) {
            story = savedInstanceState.getParcelable(STATE_ITEM);
            mItemId = savedInstanceState.getString(STATE_ITEM_ID);
        } else {
            if (Intent.ACTION_VIEW.equalsIgnoreCase(intent.getAction())) {
                if (intent.getData() != null) {
                    if (TextUtils.equals(intent.getData().getScheme(), BuildConfig.APPLICATION_ID)) {
                        mItemId = intent.getData().getLastPathSegment();
                    } else {
                        mItemId = intent.getData().getQueryParameter(PARAM_ID);
                    }
                }
            } else if (intent.hasExtra(EXTRA_ITEM)) {
                ItemManager.WebItem item = intent.getParcelableExtra(EXTRA_ITEM);
                mItemId = item.getId();
                if (item instanceof ItemManager.Item) {
                    story = (ItemManager.Item) item;
                }
            }
        }

        if (story != null) bindData();
        else if (!TextUtils.isEmpty(mItemId)) {
            mItemManager.getItem(mItemId, new ItemResponseListener(this));
        }
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
            AppUtils.openExternal(ItemActivity.this, mAlertDialogBuilder, story);
            return true;
        }

        if (item.getItemId() == R.id.menu_share) {
            AppUtils.share(ItemActivity.this, mAlertDialogBuilder, story);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ITEM, story);
        outState.putString(STATE_ITEM_ID, mItemId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }


    @Override
    public void scrollToTop() {
        appbar.setExpanded(true, true);
    }


    private void onItemLoaded(ItemManager.Item response) {
        story = response;
        supportInvalidateOptionsMenu();
        bindData();
    }


    private void bindFavorite() {
        if (story == null) return;
        if (!story.isStoryType()) return;

        bookmarked.setVisibility(View.VISIBLE);
        decorateFavorite(story.isFavorite());
    }


    @Click
    void bookmarkedClicked() {
        final int toastMessageResId;
        if (!story.isFavorite()) {
            mFavoriteManager.add(ItemActivity.this, story);
            toastMessageResId = R.string.toast_saved;
        } else {
            mFavoriteManager.remove(ItemActivity.this, story.getId());
            toastMessageResId = R.string.toast_removed;
        }

        if (!bookmarkedUndo) {
            Snackbar.make(coordinatorLayout, toastMessageResId, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bookmarkedUndo = true;
                            bookmarked.performClick();
                        }
                    })
                    .show();
        }

        bookmarkedUndo = false;
    }


    private void bindData() {
        if (story == null) return;

        bindFavorite();
        sessionManager.view(this, story.getId());
        voteButton.setVisibility(View.VISIBLE);

        final TextView titleTextView = (TextView) findViewById(android.R.id.text2);

        if (story.isStoryType()) {
            titleTextView.setText(story.getDisplayedTitle());
            if (!TextUtils.isEmpty(story.getSource())) {
                TextView sourceTextView = (TextView) findViewById(R.id.source);
                sourceTextView.setText(story.getSource());
                sourceTextView.setVisibility(View.VISIBLE);
            }

        } else AppUtils.setHtmlText(titleTextView, story.getDisplayedTitle());

        final TextView postedTextView = (TextView) findViewById(R.id.posted);
        postedTextView.setText(story.getDisplayedTime(this, false, true));
        postedTextView.setMovementMethod(LinkMovementMethod.getInstance());

        switch (story.getType()) {
            case ItemManager.Item.JOB_TYPE:
                postedTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_work_white_18dp, 0, 0, 0);
                break;
            case ItemManager.Item.POLL_TYPE:
                postedTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_poll_white_18dp, 0, 0, 0);
                break;
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.divider));
        viewPager.setPageMarginDrawable(R.color.blackT12);

        final ItemPagerAdapter adapter = new ItemPagerAdapter(this, getSupportFragmentManager(),
                story, !mExternalBrowser);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment activeFragment = adapter.getItem(viewPager.getCurrentItem());
                if (activeFragment != null) {
                    ((Scrollable) activeFragment).scrollToTop();
                }
                scrollToTop();
            }
        });

        switch (mStoryViewMode) {
            case Article:
                if (viewPager.getAdapter().getCount() == 3) {
                    viewPager.setCurrentItem(1);
                }
                break;
            case Readability:
                viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                break;
        }
        if (story.isStoryType() && mExternalBrowser) {
            findViewById(R.id.header_card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppUtils.openWebUrlExternal(ItemActivity.this,
                            story.getDisplayedTitle(),
                            story.getUrl());
                }
            });
        } else {
            findViewById(R.id.header_card_view).setClickable(false);
        }
    }


    private void decorateFavorite(boolean isFavorite) {
        bookmarked.setImageResource(isFavorite ?
                R.drawable.ic_bookmark_white_24dp : R.drawable.ic_bookmark_border_white_24dp);
    }


    @Click
    void voteButtonClicked() {
        ItemManager.Item story = this.story;
        mUserServices.voteUp(ItemActivity.this, story.getId(), new VoteCallback(this));
    }


    @Click
    void replyButtonClicked() {
        startActivity(new Intent(ItemActivity.this, ComposeActivity.class)
                .putExtra(ComposeActivity.EXTRA_PARENT_ID, story.getId())
                .putExtra(ComposeActivity.EXTRA_PARENT_TEXT, story.getText()));
    }


    private void onVoted(Boolean successful) {
        if (successful == null) {
            Toast.makeText(this, R.string.vote_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Drawable drawable = DrawableCompat.wrap(voteButton.getDrawable());
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.greenA700));
            Toast.makeText(this, R.string.voted, Toast.LENGTH_SHORT).show();
        } else {
            AppUtils.showLogin(this, mAlertDialogBuilder);
        }
    }


    private static class ItemResponseListener implements ResponseListener<ItemManager.Item> {
        private final WeakReference<ItemActivity> mItemActivity;


        public ItemResponseListener(ItemActivity itemActivity) {
            mItemActivity = new WeakReference<>(itemActivity);
        }


        @Override
        public void onResponse(ItemManager.Item response) {
            if (mItemActivity.get() != null && !mItemActivity.get().isActivityDestroyed()) {
                mItemActivity.get().onItemLoaded(response);
            }
        }


        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }


    private static class VoteCallback extends UserServices.Callback {
        private final WeakReference<ItemActivity> mItemActivity;


        public VoteCallback(ItemActivity itemActivity) {
            mItemActivity = new WeakReference<>(itemActivity);
        }


        @Override
        public void onDone(boolean successful) {
            if (mItemActivity.get() != null && !mItemActivity.get().isActivityDestroyed()) {
                mItemActivity.get().onVoted(successful);
            }
        }


        @Override
        public void onError() {
            if (mItemActivity.get() != null && !mItemActivity.get().isActivityDestroyed()) {
                mItemActivity.get().onVoted(null);
            }
        }
    }
}
