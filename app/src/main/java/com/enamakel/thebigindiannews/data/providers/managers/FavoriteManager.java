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

package com.enamakel.thebigindiannews.data.providers.managers;


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
import android.util.Log;

import com.enamakel.thebigindiannews.data.FavoriteModel;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.data.providers.entries.FavoriteEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;


/**
 * Data repository for {@link FavoriteModel}
 */
public class FavoriteManager {
    static final String TAG = FavoriteManager.class.getSimpleName();
    public static final int LOADER = 0;
    public static ArrayList<String> favoriteIds = new ArrayList<>();


    /**
     * {@link android.content.Intent#getAction()} for broadcasting getting favorites matching query
     */
    public static final String ACTION_GET = FavoriteManager.class.getName() + ".ACTION_GET";

    /**
     * {@link android.os.Bundle} key for {@link #ACTION_GET} that contains {@link ArrayList} of
     * {@link FavoriteModel}
     */
    public static final String ACTION_GET_EXTRA_DATA = ACTION_GET + ".EXTRA_DATA";
    static final String URI_PATH_ADD = "add";
    static final String URI_PATH_REMOVE = "remove";
    static final String URI_PATH_CLEAR = "clear";


    @Inject
    public FavoriteManager(Context context) {
        Log.d(TAG, "initialize");
        refresh(context);
    }


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
            Log.d(TAG + ":get", "");
            selection = null;
            selectionArgs = null;
        } else {
            Log.d(TAG + ":get", query);
            selection = FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%"};
        }

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        new FavoriteHandler(context.getContentResolver(), new FavoriteCallback() {
            @Override
            void onQueryComplete(ArrayList<StoryModel> favorites) {
                favorites.clear();
                for (StoryModel story : favorites) favoriteIds.add(story.getId());

                broadcastManager.sendBroadcast(makeGetBroadcastIntent(favorites));
            }
        }).startQuery(0, null, BigIndianProvider.URI_FAVORITE,
                null, selection, selectionArgs, null);
    }


    public void refresh(Context context) {
        Log.d(TAG + ":refresh", "");
        new FavoriteHandler(context.getContentResolver(), new FavoriteCallback() {
            @Override
            void onQueryComplete(ArrayList<StoryModel> favorites) {
                favoriteIds.clear();
                for (StoryModel story : favorites) favoriteIds.add(story.getId());
            }
        }).startQuery(0, null, BigIndianProvider.URI_FAVORITE, null, null, null, null);
    }


    /**
     * Adds given story as favorite
     *
     * @param context an instance of {@link android.content.Context}
     * @param story   story to be added as favorite
     */
    public void add(Context context, StoryModel story) {
        Log.d(TAG + ":add", story.getId());

        final ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteEntry._ID, story.getId());
        contentValues.put(FavoriteEntry.COLUMN_NAME_JSON, story.toJSON());
        contentValues.put(FavoriteEntry.COLUMN_NAME_TIME, String.valueOf((new Date()).getTime()));
        contentValues.put(FavoriteEntry.COLUMN_NAME_EXCERPT, story.getExcerpt());
        contentValues.put(FavoriteEntry.COLUMN_NAME_TITLE, story.getTitle());

        ContentResolver contentResolver = context.getContentResolver();
        new FavoriteHandler(contentResolver).startInsert(0, story.getId(),
                BigIndianProvider.URI_FAVORITE, contentValues);

        favoriteIds.add(story.getId());
        contentResolver.notifyChange(buildAdded().appendPath(story.getId()).build(), null);
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
            selection = FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%"};
        }

        ContentResolver cr = context.getContentResolver();
        new FavoriteHandler(cr).startDelete(0, null, BigIndianProvider.URI_FAVORITE,
                selection, selectionArgs);

        refresh(context);
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
        Log.d(TAG + ":check", itemId);
        if (itemId == null) return;
        if (callbacks == null) return;

        new FavoriteHandler(contentResolver, new FavoriteCallback() {
            @Override
            void onCheckComplete(boolean isFavorite) {
                Log.d(TAG + ":check", itemId + " -> " + isFavorite);
                callbacks.onCheckFavoriteComplete(isFavorite);
            }
        }).startQuery(0, itemId, BigIndianProvider.URI_FAVORITE, null,
                FavoriteEntry._ID + " = ?",
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
        Log.d(TAG + ":remove", story.getId());

        ContentResolver cr = context.getContentResolver();
        new FavoriteHandler(cr).startDelete(0, story.get_id(),
                BigIndianProvider.URI_FAVORITE,
                FavoriteEntry._ID + " = ?",
                new String[]{story.getId()});

        favoriteIds.remove(story.getId());
        cr.notifyChange(buildRemoved().appendPath(story.getId()).build(), null);
    }


    /**
     * Removes multiple stories with given IDs from favorites be sent upon completion
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
                    contentResolver.delete(BigIndianProvider.URI_FAVORITE,
                            FavoriteEntry._ID + " = ?",
                            new String[]{param});
                }

                return null;
            }
        }.execute(itemIds.toArray(new String[itemIds.size()]));

        for (String itemId : itemIds) {
            favoriteIds.remove(itemId);
            contentResolver.notifyChange(buildRemoved().appendPath(itemId).build(), null);
        }
    }


    /**
     * Creates an intent filter for build action broadcast
     *
     * @return build intent filter
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


    static Uri.Builder buildAdded() {
        return BigIndianProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_ADD);
    }


    static Uri.Builder buildRemoved() {
        return BigIndianProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_REMOVE);
    }


    static Uri.Builder buildCleared() {
        return BigIndianProvider.URI_FAVORITE.buildUpon().appendPath(URI_PATH_CLEAR);
    }


    static Intent makeGetBroadcastIntent(ArrayList<StoryModel> favorites) {
        final Intent intent = new Intent(ACTION_GET);
        intent.putExtra(ACTION_GET_EXTRA_DATA, favorites);
        return intent;
    }


    /**
     * A cursor wrapper to retrieve associated {@link FavoriteModel}
     */
    public static class Cursor extends CursorWrapper {
        public Cursor(android.database.Cursor cursor) {
            super(cursor);
        }


        public StoryModel getFavorite() {
            String json = getString(getColumnIndexOrThrow(
                    FavoriteEntry.COLUMN_NAME_JSON));
            StoryModel story = StoryModel.fromJSON(json);
            story.setFavorite(true);
            return story;
        }
    }


    /**
     * A {@link android.support.v4.content.CursorLoader} to query {@link FavoriteModel}
     */
    public static class CursorLoader extends android.support.v4.content.CursorLoader {
        /**
         * Constructs a cursor loader to query all {@link FavoriteModel}
         *
         * @param context an instance of {@link android.content.Context}
         */
        public CursorLoader(Context context) {
            super(context, BigIndianProvider.URI_FAVORITE, null, null, null, null);
        }


        /**
         * Constructs a cursor loader to query {@link FavoriteModel}
         * with title matching given query
         *
         * @param context an instance of {@link android.content.Context}
         * @param query   query to filter
         */
        public CursorLoader(Context context, String query) {
            super(context, BigIndianProvider.URI_FAVORITE, null,
                    FavoriteEntry.COLUMN_NAME_TITLE + " LIKE ?",
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
        void onCheckFavoriteComplete(boolean isFavorite);
    }


    static class FavoriteHandler extends AsyncQueryHandler {
        FavoriteCallback callback;


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
                ArrayList<StoryModel> favorites = new ArrayList<>(cursor.getCount());
                favorites.clear();
                Cursor favoriteCursor = new Cursor(cursor);
                boolean any = favoriteCursor.moveToFirst();

                if (any) do {
                    StoryModel story = favoriteCursor.getFavorite();
                    favorites.add(story);
                    favoriteIds.add(story.getId());
                } while (favoriteCursor.moveToNext());

                callback.onQueryComplete(favorites);
            }
            callback = null;
        }
    }


    static abstract class FavoriteCallback {
        void onQueryComplete(ArrayList<StoryModel> favorites) {

        }


        void onCheckComplete(boolean isFavorite) {

        }
    }
}
