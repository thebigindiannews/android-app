package com.enamakel.thebigindiannews;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import com.enamakel.thebigindiannews.test.ParameterizedRobolectricGradleTestRunner;
import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowSupportPreferenceManager.class})
@RunWith(ParameterizedRobolectricGradleTestRunner.class)
public class PreferencesTextSizeTest {
    private final String choice;
    private final int resId;
    private Activity activity;

    public PreferencesTextSizeTest(String choice, int resId) {
        this.choice = choice;
        this.resId = resId;
    }

    @ParameterizedRobolectricGradleTestRunner.Parameters
    public static List<Object[]> provideParameters() {
        return Arrays.asList(
                new Object[]{"-1", R.style.AppTextSize_XSmall},
                new Object[]{"1", R.style.AppTextSize_Medium},
                new Object[]{"2", R.style.AppTextSize_Large},
                new Object[]{"3", R.style.AppTextSize_XLarge}
        );
    }

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(Activity.class);
        shadowOf(activity.getTheme()).setTo(activity.getResources().newTheme());
    }

    @Test
    public void testResolveTextSizeResId() {
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(activity.getString(R.string.pref_text_size), choice)
                .commit();
        Preferences.Theme.apply(activity, false);
        assertEquals(resId, shadowOf(activity.getTheme()).getStyleResourceId());
    }
}
