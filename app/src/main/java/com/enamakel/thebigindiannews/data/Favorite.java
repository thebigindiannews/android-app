package com.enamakel.thebigindiannews.data;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.clients.HackerNewsClient;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;

/**
 * Represents a favorite item
 */
public class Favorite extends BaseCardModel {
    private String id;
    private String url;
    private String title;
    private long time;

    public static final Creator<Favorite> CREATOR = new Creator<Favorite>() {
        @Override
        public Favorite createFromParcel(Parcel source) {
            return new Favorite(source);
        }


        @Override
        public Favorite[] newArray(int size) {
            return new Favorite[size];
        }
    };


    Favorite(String itemId, String url, String title, long time) {
        this.id = itemId;
        this.url = url;
        this.title = title;
        this.time = time;
    }


    private Favorite(Parcel source) {
        id = source.readString();
        url = source.readString();
        title = source.readString();
    }


//    @Override
    public String getUrl() {
        return url;
    }


//    @Override
    public boolean isStoryType() {
        return true;
    }


    @Override
    public String getId() {
        return id;
    }


//    @Override
    public long getLongId() {
        return Long.valueOf(id);
    }


//    @Override
    public String getDisplayedTitle() {
        return title;
    }


//    @Override
    public Spannable getDisplayedTime(Context context, boolean abbreviate, boolean authorLink) {
        return new SpannableString(context.getString(R.string.saved,
                DateUtils.getRelativeDateTimeString(context, time,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.YEAR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_MONTH)));
    }


//    @Override
    public String getSource() {
        return TextUtils.isEmpty(url) ? null : Uri.parse(url).getHost();
    }


    @NonNull
//    @Override
    public String getType() {
        // TODO treating all saved items as stories for now
//        return STORY_TYPE;
        return null;
    }


    @Override
    public String toString() {
        return String.format("%s (%s) - %s", title, url, String.format(HackerNewsClient.WEB_ITEM_PATH, id));
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(title);
    }
}
