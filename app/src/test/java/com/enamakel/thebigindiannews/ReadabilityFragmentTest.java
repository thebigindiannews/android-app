package com.enamakel.thebigindiannews;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.MenuItem;
import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowNetworkInfo;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;

import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ReadabilityClient;
import com.enamakel.thebigindiannews.fragments.ReadabilityFragment;
import com.enamakel.thebigindiannews.test.ShadowNestedScrollView;
import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;
import com.enamakel.thebigindiannews.test.TestReadabilityActivity;
import com.enamakel.thebigindiannews.test.TestWebItem;
import com.enamakel.thebigindiannews.util.Preferences;

import static junit.framework.Assert.assertEquals;
import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowNestedScrollView.class, ShadowSupportPreferenceManager.class})
@RunWith(RobolectricGradleTestRunner.class)
public class ReadabilityFragmentTest {
    private TestReadabilityActivity activity;
    private ActivityController<TestReadabilityActivity> controller;
    @Inject ReadabilityClient readabilityClient;
    @Captor ArgumentCaptor<ReadabilityClient.Callback> callback;
    private ReadabilityFragment fragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestApplication.applicationGraph.inject(this);
        reset(readabilityClient);
        controller = Robolectric.buildActivity(TestReadabilityActivity.class);
        activity = controller.create().start().resume().visible().get();
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(activity.getString(R.string.pref_lazy_load), false)
                .commit();
        shadowOf((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE))
                .setActiveNetworkInfo(ShadowNetworkInfo.newInstance(null,
                        ConnectivityManager.TYPE_WIFI, 0, true, true));
        Bundle args = new Bundle();
        ItemManager.WebItem item = new TestWebItem() {
            @Override
            public String getId() {
                return "1";
            }

            @Override
            public String getUrl() {
                return "http://example.com/article.html";
            }
        };
        args.putParcelable(ReadabilityFragment.EXTRA_ITEM, item);
        fragment = (ReadabilityFragment) Fragment.instantiate(activity, ReadabilityFragment.class.getName(), args);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment, "tag")
                .commit();
    }

    @Test
    public void testParseAndBind() {
        assertThat(activity.findViewById(R.id.progress)).isVisible();
        verify(readabilityClient).parse(eq("1"), eq("http://example.com/article.html"),
                callback.capture());
        callback.getValue().onResponse("<div>content</div>");
        assertThat(activity.findViewById(R.id.progress)).isNotVisible();
        assertThat(shadowOf((WebView) activity.findViewById(R.id.content))
                .getLastLoadDataWithBaseURL().data).contains("content");
        shadowOf(activity).recreate();
        assertThat(shadowOf((WebView) activity.findViewById(R.id.content))
                .getLastLoadDataWithBaseURL().data).contains("content");
        controller.pause().stop().destroy();
    }

    @Test
    public void testParseFailed() {
        assertThat(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_font_options)).isNotVisible();
        assertThat(activity.findViewById(R.id.progress)).isVisible();
        verify(readabilityClient).parse(eq("1"), eq("http://example.com/article.html"),
                callback.capture());
        callback.getValue().onResponse(null);
        reset(readabilityClient);
        assertThat(activity.findViewById(R.id.progress)).isNotVisible();
        assertThat(activity.findViewById(R.id.empty)).isVisible();
        assertThat(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_font_options)).isNotVisible();
        controller.pause().stop().destroy();
    }

    @Test
    public void testScrollToTop() {
        NestedScrollView scrollView = (NestedScrollView) activity.findViewById(R.id.nested_scroll_view);
        scrollView.smoothScrollTo(0, 1);
        assertEquals(1, ((ShadowNestedScrollView) ShadowExtractor.extract(scrollView))
                .getSmoothScrollY());
        fragment.scrollToTop();
        assertEquals(0, ((ShadowNestedScrollView) ShadowExtractor.extract(scrollView))
                .getSmoothScrollY());
        controller.pause().stop().destroy();
    }

    @Test
    public void testFontSizeMenu() {
        verify(readabilityClient).parse(eq("1"), eq("http://example.com/article.html"),
                callback.capture());
        callback.getValue().onResponse("<div>content</div>");
        assertThat(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_font_options)).isVisible();
        assertThat(shadowOf((WebView) activity.findViewById(R.id.content))
                .getLastLoadDataWithBaseURL().data).contains("14"); // small - default
        MenuItem menuItem = shadowOf(activity).getOptionsMenu().findItem(R.id.menu_font_size);
        assertThat(menuItem).hasSubMenu();
        assertThat(menuItem.getSubMenu()).hasSize(5);
        shadowOf(activity).clickMenuItem(R.id.menu_font_size);
        fragment.onOptionsItemSelected(menuItem.getSubMenu().getItem(4)); // extra large
        assertThat(shadowOf((WebView) activity.findViewById(R.id.content))
                .getLastLoadDataWithBaseURL().data).contains("20");
        assertEquals(R.style.AppTextSize_XLarge,
                Preferences.Theme.resolvePreferredReadabilityTextSize(activity));
        controller.pause().stop().destroy();
    }

    @Test
    public void testFontMenu() {
        verify(readabilityClient).parse(eq("1"), eq("http://example.com/article.html"),
                callback.capture());
        callback.getValue().onResponse("<div>content</div>");
        MenuItem menuItem = shadowOf(activity).getOptionsMenu().findItem(R.id.menu_font);
        assertThat(menuItem).hasSubMenu();
        assertThat(menuItem.getSubMenu()).hasSize(5);
        shadowOf(activity).clickMenuItem(R.id.menu_font);
        fragment.onOptionsItemSelected(menuItem.getSubMenu().getItem(1)); // non default
        assertThat(shadowOf((WebView) activity.findViewById(R.id.content))
                .getLastLoadDataWithBaseURL().data).contains("DroidSans.ttf");
        assertEquals("DroidSans.ttf", Preferences.Theme.getReadabilityTypeface(activity));
        controller.pause().stop().destroy();
    }

    @Test
    public void testBindAfterDetached() {
        assertThat(activity.findViewById(R.id.progress)).isVisible();
        controller.pause().stop().destroy();
        verify(readabilityClient).parse(eq("1"), eq("http://example.com/article.html"), callback.capture());
        callback.getValue().onResponse("<div>content</div>");
    }
}
