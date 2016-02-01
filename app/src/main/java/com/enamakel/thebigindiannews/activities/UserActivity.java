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

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.base.InjectableActivity;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.UserManager;
import com.enamakel.thebigindiannews.util.Scrollable;
import com.enamakel.thebigindiannews.widgets.CommentItemDecoration;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

public class UserActivity extends InjectableActivity implements Scrollable {
    public static final String EXTRA_USERNAME = UserActivity.class.getName() + ".EXTRA_USERNAME";
    static final String STATE_USER = "state:user";
    static final String PARAM_ID = "id";
    String username;
    UserManager.User user;
    TextView info;
    TextView about;
    RecyclerView recyclerView;
    TabLayout tabLayout;
    AppBarLayout appBarLayout;
    View emptyView;

    @Inject UserManager userManager;
    @Inject @Named(ActivityModule.HN) ItemManager itemManger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (TextUtils.isEmpty(username) && getIntent().getData() != null) {
            if (TextUtils.equals(getIntent().getData().getScheme(), BuildConfig.APPLICATION_ID))
                username = getIntent().getData().getLastPathSegment();
            else username = getIntent().getData().getQueryParameter(PARAM_ID);
        }

        if (TextUtils.isEmpty(username)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_user);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        ((TextView) findViewById(R.id.title)).setText(username);
        info = (TextView) findViewById(R.id.user_info);
        about = (TextView) findViewById(R.id.about);
        emptyView = findViewById(R.id.empty);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // no op
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // no op
            }


            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollToTop();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (savedInstanceState != null) user = savedInstanceState.getParcelable(STATE_USER);

        if (user == null) load();
        else bind();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_USER, user);
    }


    @Override
    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
        appBarLayout.setExpanded(true, true);
    }


    private void load() {
        userManager.getUser(username, new UserResponseListener(this));
    }


    private void onUserLoaded(UserManager.User response) {
        if (response != null) {
            user = response;
            bind();
        } else {
            showEmpty();
        }
    }


    private void showEmpty() {
        info.setVisibility(View.GONE);
        about.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        tabLayout.addTab(tabLayout.newTab()
                .setText(getResources().getQuantityString(R.plurals.submissions_count, 0, "").trim()));
    }


    private void bind() {
        info.setText(getString(R.string.user_info, user.getCreated(this), user.getKarma()));
        if (TextUtils.isEmpty(user.getAbout())) about.setVisibility(View.GONE);
        else AppUtils.setTextWithLinks(about, user.getAbout());

        int count = user.getItems().length;
        tabLayout.addTab(tabLayout.newTab()
                .setText(getResources().getQuantityString(R.plurals.submissions_count, count, count)));
//        recyclerView.setAdapter(new SubmissionRecyclerViewAdapter(itemManger, user.getItems()));
        recyclerView.addItemDecoration(new CommentItemDecoration(this));
    }


    private static class UserResponseListener implements ResponseListener<UserManager.User> {
        private final WeakReference<UserActivity> mUserActivity;


        public UserResponseListener(UserActivity userActivity) {
            mUserActivity = new WeakReference<>(userActivity);
        }


        @Override
        public void onResponse(UserManager.User response) {
            if (mUserActivity.get() != null && !mUserActivity.get().isActivityDestroyed())
                mUserActivity.get().onUserLoaded(response);
        }


        @Override
        public void onError(String errorMessage) {
            if (mUserActivity.get() != null && !mUserActivity.get().isActivityDestroyed())
                Toast.makeText(mUserActivity.get(), R.string.user_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
