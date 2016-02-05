package com.enamakel.thebigindiannews.data.clients;


import com.enamakel.thebigindiannews.data.clients.bigindian.ReportsClient;
import com.enamakel.thebigindiannews.data.clients.bigindian.StoriesClient;

import javax.inject.Inject;


public class BigIndianClient {
    public static final String BASE_CDN_URL = "https://cdn.thebigindian.news";

    @Inject public ReportsClient reports;
    @Inject public StoriesClient stories;
}