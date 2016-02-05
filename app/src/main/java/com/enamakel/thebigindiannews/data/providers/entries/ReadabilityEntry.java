package com.enamakel.thebigindiannews.data.providers.entries;


import android.provider.BaseColumns;

import com.enamakel.thebigindiannews.data.providers.BigIndianProvider;



public interface ReadabilityEntry extends BaseColumns {
    String TABLE_NAME = "readability";
    String MIME_TYPE = "vnd.android.cursor.dir/vnd." + BigIndianProvider.PROVIDER_AUTHORITY + "." + TABLE_NAME;
    String COLUMN_NAME_ITEM_ID = "itemid";
    String COLUMN_NAME_CONTENT = "content";
}
