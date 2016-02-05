package com.enamakel.thebigindiannews.test;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.Spannable;

import com.enamakel.thebigindiannews.data.managers.ItemManager;

public abstract class TestWebItem implements ItemManager.WebItem {
    @Override
    public String getDisplayedTitle() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public boolean isStoryType() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public long getLongId() {
        return 0;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public Spannable getDisplayedTime(Context context, boolean abbreviate, boolean authorLink) {
        return null;
    }

    @NonNull
    @Override
    public String getType() {
        return STORY_TYPE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
