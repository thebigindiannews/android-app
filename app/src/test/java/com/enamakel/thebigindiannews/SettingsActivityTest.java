package com.enamakel.thebigindiannews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.util.ActivityController;

import com.enamakel.thebigindiannews.activities.SettingsActivity;
import com.enamakel.thebigindiannews.data.AlgoliaClient;
import com.enamakel.thebigindiannews.fragments.SettingsFragment;
import com.enamakel.thebigindiannews.test.ShadowSearchRecentSuggestions;
import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;
import com.enamakel.thebigindiannews.util.Preferences;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.android.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@Config(shadows = {ShadowSearchRecentSuggestions.class, ShadowSupportPreferenceManager.class})
@RunWith(RobolectricGradleTestRunner.class)
public class SettingsActivityTest {
    private SettingsActivity activity;
    private ActivityController<SettingsActivity> controller;
    private SettingsFragment fragment;

    @Before
    public void setUp() {
        TestApplication.applicationGraph.inject(this);
        controller = Robolectric.buildActivity(SettingsActivity.class);
        activity = controller.create().postCreate(null).start().resume().visible().get();
        fragment = (SettingsFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(SettingsFragment.class.getName());
    }

    @Test
    public void testClearRecentSearches() {
        ShadowSearchRecentSuggestions.historyClearCount = 0;
        assertNotNull(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_clear_recent));
        shadowOf(activity).clickMenuItem(R.id.menu_clear_recent);
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertEquals(1, ShadowSearchRecentSuggestions.historyClearCount);
    }

    @Test
    public void testReset() {
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(activity.getString(R.string.pref_color_code), false)
                .commit();
        assertFalse(Preferences.colorCodeEnabled(activity));
        assertNotNull(shadowOf(activity).getOptionsMenu().findItem(R.id.menu_reset));
        shadowOf(activity).clickMenuItem(R.id.menu_reset);
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alertDialog);
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertTrue(Preferences.colorCodeEnabled(activity));
    }

    @Test
    public void testPrefTheme() {
        String key = activity.getString(R.string.pref_theme);
        // trigger listener
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(key, "dark")
                .commit();
        fragment.mListener.onSharedPreferenceChanged(
                ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity), key);
        assertNotNull(shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void testPrefFont() {
        String key = activity.getString(R.string.pref_text_size);
        // trigger listener
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(key, "1")
                .commit();
        fragment.mListener.onSharedPreferenceChanged(
                ShadowSupportPreferenceManager.getDefaultSharedPreferences(activity), key);
        assertNotNull(shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void testHelp() {
        fragment.getPreferenceScreen()
                .findPreference(activity.getString(R.string.pref_highlight_updated_help))
                .performClick();
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        assertThat((TextView) dialog.findViewById(R.id.alertTitle))
                .hasText(R.string.pref_highlight_updated_title);
    }

    @Test
    public void testLazyLoadHelp() {
        fragment.getPreferenceScreen()
                .findPreference(activity.getString(R.string.pref_lazy_load_help))
                .performClick();
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        assertThat((TextView) dialog.findViewById(R.id.alertTitle))
                .hasText(R.string.pref_lazy_load_title);
    }

    @After
    public void tearDown() {
        AlgoliaClient.sSortByTime = true;
        controller.pause().stop().destroy();
    }
}
