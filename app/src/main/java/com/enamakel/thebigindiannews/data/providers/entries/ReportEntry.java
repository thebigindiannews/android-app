package com.enamakel.thebigindiannews.data.providers.entries;


import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;


/**
 * Created by robert on 2/6/16.
 */
public interface ReportEntry {
    String TABLE_NAME = "reports";
    String MIME_TYPE = "vnd.android.cursor.dir/vnd." + BigIndianProvider.PROVIDER_AUTHORITY + "." + TABLE_NAME;
    String _ID = "_id";
    String COLUMN_NAME_JSON = "json";
}
