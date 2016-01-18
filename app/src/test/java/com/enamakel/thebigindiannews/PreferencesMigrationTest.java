package com.enamakel.thebigindiannews;

import android.support.annotation.StringRes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import com.enamakel.thebigindiannews.test.ParameterizedRobolectricGradleTestRunner;
import com.enamakel.thebigindiannews.test.ShadowSupportPreferenceManager;
import com.enamakel.thebigindiannews.util.Preferences;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@Config(shadows = {ShadowSupportPreferenceManager.class})
@RunWith(ParameterizedRobolectricGradleTestRunner.class)
public class PreferencesMigrationTest {
    private final int oldKey;
    private final boolean oldValue;
    private final int newKey;
    private final int newValue;

    public PreferencesMigrationTest(@StringRes int oldKey, boolean oldValue,
                                    @StringRes int newKey, @StringRes int newValue) {
        this.oldKey = oldKey;
        this.oldValue = oldValue;
        this.newKey = newKey;
        this.newValue = newValue;
    }

    @ParameterizedRobolectricGradleTestRunner.Parameters
    public static List<Object[]> provideParameters() {
        return Arrays.asList(
                new Object[]{R.string.pref_item_click, true,
                        R.string.pref_story_display, R.string.pref_story_display_value_comments},
                new Object[]{R.string.pref_item_search_recent, false,
                        R.string.pref_search_sort, R.string.pref_search_sort_value_default}
        );
    }

    @Test
    public void testMigrate() {
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                .edit()
                .putBoolean(RuntimeEnvironment.application.getString(oldKey), oldValue)
                .commit();
        assertTrue(ShadowSupportPreferenceManager
                .getDefaultSharedPreferences(RuntimeEnvironment.application)
                .contains(RuntimeEnvironment.application.getString(oldKey)));
        Preferences.migrate(RuntimeEnvironment.application);
        assertFalse(ShadowSupportPreferenceManager
                .getDefaultSharedPreferences(RuntimeEnvironment.application)
                .contains(RuntimeEnvironment.application.getString(oldKey)));
        assertEquals(RuntimeEnvironment.application.getString(newValue),
                ShadowSupportPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
        .getString(RuntimeEnvironment.application.getString(newKey), null));
    }

    @Test
    public void testNoMigrate() {
        assertFalse(ShadowSupportPreferenceManager
                .getDefaultSharedPreferences(RuntimeEnvironment.application)
                .contains(RuntimeEnvironment.application.getString(oldKey)));
        Preferences.migrate(RuntimeEnvironment.application);
        assertNull(RuntimeEnvironment.application.getString(newValue),
                ShadowSupportPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                        .getString(RuntimeEnvironment.application.getString(newKey), null));
    }

    @Test
    public void testNoMigrateDefault() {
        ShadowSupportPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                .edit()
                .putBoolean(RuntimeEnvironment.application.getString(oldKey), !oldValue)
                .commit();
        assertTrue(ShadowSupportPreferenceManager
                .getDefaultSharedPreferences(RuntimeEnvironment.application)
                .contains(RuntimeEnvironment.application.getString(oldKey)));
        Preferences.migrate(RuntimeEnvironment.application);
        assertFalse(ShadowSupportPreferenceManager
                .getDefaultSharedPreferences(RuntimeEnvironment.application)
                .contains(RuntimeEnvironment.application.getString(oldKey)));
        assertNull(RuntimeEnvironment.application.getString(newValue),
                ShadowSupportPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
                        .getString(RuntimeEnvironment.application.getString(newKey), null));
    }
}
