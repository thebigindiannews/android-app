package com.enamakel.thebigindiannews;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.util.ActivityController;

import com.enamakel.thebigindiannews.data.ItemManager;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class StoriesActivityTest {
    @Test
    public void testShowActivity() {
        ActivityController<ShowActivity> controller = Robolectric.buildActivity(ShowActivity.class);
        ShowActivity activity = controller.create().start().resume().get();
        assertEquals(activity.getString(R.string.title_activity_show), activity.getDefaultTitle());
        assertEquals(ItemManager.SHOW_FETCH_MODE, activity.getFetchMode());
        controller.pause().stop().destroy();
    }

    @Test
    public void testNewActivity() {
        ActivityController<NewActivity> controller = Robolectric.buildActivity(NewActivity.class);
        NewActivity activity = controller.create().start().resume().get();
        assertEquals(activity.getString(R.string.title_activity_new), activity.getDefaultTitle());
        assertEquals(ItemManager.NEW_FETCH_MODE, activity.getFetchMode());
        controller.pause().stop().destroy();
    }

    @Test
    public void testAskActivity() {
        ActivityController<AskActivity> controller = Robolectric.buildActivity(AskActivity.class);
        AskActivity activity = controller.create().start().resume().get();
        assertEquals(activity.getString(R.string.title_activity_ask), activity.getDefaultTitle());
        assertEquals(ItemManager.ASK_FETCH_MODE, activity.getFetchMode());
        controller.pause().stop().destroy();
    }

    @Test
    public void testJobsActivity() {
        ActivityController<JobsActivity> controller = Robolectric.buildActivity(JobsActivity.class);
        JobsActivity activity = controller.create().start().resume().get();
        assertEquals(activity.getString(R.string.title_activity_jobs), activity.getDefaultTitle());
        assertEquals(ItemManager.JOBS_FETCH_MODE, activity.getFetchMode());
        controller.pause().stop().destroy();
    }
}
