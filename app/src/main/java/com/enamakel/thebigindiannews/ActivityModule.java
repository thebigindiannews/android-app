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

import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.accounts.UserServicesClient;
import com.enamakel.thebigindiannews.activities.AboutActivity;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.activities.FavoriteActivity;
import com.enamakel.thebigindiannews.activities.ListActivity;
import com.enamakel.thebigindiannews.activities.LoginActivity;
import com.enamakel.thebigindiannews.activities.NewActivity;
import com.enamakel.thebigindiannews.activities.PopularActivity;
import com.enamakel.thebigindiannews.activities.SearchActivity;
import com.enamakel.thebigindiannews.activities.SettingsActivity;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.activities.SubmitActivity;
import com.enamakel.thebigindiannews.activities.ThreadPreviewActivity;
import com.enamakel.thebigindiannews.activities.UserActivity;
import com.enamakel.thebigindiannews.adapters.FavoriteRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.MultiPageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.SinglePageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.StoryRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.SubmissionRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adapters.ThreadPreviewRecyclerViewAdapter;
import com.enamakel.thebigindiannews.data.providers.managers.FavoriteManager;
import com.enamakel.thebigindiannews.data.providers.managers.ItemManager;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.providers.managers.ReportManager;
import com.enamakel.thebigindiannews.data.providers.managers.SessionManager;
import com.enamakel.thebigindiannews.data.providers.managers.UserManager;
import com.enamakel.thebigindiannews.data.clients.AlgoliaClient;
import com.enamakel.thebigindiannews.data.clients.AlgoliaPopularClient;
import com.enamakel.thebigindiannews.data.clients.BigIndianClient;
import com.enamakel.thebigindiannews.data.clients.HackerNewsClient;
import com.enamakel.thebigindiannews.data.clients.ReadabilityClient;
import com.enamakel.thebigindiannews.data.clients.bigindian.ReportsClient;
import com.enamakel.thebigindiannews.data.clients.bigindian.StoriesClient;
import com.enamakel.thebigindiannews.fragments.DrawerFragment;
import com.enamakel.thebigindiannews.fragments.FavoriteFragment;
import com.enamakel.thebigindiannews.fragments.ItemFragment;
import com.enamakel.thebigindiannews.fragments.ListFragment;
import com.enamakel.thebigindiannews.fragments.ReadabilityFragment;
import com.enamakel.thebigindiannews.fragments.WebFragment;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.views.StoryReportView;
import com.enamakel.thebigindiannews.widgets.PopupMenu;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module(
        injects = {
                AboutActivity.class,
                BigIndianClient.class,
                ComposeActivity.class,
                DrawerFragment.class,
                FavoriteActivity.class,
                FavoriteFragment.class,
                FavoriteRecyclerViewAdapter.class,
                ItemFragment.class,
                ListActivity.class,
                ListFragment.class,
                LoginActivity.class,
                MultiPageItemRecyclerViewAdapter.class,
                NewActivity.class,
                PopularActivity.class,
                ReadabilityFragment.class,
                ReportsClient.class,
                ReportManager.class,
                SearchActivity.class,
                SettingsActivity.class,
                SinglePageItemRecyclerViewAdapter.class,
                SingleStoryActivity.class,
                StoriesClient.class,
                StoryRecyclerViewAdapter.class,
                StoryReportView.class,
                SubmissionRecyclerViewAdapter.class,
                SubmitActivity.class,
                ThreadPreviewActivity.class,
                ThreadPreviewRecyclerViewAdapter.class,
                UserActivity.class,
                WebFragment.class
        },
        library = true
)
public class ActivityModule {
    public static final String ALGOLIA = "algolia";
    public static final String POPULAR = "popular";
    public static final String HN = "hn";

    final Context context;


    public ActivityModule(Context context) {
        this.context = context;
    }


    @Provides
    @Singleton
    public Context provideContext() {
        return context;
    }


    @Provides
    @Singleton
    @Named(HN)
    public ItemManager provideHackerNewsClient(HackerNewsClient client) {
        return client;
    }


    @Provides
    @Singleton
    @Named(ALGOLIA)
    public ItemManager provideAlgoliaClient(AlgoliaClient client) {
        return client;
    }


    @Provides
    @Singleton
    @Named(POPULAR)
    public ItemManager provideAlgoliaPopularClient(AlgoliaPopularClient client) {
        return client;
    }


    @Provides
    @Singleton
    public UserManager provideUserManager(HackerNewsClient client) {
        return client;
    }


    @Provides
    @Singleton
    public ReadabilityClient provideReadabilityClient(ReadabilityClient.Impl client) {
        return client;
    }


    @Provides
    @Singleton
    public FavoriteManager provideFavoriteManager() {
        return new FavoriteManager(context);
    }


    @Provides
    @Singleton
    public SessionManager provideSessionManager() {
        return new SessionManager();
    }


    @Provides
    @Singleton
    public RestServiceFactory provideRestServiceFactory(Context context) {
        return new RestServiceFactory.Impl(context);
    }


    @Provides
    @Singleton
    public ActionViewResolver provideActionViewResolver() {
        return new ActionViewResolver();
    }


    @Provides
    public AlertDialogBuilder provideAlertDialogBuilder(Context context) {
        return new AlertDialogBuilder.Impl();
    }


    @Provides
    @Singleton
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
