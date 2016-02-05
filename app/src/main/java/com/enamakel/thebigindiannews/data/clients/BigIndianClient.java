package com.enamakel.thebigindiannews.data.clients;


import com.enamakel.thebigindiannews.data.clients.bigindian.ReportsClient;
import com.enamakel.thebigindiannews.data.clients.bigindian.StoriesClient;

import javax.inject.Inject;


public class BigIndianClient {
    public static final String BASE_CDN_URL = "https://cdn.thebigindian.news";
    public static final String BASE_WEB_URL = "https://thebigindian.news";
//    public static final String BASE_WEB_URL = "http://192.168.1.101:3000";

    @Inject public ReportsClient reports;
    @Inject public StoriesClient stories;
}