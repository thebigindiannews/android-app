package com.enamakel.thebigindiannews.data.providers.managers;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CursorWrapper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.enamakel.thebigindiannews.data.models.ReportModel;
import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;
import com.enamakel.thebigindiannews.data.providers.entries.ReportEntry;

import java.util.ArrayList;

import javax.inject.Inject;


public class ReportManager {
    static final String TAG = ReportManager.class.getSimpleName();
    static final ArrayList<String> reportedStories = new ArrayList<>();


    /**
     * {@link android.content.Intent#getAction()} for broadcasting getting reports matching query
     */
    public static final String ACTION_GET = ReportManager.class.getName() + ".ACTION_GET";


    /**
     * {@link android.os.Bundle} key for {@link #ACTION_GET} that contains {@link ArrayList} of
     * {@link ReportModel}
     */
    public static final String ACTION_GET_EXTRA_DATA = ACTION_GET + ".EXTRA_DATA";
    static final String URI_PATH_ADD = "add";
    static final String URI_PATH_REMOVE = "remove";
    static final String URI_PATH_CLEAR = "clear";


    @Inject
    public ReportManager(Context context) {
        Log.d(TAG, "initialize");
        refresh(context);
    }


    public void refresh(Context context) {
        Log.d(TAG + ":refresh", "");
        new ReportHandler(context.getContentResolver(), new ReportCallback() {
            @Override
            void onQueryComplete(ArrayList<ReportModel> reports) {
                reportedStories.clear();
                for (ReportModel report : reports) reportedStories.add(report.getStory());
            }
        }).startQuery(0, null, BigIndianProvider.URI_REPORT, null, null, null, null);
    }


    public void add(Context context, ReportModel report) {
        Log.d(TAG + ":add", report.getStory());

        final ContentValues contentValues = new ContentValues();
        contentValues.put(ReportEntry._ID, report.getStory());
        contentValues.put(ReportEntry.COLUMN_NAME_JSON, report.toJSON());

        ContentResolver contentResolver = context.getContentResolver();
        new ReportHandler(contentResolver).startInsert(0, report.getStory(),
                BigIndianProvider.URI_REPORT, contentValues);

        reportedStories.add(report.getStory());
        contentResolver.notifyChange(buildAdded().appendPath(report.getStory()).build(), null);
    }


    public void check(ContentResolver contentResolver, final String itemId,
                      final OperationCallbacks callbacks) {
        Log.d(TAG + ":check", itemId);
        Log.d(TAG + ":check", BigIndianProvider.URI_REPORT.toString());
        if (itemId == null) return;
        if (callbacks == null) return;

        new ReportHandler(contentResolver, new ReportCallback() {
            @Override
            void onCheckComplete(boolean isReported) {
                Log.d(TAG + ":check", itemId + " -> " + isReported);
                callbacks.onCheckReportComplete(isReported);
            }
        }).startQuery(0, itemId, BigIndianProvider.URI_REPORT, null,
                ReportEntry._ID + " = ?", new String[]{itemId}, null);
    }


    static Uri.Builder buildAdded() {
        return BigIndianProvider.URI_REPORT.buildUpon().appendPath(URI_PATH_ADD);
    }


    /**
     * A cursor wrapper to retrieve associated {@link ReportModel}
     */
    public static class Cursor extends CursorWrapper {
        public Cursor(android.database.Cursor cursor) {
            super(cursor);
        }


        public ReportModel getReport() {
            String json = getString(getColumnIndexOrThrow(ReportEntry.COLUMN_NAME_JSON));
            return ReportModel.fromJSON(json);
        }
    }


    static class ReportHandler extends AsyncQueryHandler {
        ReportCallback callback;


        public ReportHandler(ContentResolver contentResolver, @NonNull ReportCallback callback) {
            this(contentResolver);
            this.callback = callback;
        }


        public ReportHandler(ContentResolver cr) {
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
                ArrayList<ReportModel> reports = new ArrayList<>(cursor.getCount());
                reports.clear();
                Cursor reportCursor = new Cursor(cursor);
                boolean any = reportCursor.moveToFirst();

                if (any) do {
                    ReportModel report = reportCursor.getReport();
                    reports.add(report);
                } while (reportCursor.moveToNext());

                callback.onQueryComplete(reports);
            }
            callback = null;
        }
    }


    static abstract class ReportCallback {
        void onQueryComplete(ArrayList<ReportModel> reports) {

        }


        void onCheckComplete(boolean isReported) {

        }
    }


    /**
     * Callback interface for asynchronous report CRUD operations
     */
    public interface OperationCallbacks {
        /**
         * Fired when checking of report status is completed
         *
         * @param isReported true if story id has been reported already, false otherwise
         */
        void onCheckReportComplete(boolean isReported);
    }
}
