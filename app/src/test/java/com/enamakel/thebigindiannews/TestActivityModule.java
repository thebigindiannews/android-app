package com.enamakel.thebigindiannews;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAccountManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.AboutActivity;
import com.enamakel.thebigindiannews.activities.AskActivity;
import com.enamakel.thebigindiannews.activities.ComposeActivity;
import com.enamakel.thebigindiannews.activities.FavoriteActivity;
import com.enamakel.thebigindiannews.activities.SingleStoryActivity;
import com.enamakel.thebigindiannews.activities.JobsActivity;
import com.enamakel.thebigindiannews.activities.ListActivity;
import com.enamakel.thebigindiannews.activities.LoginActivity;
import com.enamakel.thebigindiannews.activities.NewActivity;
import com.enamakel.thebigindiannews.activities.PopularActivity;
import com.enamakel.thebigindiannews.activities.SearchActivity;
import com.enamakel.thebigindiannews.activities.SettingsActivity;
import com.enamakel.thebigindiannews.activities.ShowActivity;
import com.enamakel.thebigindiannews.activities.SubmitActivity;
import com.enamakel.thebigindiannews.activities.ThreadPreviewActivity;
import com.enamakel.thebigindiannews.activities.UserActivity;
import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.clients.FeedbackClient;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ReadabilityClient;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.data.UserManager;
import com.enamakel.thebigindiannews.fragments.DrawerFragment;
import com.enamakel.thebigindiannews.fragments.FavoriteFragment;
import com.enamakel.thebigindiannews.fragments.ItemFragment;
import com.enamakel.thebigindiannews.fragments.ListFragment;
import com.enamakel.thebigindiannews.fragments.ReadabilityFragment;
import com.enamakel.thebigindiannews.fragments.WebFragment;
import com.enamakel.thebigindiannews.test.TestFavoriteActivity;
import com.enamakel.thebigindiannews.test.TestItemActivity;
import com.enamakel.thebigindiannews.test.TestListActivity;
import com.enamakel.thebigindiannews.test.TestReadabilityActivity;
import com.enamakel.thebigindiannews.test.WebActivity;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;
import com.enamakel.thebigindiannews.adaptors.FavoriteRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adaptors.MultiPageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.widgets.PopupMenu;
import com.enamakel.thebigindiannews.adaptors.SinglePageItemRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adaptors.StoryRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adaptors.SubmissionRecyclerViewAdapter;
import com.enamakel.thebigindiannews.adaptors.ThreadPreviewRecyclerViewAdapter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Module(
        injects = {
                // source classes
                LoginActivity.class,
                SettingsActivity.class,
                AskActivity.class,
                AboutActivity.class,
                FavoriteActivity.class,
                SingleStoryActivity.class,
                JobsActivity.class,
                ListActivity.class,
                NewActivity.class,
                SearchActivity.class,
                ShowActivity.class,
                WebActivity.class,
                PopularActivity.class,
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
                ThreadPreviewRecyclerViewAdapter.class,
                // test classes
                AppUtilsTest.class,
                SettingsActivityTest.class,
                SearchActivityTest.class,
                ItemActivityTest.class,
                ItemFragmentMultiPageTest.class,
                ItemFragmentSinglePageTest.class,
                TestItemActivity.class,
                TestReadabilityActivity.class,
                TestListActivity.class,
                com.enamakel.thebigindiannews.test.ListActivity.class,
                ListFragmentViewHolderTest.class,
                ListFragmentViewHolderEdgeTest.class,
                FavoriteActivityTest.class,
                FavoriteActivityEmptyTest.class,
                TestFavoriteActivity.class,
                WebFragmentLocalTest.class,
                WebFragmentTest.class,
                FeedbackTest.class,
                ListFragmentTest.class,
                PopularActivityTest.class,
                ReadabilityFragmentTest.class,
                ReadabilityFragmentLazyLoadTest.class,
                LoginActivityTest.class,
                DrawerFragmentLoginTest.class,
                ComposeActivityTest.class,
                SubmitActivityTest.class,
                UserActivityTest.class,
                ThreadPreviewActivityTest.class
        },
        library = true,
        overrides = true
)
public class TestActivityModule {
    private final ItemManager hackerNewsClient = mock(ItemManager.class);
    private final ItemManager algoliaClient = mock(ItemManager.class);
    private final ItemManager algoliaPopularClient = mock(ItemManager.class);
    private final UserManager userManager = mock(UserManager.class);
    private final FavoriteManager favoriteManager = mock(FavoriteManager.class);
    private final SessionManager sessionManager = mock(SessionManager.class);
    private final SearchView searchView = mock(SearchView.class);
    private final FeedbackClient feedbackClient = mock(FeedbackClient.class);
    private final ReadabilityClient readabilityClient = mock(ReadabilityClient.class);
    private final UserServices userServices = mock(UserServices.class);

    @Provides @Singleton @Named(ActivityModule.HN)
    public ItemManager provideHackerNewsClient() {
        return hackerNewsClient;
    }

    @Provides @Singleton @Named(ActivityModule.ALGOLIA)
    public ItemManager provideAlgoliaClient() {
        return algoliaClient;
    }

    @Provides @Singleton @Named(ActivityModule.POPULAR)
    public ItemManager provideAlgoliaPopularClient() {
        return algoliaPopularClient;
    }

    @Provides @Singleton
    public FavoriteManager provideFavoriteManager() {
        return favoriteManager;
    }

    @Provides @Singleton
    public SessionManager provideSessionManager() {
        return sessionManager;
    }

    @Provides @Singleton
    public FeedbackClient provideFeedbackClient() {
        return feedbackClient;
    }

    @Provides @Singleton
    public ReadabilityClient provideReadabilityClient() {
        return readabilityClient;
    }

    @Provides @Singleton
    public UserManager provideUserManager() {
        return userManager;
    }

    @Provides @Singleton
    public ActionViewResolver provideActionViewResolver() {
        ActionViewResolver resolver = mock(ActionViewResolver.class);
        when(resolver.getActionView(any(MenuItem.class))).thenReturn(searchView);
        return resolver;
    }

    @Provides
    public AlertDialogBuilder provideAlertDialogBuilder() {
        return new AlertDialogBuilder() {
            private AlertDialog.Builder builder;

            @Override
            public AlertDialogBuilder init(Context context) {
                builder = new AlertDialog.Builder(context);
                return this;
            }

            @Override
            public AlertDialogBuilder setTitle(int titleId) {
                builder.setTitle(titleId);
                return this;
            }

            @Override
            public AlertDialogBuilder setMessage(@StringRes int messageId) {
                builder.setMessage(messageId);
                return this;
            }

            @Override
            public AlertDialogBuilder setView(View view) {
                builder.setView(view);
                return this;
            }

            @Override
            public AlertDialogBuilder setSingleChoiceItems(CharSequence[] items, int checkedItem, DialogInterface.OnClickListener listener) {
                builder.setSingleChoiceItems(items, checkedItem, listener);
                return this;
            }

            @Override
            public AlertDialogBuilder setNegativeButton(@StringRes int textId,
                                                        DialogInterface.OnClickListener listener) {
                builder.setNegativeButton(textId, listener);
                return this;
            }

            @Override
            public AlertDialogBuilder setPositiveButton(@StringRes int textId,
                                                        DialogInterface.OnClickListener listener) {
                builder.setPositiveButton(textId, listener);
                return this;
            }

            @Override
            public Dialog create() {
                return builder.create();
            }

            @Override
            public Dialog show() {
                return builder.show();
            }
        };
    }

    @Provides @Singleton
    public UserServices provideUserServices() {
        return userServices;
    }

    @Provides
    public AccountManager provideAccountManager() {
        return ShadowAccountManager.get(RuntimeEnvironment.application);
    }

    @Provides
    public PopupMenu providePopupMenu() {
        return new PopupMenu() {
            private android.widget.PopupMenu popupMenu;

            @Override
            public void create(Context context, View anchor, int gravity) {
                popupMenu = new android.widget.PopupMenu(context, anchor, gravity);
            }

            @Override
            public void inflate(@MenuRes int menuRes) {
                popupMenu.inflate(menuRes);
            }

            @Override
            public Menu getMenu() {
                return popupMenu.getMenu();
            }

            @Override
            public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
                popupMenu.setOnMenuItemClickListener(new android.widget.PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return listener.onMenuItemClick(item);
                    }
                });
            }

            @Override
            public void show() {
                popupMenu.show();
            }
        };
    }
}
