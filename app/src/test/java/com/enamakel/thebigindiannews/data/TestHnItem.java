package com.enamakel.thebigindiannews.data;

import android.annotation.SuppressLint;

import com.enamakel.thebigindiannews.data.clients.HackerNewsClient;

@SuppressLint("ParcelCreator")
public class TestHnItem extends HackerNewsClient.HackerNewsItem {
    public TestHnItem(long id) {
        super(id);
    }
}
