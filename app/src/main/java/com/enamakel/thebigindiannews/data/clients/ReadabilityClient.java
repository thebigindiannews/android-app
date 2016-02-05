/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enamakel.thebigindiannews.data.clients;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;

import javax.inject.Inject;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;


public interface ReadabilityClient {
    interface Callback {
        void onResponse(String content);
    }


    void parse(String itemId, String url, Callback callback);


    class Impl implements ReadabilityClient {
        static final CharSequence EMPTY_CONTENT = "<div></div>";
        final ReadabilityService readabilityService;
        final ContentResolver mContentResolver;


        @Inject
        public Impl(Context context, RestServiceFactory factory) {
            readabilityService = factory.create(ReadabilityService.READABILITY_API_URL,
                    ReadabilityService.class);
            mContentResolver = context.getContentResolver();
        }


        @Override
        public void parse(final String itemId, final String url, final Callback callback) {
            new ReadabilityHandler(mContentResolver, itemId)
                    .setQueryCallback(new QueryCallback() {
                        @Override
                        public void onQueryComplete(String content) {
                            if (TextUtils.equals(EMPTY_CONTENT, content)) {
                                callback.onResponse(null);
                            } else if (TextUtils.isEmpty(content)) {
                                readabilityParse(itemId, url, callback);
                            } else {
                                callback.onResponse(content);
                            }
                        }
                    })
                    .startQuery(0, itemId, BigIndianProvider.URI_READABILITY,
                            new String[]{BigIndianProvider.ReadabilityEntry.COLUMN_NAME_CONTENT},
                            BigIndianProvider.ReadabilityEntry.COLUMN_NAME_ITEM_ID + " = ?",
                            new String[]{itemId}, null);
        }


        void readabilityParse(final String itemId, String url, final Callback callback) {
            readabilityService.parse(url)
                    .enqueue(new retrofit.Callback<Readable>() {
                        @Override
                        public void onResponse(Response<Readable> response, Retrofit retrofit) {
                            Readable readable = response.body();

                            if (readable == null) {
                                callback.onResponse(null);
                                return;
                            }

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


        void cache(String itemId, String content) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(BigIndianProvider.ReadabilityEntry.COLUMN_NAME_ITEM_ID, itemId);
            contentValues.put(BigIndianProvider.ReadabilityEntry.COLUMN_NAME_CONTENT, content);
            new ReadabilityHandler(mContentResolver, itemId).startInsert(0, itemId,
                    BigIndianProvider.URI_READABILITY, contentValues);
        }


        interface ReadabilityService {
            String READABILITY_API_URL = "https://readability.com/api/content/v1/";


            @GET("parser?token=" + BuildConfig.READABILITY_TOKEN)
            Call<Readable> parse(@Query("url") String url);
        }


        static class Readable {
            String content;
        }


        static class ReadabilityHandler extends AsyncQueryHandler {
            final String mItemId;
            QueryCallback callback;


            public ReadabilityHandler(ContentResolver cr, String itemId) {
                super(cr);
                mItemId = itemId;
            }


            ReadabilityHandler setQueryCallback(@NonNull QueryCallback callback) {
                this.callback = callback;
                return this;
            }


            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                if (callback == null) {
                    return;
                }
                if (cookie == null || !cookie.equals(mItemId)) {
                    callback = null;
                    return;
                }
                if (!cursor.moveToFirst()) {
                    callback.onQueryComplete(null);
                } else {
                    callback.onQueryComplete(cursor.getString(cursor.getColumnIndexOrThrow(
                            BigIndianProvider.ReadabilityEntry.COLUMN_NAME_CONTENT)));
                }
                callback = null;
            }
        }


        interface QueryCallback {
            void onQueryComplete(String content);
        }
    }
}
