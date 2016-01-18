package com.enamakel.thebigindiannews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.junit.After;
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
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import javax.inject.Inject;
import javax.inject.Named;

import com.enamakel.thebigindiannews.activities.ItemActivity;
import com.enamakel.thebigindiannews.activities.ThreadPreviewActivity;
import com.enamakel.thebigindiannews.activities.UserActivity;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.TestHnItem;
import com.enamakel.thebigindiannews.data.UserManager;
import com.enamakel.thebigindiannews.test.ShadowRecyclerView;
import com.enamakel.thebigindiannews.test.ShadowRecyclerViewAdapter;

import static junit.framework.Assert.assertEquals;
import static org.assertj.android.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowRecyclerView.class, ShadowRecyclerViewAdapter.class, ShadowRecyclerViewAdapter.ShadowViewHolder.class})
@RunWith(RobolectricGradleTestRunner.class)
public class UserActivityTest {
    private ActivityController<UserActivity> controller;
    private UserActivity activity;
    @Inject UserManager userManager;
    @Inject @Named(ActivityModule.HN) ItemManager itemManager;
    @Captor ArgumentCaptor<ResponseListener<UserManager.User>> userCaptor;
    @Captor ArgumentCaptor<ResponseListener<ItemManager.Item>> itemCaptor;
    private UserManager.User user;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestApplication.applicationGraph.inject(this);
        reset(userManager);
        reset(itemManager);
        controller = Robolectric.buildActivity(UserActivity.class);
        Intent intent = new Intent();
        intent.putExtra(UserActivity.EXTRA_USERNAME, "username");
        activity = controller.withIntent(intent).create().start().resume().visible().get();
        user = mock(UserManager.User.class);
        when(user.getId()).thenReturn("username");
        when(user.getCreated(any(Context.class))).thenReturn("May 01 2015");
        when(user.getKarma()).thenReturn(2016L);
        when(user.getAbout()).thenReturn("about");
        when(user.getItems()).thenReturn(new ItemManager.Item[]{
                new TestHnItem(1L){
                    @NonNull
                    @Override
                    public String getType() {
                        return COMMENT_TYPE;
                    }
                },
                new TestHnItem(2L) {
                    @NonNull
                    @Override
                    public String getType() {
                        return STORY_TYPE;
                    }
                }
        });
    }

    @Test
    public void testHomeClick() {
        shadowOf(activity).clickMenuItem(android.R.id.home);
        assertThat(activity).isFinishing();
    }

    @Test
    public void testBinding() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        assertThat((TextView) activity.findViewById(R.id.title)).hasTextString("username");
        assertThat((TextView) activity.findViewById(R.id.user_info)).containsText("karma: 2016");
        assertThat((TextView) activity.findViewById(R.id.about)).hasTextString("about");
        assertEquals(activity.getResources().getQuantityString(R.plurals.submissions_count, 2, 2),
                ((TabLayout) activity.findViewById(R.id.tab_layout)).getTabAt(0).getText());
        assertEquals(2, (((RecyclerView) activity.findViewById(R.id.recycler_view)).getAdapter())
                .getItemCount());
        shadowOf(activity).recreate();
        assertThat((TextView) activity.findViewById(R.id.title)).hasTextString("username");
    }

    @Test
    public void testBindingNoAbout() {
        when(user.getAbout()).thenReturn(null);
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        assertThat((TextView) activity.findViewById(R.id.about)).isNotVisible();
    }

    @Test
    public void testEmpty() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(null);
        assertThat(activity.findViewById(R.id.empty)).isVisible();
    }

    @Test
    public void testFailed() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onError(null);
        assertEquals(activity.getString(R.string.user_failed), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testScrollToTop() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        ShadowRecyclerView recyclerView = (ShadowRecyclerView) ShadowExtractor
                .extract(activity.findViewById(R.id.recycler_view));
        recyclerView.setSmoothScrollToPosition(1);
        TabLayout.Tab tab = ((TabLayout) activity.findViewById(R.id.tab_layout)).getTabAt(0);
        tab.select();
        tab.select();
        assertEquals(0, recyclerView.getSmoothScrollToPosition());
    }

    @Test
    public void testNoId() {
        controller = Robolectric.buildActivity(UserActivity.class);
        activity = controller.create().get();
        assertThat(activity).isFinishing();
    }

    @Test
    public void testNoDataId() {
        controller = Robolectric.buildActivity(UserActivity.class);
        Intent intent = new Intent();
        intent.setData(Uri.parse(BuildConfig.APPLICATION_ID + "://user/"));
        activity = controller.withIntent(intent).create().get();
        assertThat(activity).isFinishing();
    }

    @Test
    public void testWithDataId() {
        controller = Robolectric.buildActivity(UserActivity.class);
        Intent intent = new Intent();
        intent.setData(Uri.parse(BuildConfig.APPLICATION_ID + "://user/123"));
        activity = controller.withIntent(intent).create().get();
        assertThat(activity).isNotFinishing();
    }

    @Test
    public void testDeepLink() {
        controller = Robolectric.buildActivity(UserActivity.class);
        Intent intent = new Intent();
        intent.setData(Uri.parse("https://news.ycombinator.com/user?id=123"));
        activity = controller.withIntent(intent).create().get();
        assertThat(activity).isNotFinishing();
    }

    @Test
    public void testCommentBinding() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        ShadowRecyclerViewAdapter adapter = (ShadowRecyclerViewAdapter)
                ShadowExtractor.extract(recyclerView.getAdapter());
        adapter.makeItemVisible(0);
        verify(itemManager).getItem(eq("1"), itemCaptor.capture());
        itemCaptor.getValue().onResponse(new TestHnItem(1L) {
            @Override
            public String getText() {
                return "content";
            }

            @Override
            public String getParent() {
                return "2";
            }
        });
        adapter.makeItemVisible(0);
        RecyclerView.ViewHolder viewHolder = adapter.getViewHolder(0);
        assertThat(viewHolder.itemView.findViewById(R.id.title)).isNotVisible();
        assertThat((TextView) viewHolder.itemView.findViewById(R.id.text))
                .isVisible()
                .hasTextString("content");
        viewHolder.itemView.findViewById(R.id.comment).performClick();
        assertThat(shadowOf(activity).getNextStartedActivity())
                .hasComponent(activity, ThreadPreviewActivity.class)
                .hasExtra(ThreadPreviewActivity.EXTRA_ITEM);
    }

    @Test
    public void testStoryBinding() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        ShadowRecyclerViewAdapter adapter = (ShadowRecyclerViewAdapter)
                ShadowExtractor.extract(recyclerView.getAdapter());
        adapter.makeItemVisible(1);
        verify(itemManager).getItem(eq("2"), itemCaptor.capture());
        itemCaptor.getValue().onResponse(new TestHnItem(2L) {
            @Override
            public String getTitle() {
                return "title";
            }

            @Override
            public String getText() {
                return "content";
            }

            @Override
            public int getScore() {
                return 46;
            }
        });
        adapter.makeItemVisible(1);
        RecyclerView.ViewHolder viewHolder = adapter.getViewHolder(1);
        assertThat((TextView) viewHolder.itemView.findViewById(R.id.posted))
                .containsText(activity.getResources().getQuantityString(R.plurals.score, 46, 46));
        assertThat((TextView) viewHolder.itemView.findViewById(R.id.title))
                .isVisible()
                .hasTextString("title");
        assertThat((TextView) viewHolder.itemView.findViewById(R.id.text))
                .isVisible()
                .hasTextString("content");
        viewHolder.itemView.findViewById(R.id.comment).performClick();
        assertThat(shadowOf(activity).getNextStartedActivity())
                .hasComponent(activity, ItemActivity.class)
                .hasExtra(ItemActivity.EXTRA_ITEM);
    }

    @Test
    public void testDeletedItemBinding() {
        verify(userManager).getUser(eq("username"), userCaptor.capture());
        userCaptor.getValue().onResponse(user);
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        ShadowRecyclerViewAdapter adapter = (ShadowRecyclerViewAdapter)
                ShadowExtractor.extract(recyclerView.getAdapter());
        adapter.makeItemVisible(0);
        verify(itemManager).getItem(eq("1"), itemCaptor.capture());
        itemCaptor.getValue().onResponse(new TestHnItem(1L) {
            @Override
            public boolean isDeleted() {
                return true;
            }
        });
        adapter.makeItemVisible(0);
        RecyclerView.ViewHolder viewHolder = adapter.getViewHolder(0);
        assertThat(viewHolder.itemView.findViewById(R.id.comment)).isNotVisible();
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }
}
