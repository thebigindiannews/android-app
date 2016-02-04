package com.enamakel.thebigindiannews.data;


import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.clients.HackerNewsClient;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.google.gson.annotations.Expose;

import lombok.Data;


/**
 * Represents a favorite item
 */

public class FavoriteModel {





//    //    @Override
//    public boolean isStoryType() {
//        return true;
//    }



//    //    @Override
//    public Spannable getDisplayedTime(Context context, boolean abbreviate, boolean authorLink) {
//        return new SpannableString(context.getString(R.string.saved,
//                DateUtils.getRelativeDateTimeString(context, time,
//                        DateUtils.MINUTE_IN_MILLIS,
//                        DateUtils.YEAR_IN_MILLIS,
//                        DateUtils.FORMAT_ABBREV_MONTH)));
//    }


//    //    @Override
//    public String getSource() {
//        return TextUtils.isEmpty(url) ? null : Uri.parse(url).getHost();
//    }


//    @NonNull
////    @Override
//    public String getType() {
//        // TODO treating all saved items as stories for now
////        return STORY_TYPE;
//        return null;
//    }


//    @Override
//    public String toString() {
//        return String.format("%s (%s) - %s", title, url, String.format(HackerNewsClient.WEB_ITEM_PATH, id));
//    }


    public int describeContents() {
        return 0;
    }
}
