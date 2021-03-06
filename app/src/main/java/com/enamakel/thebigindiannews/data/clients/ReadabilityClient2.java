package com.enamakel.thebigindiannews.data.clients;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.NewsApplication;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.data.providers.entries.ReadabilityEntry;
import com.google.gson.annotations.Expose;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class ReadabilityClient2 {
    static final ReadabilityService readabilityService;
    static final CharSequence EMPTY_CONTENT = "<div></div>";
    static final String READABILITY_API_URL = "https://readability.com/api/content/v1/";
    static final ContentResolver contentResolver;


    static {
        contentResolver = NewsApplication.getContext().getContentResolver();

        // Initialize retrofit
        retrofit2.Retrofit retrofit = RetrofitFactory.build(READABILITY_API_URL);

        // Create the service!
        readabilityService = retrofit.create(ReadabilityService.class);
    }


    public static void parse(final String itemId, final String url, final Callback callback) {
        new ReadabilityHandler(contentResolver, itemId)
                .setQueryCallback(new QueryCallback() {
                    @Override
                    public void onQueryComplete(String content) {
                        if (TextUtils.equals(EMPTY_CONTENT, content)) callback.onResponse(null);
                        else if (TextUtils.isEmpty(content))
                            readabilityParse(itemId, url, callback);
                        else callback.onResponse(content);
                    }
                })

                .startQuery(0, itemId, BigIndianProvider.URI_READABILITY,
                        new String[]{ReadabilityEntry.COLUMN_NAME_CONTENT},
                        ReadabilityEntry.COLUMN_NAME_ITEM_ID + " = ?",
                        new String[]{itemId}, null);
    }


    static void readabilityParse(final String itemId, String url, final Callback callback) {
        readabilityService.parse(url)
                .enqueue(new retrofit2.Callback<ReadableResponse>() {
                    @Override
                    public void onResponse(Response<ReadableResponse> response) {
                        ReadableResponse readable = response.body();

                        if (readable == null) {
                            callback.onResponse(null);
                            return;
                        }

                        readable.content = fixedContent(readable.content);

                        cache(itemId, readable.content);

                        if (TextUtils.equals(EMPTY_CONTENT, readable.content))
                            callback.onResponse(null);
                        else callback.onResponse(readable.content);

                    }


                    @Override
                    public void onFailure(Throwable t) {
                        callback.onResponse(null);
                    }
                });
    }


    static void cache(String itemId, String content) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(ReadabilityEntry.COLUMN_NAME_ITEM_ID, itemId);
        contentValues.put(ReadabilityEntry.COLUMN_NAME_CONTENT, content);
        new ReadabilityHandler(contentResolver, itemId).startInsert(0, itemId,
                BigIndianProvider.URI_READABILITY, contentValues);
    }


    interface ReadabilityService {
        @GET("parser?token=" + BuildConfig.READABILITY_TOKEN)
        Call<ReadableResponse> parse(@Query("url") String url);
    }


    static class ReadabilityHandler extends AsyncQueryHandler {
        final String itemId;
        QueryCallback callback;


        public ReadabilityHandler(ContentResolver contentResolver, String itemId) {
            super(contentResolver);
            this.itemId = itemId;
        }


        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            if (callback == null) return;

            if (cookie == null || !cookie.equals(itemId)) {
                callback = null;
                return;
            }

            if (!cursor.moveToFirst()) callback.onQueryComplete(null);
            else callback.onQueryComplete(cursor.getString(cursor.getColumnIndexOrThrow(
                    ReadabilityEntry.COLUMN_NAME_CONTENT)));
            callback = null;
        }


        ReadabilityHandler setQueryCallback(@NonNull QueryCallback callback) {
            this.callback = callback;
            return this;
        }
    }


    /**
     * Sometimes Readability has parsing issues and messes up the image 'src' attributes. This
     * function takes the content and gets rid of all the junk text..
     *
     * @param content The content from the Readability API
     * @return Fixed content.
     */
    static String fixedContent(String content) {
        content = content.replaceAll("[.]jpg[^\"']+", ".jpg");
        content = content.replaceAll("[.]jpeg[^\"']+", ".jpeg");
        content = content.replaceAll("[.]gif[^\"']+", ".gif");
        content = content.replaceAll("[.]png[^\"']+", ".png");
        return content;
    }


    static class ReadableResponse {
        @Expose String content;
    }


    interface QueryCallback {
        void onQueryComplete(String content);
    }


    public interface Callback {
        void onResponse(String content);
    }
}