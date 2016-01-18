package com.enamakel.thebigindiannews.preference;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.List;

import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.activities.SettingsActivity;
import com.enamakel.thebigindiannews.test.ParameterizedRobolectricGradleTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(ParameterizedRobolectricGradleTestRunner.class)
public class ThemePreferenceTest {
    private final int preferenceId;
    private final int styleResId;
    private SettingsActivity activity;
    private View preferenceView;

    @ParameterizedRobolectricGradleTestRunner.Parameters
    public static List<Object[]> provideParameters() {
        return Arrays.asList(
                new Object[]{R.id.theme_dark, R.style.AppTheme_Dark},
                new Object[]{R.id.theme_sepia, R.style.AppTheme_Sepia},
                new Object[]{R.id.theme_green, R.style.AppTheme_Green},
                new Object[]{R.id.theme_light, R.style.AppTheme}
        );
    }

    public ThemePreferenceTest(int preferenceId, int styleResId) {
        this.preferenceId = preferenceId;
        this.styleResId = styleResId;
    }

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(SettingsActivity.class)
                .create().postCreate(null).start().resume().visible().get();
        RecyclerView list = (RecyclerView) activity.findViewById(R.id.list);
        RecyclerView.Adapter adapter = list.getAdapter();
        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(list, adapter.getItemViewType(0));
        adapter.onBindViewHolder(holder, 0);
        preferenceView = holder.itemView;
    }

    @Test
    public void test() {
        preferenceView.findViewById(preferenceId).performClick();
        Preferences.Theme.apply(activity, false);
        shadowOf(activity).recreate();
        assertThat(shadowOf(activity).callGetThemeResId()).isEqualTo(styleResId);
    }
}
