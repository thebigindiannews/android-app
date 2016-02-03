package com.enamakel.thebigindiannews.data.clients;


import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.google.gson.annotations.Expose;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class BigIndianClient {
    static RestService restService;
    public static final String BASE_WEB_URL = "https://thebigindian.news";
//    public static final String BASE_WEB_URL = "http://192.168.1.106:3000";
    public static final String BASE_API_URL = BASE_WEB_URL + "/api/";
    public static final String WEB_STORY_PATH = BASE_WEB_URL + "/story/%s";
    private static String TAG = "BigIndianClient";


    static {
        // Initialize retrofit
        Retrofit retrofit = RetrofitFactory.build(BASE_API_URL);

        // Create the service!
        restService = retrofit.create(RestService.class);
    }


    public static void getStories(FetchMode mode, int page, final ResponseListener<List<StoryModel>> listener) {
        if (listener == null) return;

        Callback<StoryHits> callback = new Callback<StoryHits>() {
            @Override
            public void onResponse(Response<StoryHits> response) {
                StoryHits storyHits = response.body();
                listener.onResponse(storyHits.docs);
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        };


        switch (mode) {
            case LATEST_STORIES:
                restService.search("true", page).enqueue(callback);
                break;

            case TOP_STORIES:
                restService.search("false", page).enqueue(callback);
                break;
        }
    }


    public static void getItem(String itemId, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

        Call<StoryModel> call = restService.getById(itemId);
        call.enqueue(new Callback<StoryModel>() {
            @Override
            public void onResponse(Response<StoryModel> response) {
                listener.onResponse(response.body());
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        });
    }


    public static void submit(StoryModel story, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

        Call<StoryModel> call = restService.create(story);
        call.enqueue(new Callback<StoryModel>() {
            @Override
            public void onResponse(Response<StoryModel> response) {
                listener.onResponse(response.body());
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        });
    }


    interface RestService {
        @Headers("Cache-Control: max-age=600")
        @GET("news/stories?recent=true")
        Call<StoryHits> searchByDate(@Query("query") String query);


        @Headers("Cache-Control: max-age=600")
        @GET("news/stories")
        Call<StoryHits> search(@Query("recent") String recent, @Query("page") int page);


        @GET("news/stories/{id}")
        Call<StoryModel> getById(@Path("id") String id);


        @POST("news/stories")
        Call<StoryModel> create(@Body StoryModel story);
    }


    static class StoryHits {
        @Expose List<StoryModel> docs;
        int total;
        int limit;
        int offset;
    }
}