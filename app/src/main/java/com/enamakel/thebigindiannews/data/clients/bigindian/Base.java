package com.enamakel.thebigindiannews.data.clients.bigindian;


import android.content.ContentResolver;
import android.content.Context;

import com.enamakel.thebigindiannews.data.managers.FavoriteManager;
import com.enamakel.thebigindiannews.data.managers.SessionManager;
import com.enamakel.thebigindiannews.data.clients.BigIndianClient;


public class Base {
    public static final String BASE_API_URL = BigIndianClient.BASE_WEB_URL + "/api/";

    protected final SessionManager sessionManager;
    protected final FavoriteManager favoriteManager;
    protected final ContentResolver contentResolver;


    public Base(Context context, SessionManager sessionManager,
                FavoriteManager favoriteManager) {
        this.sessionManager = sessionManager;
        this.favoriteManager = favoriteManager;
        contentResolver = context.getApplicationContext().getContentResolver();
    }
}
