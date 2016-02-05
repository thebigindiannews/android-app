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

package com.enamakel.thebigindiannews.data.managers;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.data.providers.entries.ViewedEntry;


/**
 * Data repository for session state
 */
public class SessionManager {

    /**
     * Checks if an item has been viewed previously
     *
     * @param contentResolver an instance of {@link ContentResolver}
     * @param itemId          item ID to check
     * @param callbacks       listener to be informed upon checking completed
     */
    public void isViewed(ContentResolver contentResolver, final String itemId,
                         final OperationCallbacks callbacks) {
        if (TextUtils.isEmpty(itemId) || callbacks == null) return;

        new SessionHandler(contentResolver, itemId, callbacks).startQuery(0, itemId,
                BigIndianProvider.URI_VIEWED, null,
                ViewedEntry.COLUMN_NAME_ITEM_ID + " = ?",
                new String[]{itemId}, null);
    }


    /**
     * Marks an item as already being viewed
     *
     * @param context an instance of {@link Context}
     * @param itemId  item ID that has been viewed
     */
    public void view(Context context, final String itemId) {
        if (TextUtils.isEmpty(itemId)) return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ViewedEntry.COLUMN_NAME_ITEM_ID, itemId);
        ContentResolver cr = context.getContentResolver();

        new SessionHandler(cr, itemId).startInsert(0, itemId,
                BigIndianProvider.URI_VIEWED, contentValues);

        // optimistically assume insert ok
        cr.notifyChange(BigIndianProvider.URI_VIEWED
                        .buildUpon()
                        .appendPath(itemId)
                        .build(),
                null);
    }


    /**
     * Callback interface for asynchronous session operations
     */
    public interface OperationCallbacks {
        /**
         * Fired when checking of view status is completed
         *
         * @param isViewed true if is viewed, false otherwise
         */
        void onCheckViewedComplete(boolean isViewed);
    }


    static class SessionHandler extends AsyncQueryHandler {
        final String mItemId;
        OperationCallbacks mCallback;


        public SessionHandler(ContentResolver cr, @NonNull String itemId) {
            super(cr);
            mItemId = itemId;
        }


        public SessionHandler(ContentResolver cr, @NonNull String itemId,
                              @NonNull OperationCallbacks callback) {
            this(cr, itemId);
            mCallback = callback;
        }


        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);

            if (cookie == null) {
                mCallback = null;
                return;
            }

            if (cookie.equals(mItemId)) {
                mCallback.onCheckViewedComplete(cursor != null && cursor.getCount() > 0);
                mCallback = null;
            }
        }
    }
}