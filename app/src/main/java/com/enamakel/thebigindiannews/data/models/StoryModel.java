package com.enamakel.thebigindiannews.data.models;


import android.content.Context;
import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.google.gson.annotations.Expose;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import lombok.Data;
import lombok.ToString;


@Data
@ToString(includeFieldNames = true)
public class StoryModel extends BaseCardModel<StoryModel> {
    private static final String FORMAT_LINK_USER = "<a href=\"%1$s://user/%2$s\">%2$s</a>";


    @Expose String excerpt;
    @Expose String image_url;
    @Expose String kind;
    @Expose String merged_story;
    @Expose String slug;
    @Expose String story_cache;
    @Expose String title;
    @Expose String url;
    @Expose Thumbnail thumbnail;
    @Expose boolean expired;
    @Expose boolean moderated;
    @Expose int clicks_count;
    @Expose int comments_count;
    @Expose int words_count;
    int local_revision;

    boolean favorite;
    boolean deleted;
    boolean dead;
    boolean viewed;

    @Data public class Thumbnail {
        @Expose String color;
        @Expose String filename;
        @Expose String image_url;
        @Expose double height;
        @Expose double width;


        public String getUrl() {
            if (filename != null) return "https://thebigindian.news/uploads/" + filename;
            return null;
        }
    }


    public static StoryModel readFromParcel(Parcel source) {
        String json = source.readString();
        return gson.fromJson(json, StoryModel.class);
    }


    public void populate(StoryModel source) {
        _id = source._id;
        excerpt = source.excerpt;
    }


    @Override
    protected String getDisplayedTitle() {
        return title;
    }


    public String getSource() {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return "";
        }
    }


    public Spannable getDisplayedTime(Context context, boolean abbreviate, boolean authorLink) {
        CharSequence relativeTime = "";
        long time = (new Date()).getTime();

        if (abbreviate) relativeTime = AppUtils.getAbbreviatedTimeSpan(time * 1000);
        else {
            try {
                relativeTime = DateUtils.getRelativeTimeSpanString(time * 1000,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL);
            } catch (NullPointerException e) {
                // TODO should properly prevent this
            }
        }

//        if (deleted) {
//            Spannable spannable = new SpannableString(relativeTime);
//            spannable.setSpan(new StrikethroughSpan(), 0, relativeTime.length(),
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//            return spannable;
//        }

        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

        if (isExpired()) {
            spannableBuilder.append(context.getString(R.string.dead_prefix)).append(" ");
        }

        spannableBuilder.append(relativeTime);

//        if (!TextUtils.isEmpty(created_by)) {
//            spannableBuilder.append(" - ")
//                    .append(authorLink ? Html.fromHtml(String.format(FORMAT_LINK_USER,
//                            BuildConfig.APPLICATION_ID, created_by)) : created_by);
//        }
        return spannableBuilder;
    }


    public String getReadtime() {
        String lessThanAMinute = "less than a minute";
        String minShortForm = "min";
        double readingTimeMinutes, readingTimeSeconds;

        // The average human reading speed (WPM)
        float wordsPerMinute = 250;

        // define words per second based on words per minute
        float wordsPerSecond = wordsPerMinute / 60;

        //define total reading time in seconds
        float totalReadingTimeSeconds = words_count / wordsPerSecond;

        // define reading time in minutes
        readingTimeMinutes = Math.round(totalReadingTimeSeconds / 60);

        // if minutes are greater than 0 then set reading time by the minute
        if (readingTimeMinutes > 0) return readingTimeMinutes + " " + minShortForm;

            // set reading time as less than a minute
        else return lessThanAMinute;
    }
}