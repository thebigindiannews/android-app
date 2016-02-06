package com.enamakel.thebigindiannews.data.providers.entries;


import android.provider.BaseColumns;

import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;


public interface FavoriteEntry extends BaseColumns {
    String TABLE_NAME = "favorite";
    String MIME_TYPE = "vnd.android.cursor.dir/vnd." + BigIndianProvider.PROVIDER_AUTHORITY + "." + TABLE_NAME;
    String COLUMN_NAME_JSON = "json";
    //        String COLUMN_NAME_URL = "url";
    String COLUMN_NAME_TITLE = "title";
    String COLUMN_NAME_EXCERPT = "excerpt";
    String COLUMN_NAME_TIME = "time";
}
