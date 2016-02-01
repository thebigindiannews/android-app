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

package com.enamakel.thebigindiannews.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.MaterialisticProvider;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Data repository for {@link Favorite}
 */
public class FavoriteManager {

    public static final int LOADER = 0;

    /**
     * {@link android.content.Intent#getAction()} for broadcasting getting favorites matching query
     */
    public static final String ACTION_GET = FavoriteManager.class.getName() + ".ACTION_GET";

    /**
     * {@link android.os.Bundle} key for {@link #ACTION_GET} that contains {@link ArrayList} of
     * {@link Favorite}
     */
    public static final String ACTION_GET_EXTRA_DATA = ACTION_GET + ".EXTRA_DATA";
    private static final String URI_PATH_ADD = "add";
    private static final String URI_PATH_REMOVE = "remove";
    private static final String URI_PATH_CLEAR = "clear";


    /**
     * Gets all favorites matched given query, a {@link #ACTION_GET} broadcast will be sent upon
     * completion
     *
     * @param context an instance of {@link android.content.Context}
     * @param query   query to filter stories to be retrieved
     * @see #makeGetIntentFilter()
     */
    public void get(Context context, String query) {
        final String selection;
        final String[] selectionArgs;
        if (TextUtils.isEmpty(query)) {
            selection = null;
            selectionArgs = null;

        } else {
            selection = MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%"};
        }

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        new FavoriteHandler(context.getContentResolver(), new FavoriteCallback() {
            @Override
            void onQueryComplete(ArrayList<Favorite> favorites) {
                broadcastManager.sendBroadcast(makeGetBroadcastIntent(favorites));
            }
        }).startQuery(0, null, MaterialisticProvider.URI_FAVORITE,
                null, selection, selectionArgs, null);
    }


    /**
     * Adds given story as favorite
     *
     * @param context an instance of {@link android.content.Context}
     * @param story   story to be added as favorite
     */
    public void add(Context context, StoryModel story) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_ITEM_ID, story.get_id());
        contentValues.put(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_URL, story.getUrl());
        contentValues.put(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TITLE, story.getTitle());
//        contentValues.put(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TIME,
//                story instanceof Favorite ?
//                        String.valueOf(((Favorite) story).time) :
//                        String.valueOf(System.currentTimeMillis()));
        ContentResolver cr = context.getContentResolver();
        new FavoriteHandler(cr).startInsert(0, story.get_id(),
                MaterialisticProvider.URI_FAVORITE, contentValues);
        cr.notifyChange(buildAdded().appendPath(story.get_id()).build(), null);
    }


    /**
     * Clears all stories matched given query from favorites
     * will be sent upon completion
     *
     * @param context an instance of {@link android.content.Context}
     * @param query   query to filter stories to be cleared
     */
    public void clear(Context context, String query) {
        final String selection;
        final String[] selectionArgs;

        if (TextUtils.isEmpty(query)) {
            selection = null;
            selectionArgs = null;
        } else {
            selection = MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%"};
        }

        ContentResolver cr = context.getContentResolver();
        new FavoriteHandler(cr).startDelete(0, null, MaterialisticProvider.URI_FAVORITE,
                selection, selectionArgs);
        cr.notifyChange(buildCleared().build(), null);
    }


    /**
     * Checks if a story with given ID is a favorite
     *
     * @param contentResolver an instance of {@link ContentResolver}
     * @param itemId          story ID to check
     * @param callbacks       listener to be informed upon checking completed
     */
    public void check(ContentResolver contentResolver, final String itemId,
                      final OperationCallbacks callbacks) {
        if (itemId == null) return;
        if (callbacks == null) return;

        new FavoriteHandler(contentResolver, new FavoriteCallback() {
            @Override
            void onCheckComplete(boolean isFavorite) {
                callbacks.onCheckComplete(isFavorite);
            }
        }).startQuery(0, itemId, MaterialisticProvider.URI_FAVORITE, null,
                MaterialisticProvider.FavoriteEntry.COLUMN_NAME_ITEM_ID + " = ?",
                new String[]{itemId}, null);
    }


    /**
     * Removes a story from favorites
     * upon completion
     *
     * @param context an instance of {@link android.content.Context}
     * @param story   The story to be removed from favorites
     */
    public void remove(Context context, StoryModel story) {
        if (story == null) return;

        ContentResolver cr = context.getContentResolver();
        new FavoriteHandler(cr).startDelete(0, story.get_id(),
                MaterialisticProvider.URI_FAVORITE,
                MaterialisticProvider.FavoriteEntry.COLUMN_NAME_ITEM_ID + " = ?",
                new String[]{story.get_id()});
        cr.notifyChange(buildRemoved().appendPath(story.get_id()).build(), null);
    }


    /**
     * Removes multiple stories with given IDs from favorites
     * be sent upon completion
     *
     * @param context an instance of {@link android.content.Context}
     * @param itemIds array of story IDs to be removed from favorites
     */
    public void remove(Context context, Collection<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return;

        final ContentResolver contentResolver = context.getContentResolver();
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                for (String param : params) {
                    contentResolver.delete(MaterialisticProvider.URI_FAVORITE,
                            MaterialisticProvider.FavoriteEntry.COLUMN_NAME_ITEM_ID + " = ?",
                            new String[]{param});
                }

                return null;
            }
        }.execute(itemIds.toArray(new String[itemIds.size()]));

        for (String itemId : itemIds) {
            contentResolver.notifyChange(buildRemoved().appendPath(itemId).build(), null);
        }
    }


    /**
     * Creates an intent filter for get action broadcast
     *
     * @return get intent filter
     * @see #get(android.content.Context, String)
     */
    public static IntentFilter makeGetIntentFilter() {
        return new IntentFilter(ACTION_GET);
    }


    public static boolean isAdded(Uri uri) {
        return uri.toString().startsWith(buildAdded().toString());
    }


    public static boolean isRemoved(Uri uri) {
        return uri.toString().startsWith(buildRemoved().toString());
    }


    public static boolean isCleared(Uri uri) {
        return uri.toString().startsWith(buildCleared().toString());
    }


    private static Uri.Builder buildAdded() {
        return MaterialisticProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_ADD);
    }


    private static Uri.Builder buildRemoved() {
        return MaterialisticProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_REMOVE);
    }


    private static Uri.Builder buildCleared() {
        return MaterialisticProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_CLEAR);
    }


    private static Intent makeGetBroadcastIntent(ArrayList<Favorite> favorites) {
        final Intent intent = new Intent(ACTION_GET);
        intent.putExtra(ACTION_GET_EXTRA_DATA, favorites);
        return intent;
    }


    /**
     * A cursor wrapper to retrieve associated {@link Favorite}
     */
    public static class Cursor extends CursorWrapper {
        public Cursor(android.database.Cursor cursor) {
            super(cursor);
        }


        public Favorite getFavorite() {
            final String itemId = getString(getColumnIndexOrThrow(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_ITEM_ID));
            final String url = getString(getColumnIndexOrThrow(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_URL));
            final String title = getString(getColumnIndexOrThrow(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TITLE));
            final String time = getString(getColumnIndexOrThrow(MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TIME));
            return new Favorite(itemId, url, title, Long.valueOf(time));
        }
    }


    /**
     * A {@link android.support.v4.content.CursorLoader} to query {@link Favorite}
     */
    public static class CursorLoader extends android.support.v4.content.CursorLoader {
        /**
         * Constructs a cursor loader to query all {@link Favorite}
         *
         * @param context an instance of {@link android.content.Context}
         */
        public CursorLoader(Context context) {
            super(context, MaterialisticProvider.URI_FAVORITE, null, null, null, null);
        }


        /**
         * Constructs a cursor loader to query {@link Favorite}
         * with title matching given query
         *
         * @param context an instance of {@link android.content.Context}
         * @param query   query to filter
         */
        public CursorLoader(Context context, String query) {
            super(context, MaterialisticProvider.URI_FAVORITE, null,
                    MaterialisticProvider.FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?",
                    new String[]{"%" + query + "%"}, null);
        }
    }

    /**
     * Callback interface for asynchronous favorite CRUD operations
     */
    public interface OperationCallbacks {
        /**
         * Fired when checking of favorite status is completed
         *
         * @param isFavorite true if is favorite, false otherwise
         */
        void onCheckComplete(boolean isFavorite);
    }

    private static class FavoriteHandler extends AsyncQueryHandler {
        private FavoriteCallback callback;


        public FavoriteHandler(ContentResolver cr, @NonNull FavoriteCallback callback) {
            this(cr);
            this.callback = callback;
        }


        public FavoriteHandler(ContentResolver cr) {
            super(cr);
        }


        @Override
        protected void onQueryComplete(int token, Object cookie, android.database.Cursor cursor) {
            if (cursor == null) {
                callback = null;
                return;
            }

            // cookie represents id
            if (cookie != null) callback.onCheckComplete(cursor.getCount() > 0);
            else {
                ArrayList<Favorite> favorites = new ArrayList<>(cursor.getCount());
                Cursor favoriteCursor = new Cursor(cursor);
                boolean any = favoriteCursor.moveToFirst();

                if (any) do {
                    favorites.add(favoriteCursor.getFavorite());
                } while (favoriteCursor.moveToNext());

                callback.onQueryComplete(favorites);
            }
            callback = null;
        }
    }

    private static abstract class FavoriteCallback {
        void onQueryComplete(ArrayList<Favorite> favorites) {
        }


        void onCheckComplete(boolean isFavorite) {
        }
    }
}
