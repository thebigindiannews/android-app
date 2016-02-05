package com.enamakel.thebigindiannews;

import android.app.SearchManager;
import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;
import javax.inject.Named;

import com.enamakel.thebigindiannews.activities.SearchActivity;
import com.enamakel.thebigindiannews.data.clients.AlgoliaClient;
import com.enamakel.thebigindiannews.data.managers.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.test.ShadowSearchRecentSuggestions;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = ShadowSearchRecentSuggestions.class)
@RunWith(RobolectricGradleTestRunner.class)
public class SearchActivityTest {
    private ActivityController<SearchActivity> controller;
    private SearchActivity activity;
    @Inject @Named(ActivityModule.ALGOLIA) ItemManager itemManager;

    @Before
    public void setUp() {
        TestApplication.applicationGraph.inject(this);
        reset(itemManager);
        ShadowSearchRecentSuggestions.recentQueries.clear();
        controller = Robolectric.buildActivity(SearchActivity.class);
        activity = controller.get();
    }

    @Test
    public void testCreateWithQuery() {
        Intent intent = new Intent();
        intent.putExtra(SearchManager.QUERY, "filter");
        controller.withIntent(intent).create().start().resume(); // skip menu inflation
        assertThat(ShadowSearchRecentSuggestions.recentQueries).contains("filter");
        assertEquals(activity.getString(R.string.title_activity_search),
                activity.getDefaultTitle());
        assertEquals("filter", activity.getSupportActionBar().getSubtitle());
    }


    @Test
    public void testCreateWithoutQuery() {
        controller.create().start().resume(); // skip menu inflation
        assertThat(ShadowSearchRecentSuggestions.recentQueries).isEmpty();
        assertEquals(activity.getString(R.string.title_activity_search),
                activity.getDefaultTitle());
        assertNull(activity.getSupportActionBar().getSubtitle());
    }

    @Test
    public void testSort() {
        Intent intent = new Intent();
        intent.putExtra(SearchManager.QUERY, "filter");
        controller.withIntent(intent).create().postCreate(null).start().resume().visible();
        assertTrue(AlgoliaClient.sSortByTime);
        activity.onOptionsItemSelected(shadowOf(activity).getOptionsMenu()
                .findItem(R.id.menu_sort_recent)); // should not trigger search
        activity.onOptionsItemSelected(shadowOf(activity).getOptionsMenu()
                .findItem(R.id.menu_sort_popular)); // should trigger search
        assertFalse(AlgoliaClient.sSortByTime);
        verify(itemManager, times(2)).getStories(anyString(), any(ResponseListener.class));
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }
}
