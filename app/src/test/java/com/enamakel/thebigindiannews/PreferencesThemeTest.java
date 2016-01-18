package com.enamakel.thebigindiannews;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowSupportPreferenceManager.class})
@RunWith(RobolectricGradleTestRunner.class)
public class PreferencesThemeTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(Activity.class);
        shadowOf(activity.getTheme()).setTo(activity.getResources().newTheme());
    }

    @Test
    public void testDefaultTheme() {
        Integer originalTheme = shadowOf(activity).callGetThemeResId();
        Preferences.Theme.apply(activity, false);
        assertThat(shadowOf(activity).callGetThemeResId()).isEqualTo(originalTheme);
    }

    @Test
    public void testDarkTheme() {
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(activity.getString(R.string.pref_theme), "dark")
                .commit();
        Preferences.Theme.apply(activity, false);
        assertThat(shadowOf(activity).callGetThemeResId()).isEqualTo(R.style.AppTheme_Dark);
    }
}
