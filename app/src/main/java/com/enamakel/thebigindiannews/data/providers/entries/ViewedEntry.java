package com.enamakel.thebigindiannews.data.providers.entries;


import android.provider.BaseColumns;

import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;


public interface ViewedEntry extends BaseColumns {
    String TABLE_NAME = "viewed";
    String MIME_TYPE = "vnd.android.cursor.dir/vnd." + BigIndianProvider.PROVIDER_AUTHORITY + "." + TABLE_NAME;
    String COLUMN_NAME_ITEM_ID = "itemid";
}
