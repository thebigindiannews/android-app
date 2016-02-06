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

package com.enamakel.thebigindiannews.data.clients;

import android.content.Context;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.data.providers.managers.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.Preferences;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

public class AlgoliaClient implements ItemManager {
    public static boolean sSortByTime = true;
    static final String BASE_API_URL = "https://hn.algolia.com/api/v1/";
    protected RestService restService;

    @Inject @Named(ActivityModule.HN) ItemManager hackerNewsClient;


    @Inject
    public AlgoliaClient(Context context, RestServiceFactory factory) {
        restService = factory.create(BASE_API_URL, RestService.class);
        sSortByTime = Preferences.isSortByRecent(context);
    }


//    @Override
//    public void getStories(String filter, final ResponseListener<Item[]> listener) {
//        if (listener == null) return;
//
//        search(filter, new Callback<AlgoliaHits>() {
//            @Override
//            public void onResponse(Response<AlgoliaHits> response, Retrofit retrofit) {
//                AlgoliaHits algoliaHits = response.body();
//                Hit[] hits = algoliaHits.hits;
//                Item[] stories = new Item[hits == null ? 0 : hits.length];
//
//                for (int i = 0; i < stories.length; i++) {
//                    HackerNewsClient.HackerNewsItem item = new HackerNewsClient.HackerNewsItem(
//                            Long.parseLong(hits[i].objectID)
//                    );
//                    item.rank = i + 1;
//                    stories[i] = item;
//                }
//
//                listener.onResponse(stories);
//            }
//
//
//            @Override
//            public void onFailure(Throwable t) {
//                listener.onError(t != null ? t.getMessage() : "");
//            }
//        });
//    }


    @Override
    public void getStories(String filter, ResponseListener<List<StoryModel>> listener) {

    }


    @Override
    public void getItem(String itemId, ResponseListener<StoryModel> listener) {
//        hackerNewsClient.getItem(itemId, listener);
    }


    protected void search(String filter, Callback<AlgoliaHits> callback) {
        Call<AlgoliaHits> call;

        if (sSortByTime) call = restService.searchByDate(filter);
        else call = restService.search(filter);
        call.enqueue(callback);
    }


    interface RestService {
        @Headers("Cache-Control: max-age=600")
        @GET("search_by_date?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> searchByDate(@Query("query") String query);


        @Headers("Cache-Control: max-age=600")
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> search(@Query("query") String query);


        @Headers("Cache-Control: max-age=600")
        @GET("search?hitsPerPage=100&tags=story&attributesToRetrieve=objectID&attributesToHighlight=none")
        Call<AlgoliaHits> searchByMinTimestamp(@Query("numericFilters") String timestampSeconds);
    }


    protected static class AlgoliaHits {
        Hit[] hits;
    }


    static class Hit {
        String objectID;
    }
}
