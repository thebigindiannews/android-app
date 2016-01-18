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

package com.enamakel.thebigindiannews;

import android.accounts.AccountManager;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.accounts.UserServicesClient;
import com.enamakel.thebigindiannews.data.AlgoliaClient;
import com.enamakel.thebigindiannews.data.AlgoliaPopularClient;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.FeedbackClient;
import com.enamakel.thebigindiannews.data.HackerNewsClient;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ReadabilityClient;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.data.UserManager;
import com.enamakel.thebigindiannews.widget.FavoriteRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widget.MultiPageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widget.PopupMenu;
import com.enamakel.thebigindiannews.widget.SinglePageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widget.StoryRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widget.SubmissionRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widget.ThreadPreviewRecyclerViewAdapter;

@Module(
        injects = {
                SettingsActivity.class,
                AboutActivity.class,
                AskActivity.class,
                FavoriteActivity.class,
                ItemActivity.class,
                JobsActivity.class,
                ListActivity.class,
                NewActivity.class,
                SearchActivity.class,
                ShowActivity.class,
                PopularActivity.class,
                LoginActivity.class,
                ComposeActivity.class,
                SubmitActivity.class,
                UserActivity.class,
                ThreadPreviewActivity.class,
                FavoriteFragment.class,
                ItemFragment.class,
                ListFragment.class,
                WebFragment.class,
                DrawerFragment.class,
                ReadabilityFragment.class,
                StoryRecyclerViewAdapter.class,
                FavoriteRecyclerViewAdapter.class,
                SinglePageItemRecyclerViewAdapter.class,
                MultiPageItemRecyclerViewAdapter.class,
                SubmissionRecyclerViewAdapter.class,
                ThreadPreviewRecyclerViewAdapter.class
        },
        library = true
)
public class ActivityModule {
    public static final String ALGOLIA = "algolia";
    public static final String POPULAR = "popular";
    public static final String HN = "hn";

    private final Context mContext;

    public ActivityModule(Context context) {
        mContext = context;
    }

    @Provides @Singleton
    public Context provideContext() {
        return mContext;
    }

    @Provides @Singleton @Named(HN)
    public ItemManager provideHackerNewsClient(HackerNewsClient client) {
        return client;
    }

    @Provides @Singleton @Named(ALGOLIA)
    public ItemManager provideAlgoliaClient(AlgoliaClient client) {
        return client;
    }

    @Provides @Singleton @Named(POPULAR)
    public ItemManager provideAlgoliaPopularClient(AlgoliaPopularClient client) {
        return client;
    }

    @Provides @Singleton
    public UserManager provideUserManager(HackerNewsClient client) {
        return client;
    }

    @Provides @Singleton
    public FeedbackClient provideFeedbackClient(FeedbackClient.Impl client) {
        return client;
    }

    @Provides @Singleton
    public ReadabilityClient provideReadabilityClient(ReadabilityClient.Impl client) {
        return client;
    }

    @Provides @Singleton
    public FavoriteManager provideFavoriteManager() {
        return new FavoriteManager();
    }

    @Provides @Singleton
    public SessionManager provideSessionManager() {
        return new SessionManager();
    }

    @Provides @Singleton
    public RestServiceFactory provideRestServiceFactory(Context context) {
        return new RestServiceFactory.Impl(context);
    }

    @Provides @Singleton
    public ActionViewResolver provideActionViewResolver() {
        return new ActionViewResolver();
    }

    @Provides
    public AlertDialogBuilder provideAlertDialogBuilder(Context context) {
        return new AlertDialogBuilder.Impl();
    }

    @Provides @Singleton
    public UserServices provideUserServices() {
        return new UserServicesClient(new OkHttpClient());
    }

    @Provides
    public AccountManager provideAccountManager(Context context) {
        return AccountManager.get(context);
    }

    @Provides
    public PopupMenu providePopupMenu() {
        return new PopupMenu.Impl();
    }
}
