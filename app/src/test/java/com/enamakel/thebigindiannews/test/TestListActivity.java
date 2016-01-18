package com.enamakel.thebigindiannews.test;

import android.view.Menu;

import com.enamakel.thebigindiannews.activities.ListActivity;

import static org.robolectric.Shadows.shadowOf;

public class TestListActivity extends ListActivity {
    @Override
    public void supportInvalidateOptionsMenu() {
        Menu optionsMenu = shadowOf(this).getOptionsMenu();
        if (optionsMenu != null) {
            onCreateOptionsMenu(optionsMenu);
            onPrepareOptionsMenu(optionsMenu);
        }
    }
}
