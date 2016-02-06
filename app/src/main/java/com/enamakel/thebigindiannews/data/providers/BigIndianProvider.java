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

package com.enamakel.thebigindiannews.data.providers;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.enamakel.thebigindiannews.data.providers.entries.FavoriteEntry;
import com.enamakel.thebigindiannews.data.providers.entries.ReadabilityEntry;
import com.enamakel.thebigindiannews.data.providers.entries.ReportEntry;
import com.enamakel.thebigindiannews.data.providers.entries.ViewedEntry;


public class BigIndianProvider extends ContentProvider {
    static final String TAG = BigIndianProvider.class.getSimpleName();
    public static final String PROVIDER_AUTHORITY = "com.enamakel.thebigindiannews.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + PROVIDER_AUTHORITY);

    public static final Uri URI_FAVORITE = BASE_URI.buildUpon()
            .appendPath(FavoriteEntry.TABLE_NAME)
            .build();

    public static final Uri URI_VIEWED = BASE_URI.buildUpon()
            .appendPath(ViewedEntry.TABLE_NAME)
            .build();

    public static final Uri URI_READABILITY = BASE_URI.buildUpon()
            .appendPath(ReadabilityEntry.TABLE_NAME)
            .build();

    public static final Uri URI_REPORT = BASE_URI.buildUpon()
            .appendPath(ReportEntry.TABLE_NAME)
            .build();


    static final String READABILITY_MAX_ENTRIES = "50";
    DbHelper dbHelper;


    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (URI_FAVORITE.equals(uri)) {
            return db.query(FavoriteEntry.TABLE_NAME, projection,
                    selection, selectionArgs,
                    null, null,
                    FavoriteEntry.COLUMN_NAME_TIME + DbHelper.ORDER_DESC);
        } else if (URI_VIEWED.equals(uri)) {
            return db.query(ViewedEntry.TABLE_NAME, projection,
                    selection, selectionArgs,
                    null, null,
                    ViewedEntry.COLUMN_NAME_ITEM_ID + DbHelper.ORDER_DESC);
        } else if (URI_REPORT.equals(uri)) {
            return db.query(ReportEntry.TABLE_NAME, projection,
                    selection, selectionArgs,
                    null, null, null);
        } else if (URI_READABILITY.equals(uri)) {
            return db.query(ReadabilityEntry.TABLE_NAME, projection,
                    selection, selectionArgs,
                    null, null,
                    ReadabilityEntry.COLUMN_NAME_ITEM_ID + DbHelper.ORDER_DESC);
        }
        return null;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        if (URI_FAVORITE.equals(uri)) return FavoriteEntry.MIME_TYPE;
        else if (URI_VIEWED.equals(uri)) return ViewedEntry.MIME_TYPE;
        else if (URI_REPORT.equals(uri)) return ReportEntry.MIME_TYPE;
        else if (URI_READABILITY.equals(uri)) return ReadabilityEntry.MIME_TYPE;

        return null;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (URI_FAVORITE.equals(uri)) {
            int updated = update(uri, values, FavoriteEntry.COLUMN_NAME_JSON + " = ?",
                    new String[]{values.getAsString(FavoriteEntry.COLUMN_NAME_JSON)});
            long id = -1;
            if (updated == 0) id = db.insert(FavoriteEntry.TABLE_NAME, null, values);

            return id == -1 ? null : ContentUris.withAppendedId(URI_FAVORITE, id);
        } else if (URI_VIEWED.equals(uri)) {
            int updated = update(uri, values, ViewedEntry.COLUMN_NAME_ITEM_ID + " = ?",
                    new String[]{values.getAsString(ViewedEntry.COLUMN_NAME_ITEM_ID)});
            long id = -1;

            if (updated == 0) id = db.insert(ViewedEntry.TABLE_NAME, null, values);
            return id == -1 ? null : ContentUris.withAppendedId(URI_VIEWED, id);
        } else if (URI_REPORT.equals(uri)) {
            int updated = update(uri, values, ReportEntry._ID + " = ?",
                    new String[]{values.getAsString(ReportEntry._ID)});
            long id = -1;

            if (updated == 0) id = db.insert(ReportEntry.TABLE_NAME, null, values);
            return id == -1 ? null : ContentUris.withAppendedId(URI_VIEWED, id);
        } else if (URI_READABILITY.equals(uri)) {
            int updated = update(uri, values, ReadabilityEntry.COLUMN_NAME_ITEM_ID + " = ?",
                    new String[]{values.getAsString(ReadabilityEntry.COLUMN_NAME_ITEM_ID)});
            long id = -1;

            if (updated == 0) {
                id = db.insert(ReadabilityEntry.TABLE_NAME, null, values);
                db.delete(ReadabilityEntry.TABLE_NAME, DbHelper.SQL_WHERE_READABILITY_TRUNCATE, null);
            }
            return id == -1 ? null : ContentUris.withAppendedId(URI_READABILITY, id);
        }

        return null;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = null;

        if (URI_FAVORITE.equals(uri)) table = FavoriteEntry.TABLE_NAME;
        else if (URI_VIEWED.equals(uri)) table = ViewedEntry.TABLE_NAME;
        else if (URI_READABILITY.equals(uri)) table = ReadabilityEntry.TABLE_NAME;
        else if (URI_REPORT.equals(uri)) table = ReportEntry.TABLE_NAME;


        if (TextUtils.isEmpty(table)) return 0;
        return db.delete(table, selection, selectionArgs);
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String table = null;

        if (URI_FAVORITE.equals(uri)) table = FavoriteEntry.TABLE_NAME;
        else if (URI_VIEWED.equals(uri)) table = ViewedEntry.TABLE_NAME;
        else if (URI_READABILITY.equals(uri)) table = ReadabilityEntry.TABLE_NAME;
        else if (URI_REPORT.equals(uri)) table = ReportEntry.TABLE_NAME;

        if (TextUtils.isEmpty(table)) return 0;
        return db.update(table, values, selection, selectionArgs);
    }


    static class DbHelper extends SQLiteOpenHelper {
        static final String DB_NAME = "BigIndian.db";
        static final int DB_VERSION = 5;
        static final String TEXT_TYPE = " TEXT";
        static final String INTEGER_TYPE = " INTEGER";
        static final String PRIMARY_KEY = " PRIMARY KEY";
        static final String COMMA_SEP = ",";
        static final String ORDER_DESC = " DESC";

        static final String SQL_CREATE_FAVORITE_TABLE =
                "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                        FavoriteEntry._ID + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                        FavoriteEntry.COLUMN_NAME_JSON + TEXT_TYPE + COMMA_SEP +
                        FavoriteEntry.COLUMN_NAME_EXCERPT + TEXT_TYPE + COMMA_SEP +
                        FavoriteEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                        FavoriteEntry.COLUMN_NAME_TIME + TEXT_TYPE +
                        " )";

        static final String SQL_CREATE_VIEWED_TABLE =
                "CREATE TABLE " + ViewedEntry.TABLE_NAME + " (" +
                        ViewedEntry._ID + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                        ViewedEntry.COLUMN_NAME_ITEM_ID + TEXT_TYPE +
                        " )";

        static final String SQL_CREATE_READABILITY_TABLE =
                "CREATE TABLE " + ReadabilityEntry.TABLE_NAME + " (" +
                        ReadabilityEntry._ID + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                        ReadabilityEntry.COLUMN_NAME_ITEM_ID + TEXT_TYPE + COMMA_SEP +
                        ReadabilityEntry.COLUMN_NAME_CONTENT + TEXT_TYPE +
                        " )";

        static final String SQL_CREATE_REPORTS_TABLE =
                "CREATE TABLE " + ReportEntry.TABLE_NAME + " (" +
                        ReportEntry._ID + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                        ReportEntry.COLUMN_NAME_JSON + TEXT_TYPE +
                        " )";

        static final String SQL_DROP_FAVORITE_TABLE =
                "DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME;

        static final String SQL_DROP_VIEWED_TABLE =
                "DROP TABLE IF EXISTS " + ViewedEntry.TABLE_NAME;

        static final String SQL_DROP_READABILITY_TABLE =
                "DROP TABLE IF EXISTS " + ReadabilityEntry.TABLE_NAME;

        static final String SQL_DROP_REPORTS_TABLE =
                "DROP TABLE IF EXISTS " + ReportEntry.TABLE_NAME;

        static final String SQL_WHERE_READABILITY_TRUNCATE = ReadabilityEntry._ID + " IN " +
                "(SELECT " + ReadabilityEntry._ID + " FROM " + ReadabilityEntry.TABLE_NAME +
                " ORDER BY " + ReadabilityEntry._ID + " DESC" +
                " LIMIT -1 OFFSET " + READABILITY_MAX_ENTRIES + ")";


        DbHelper(Context context) {
            super(context, DB_NAME, new LoggingCursorFactory(), DB_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_FAVORITE_TABLE);
            db.execSQL(SQL_CREATE_VIEWED_TABLE);
            db.execSQL(SQL_CREATE_READABILITY_TABLE);
            db.execSQL(SQL_CREATE_REPORTS_TABLE);
        }


        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                case 3:
                    db.execSQL(SQL_CREATE_REPORTS_TABLE);
                    break;
                case 2:
                    db.execSQL(SQL_CREATE_READABILITY_TABLE);
                    break;
                case 1:
                    db.execSQL(SQL_CREATE_VIEWED_TABLE);
                    break;
                default:
                    if (oldVersion < newVersion) {
                        db.execSQL(SQL_DROP_FAVORITE_TABLE);
                        db.execSQL(SQL_DROP_VIEWED_TABLE);
                        db.execSQL(SQL_DROP_READABILITY_TABLE);
                        db.execSQL(SQL_DROP_REPORTS_TABLE);
                        onCreate(db);
                    }
                    break;
            }
        }
    }


    static class LoggingCursorFactory implements SQLiteDatabase.CursorFactory {

        @Override
        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
            Log.d(TAG + ":query", query.toString());
            return new SQLiteCursor(masterQuery, editTable, query);
        }
    }
}