package com.enamakel.thebigindiannews.test;

import android.support.v7.view.ActionMode;

import com.enamakel.thebigindiannews.activities.FavoriteActivity;

public class TestFavoriteActivity extends FavoriteActivity {
    public ActionMode.Callback actionModeCallback;

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        actionModeCallback = callback;
        return super.startSupportActionMode(callback);
    }
}
