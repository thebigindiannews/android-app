package com.enamakel.thebigindiannews.test;

import android.view.Menu;

import static org.robolectric.Shadows.shadowOf;

public class TestListActivity extends com.enamakel.thebigindiannews.ListActivity {
    @Override
    public void supportInvalidateOptionsMenu() {
        Menu optionsMenu = shadowOf(this).getOptionsMenu();
        if (optionsMenu != null) {
            onCreateOptionsMenu(optionsMenu);
            onPrepareOptionsMenu(optionsMenu);
        }
    }
}
