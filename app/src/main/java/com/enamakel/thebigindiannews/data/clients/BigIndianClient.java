package com.enamakel.thebigindiannews.data.clients;

import android.content.Context;
import android.util.Log;

import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.Preferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;

import javax.inject.Inject;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;


public class BigIndianClient implements ItemManager {
    protected RestService restService;
//    private static final String BASE_API_URL = "http://192.168.1.111:3000/api/";
    private static final String BASE_API_URL = "https://thebigindian.news/api/";
    public static boolean sSortByTime = true;
    private static String TAG = "BigIndianClient";


    @Inject
    public BigIndianClient(Context context, RestServiceFactory factory) {
        Gson gson = (new GsonBuilder())
                .registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory())
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        // Initialize retrofit to use GSON
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        restService = retrofit.create(RestService.class);
        sSortByTime = Preferences.isSortByRecent(context);
    }


    @Override
    public void getStories(String filter, final ResponseListener<List<StoryModel>> listener) {
        if (listener == null) return;

        search(filter, new Callback<StoryHits>() {
            @Override
            public void onResponse(Response<StoryHits> response, Retrofit retrofit) {
                StoryHits storyHits = response.body();
                listener.onResponse(storyHits.docs);
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        });
    }


    @Override
    public void getItem(String itemId, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

        Call<StoryModel> call = restService.getById(itemId);
        call.enqueue(new Callback<StoryModel>() {
            @Override
            public void onResponse(Response<StoryModel> response, Retrofit retrofit) {
                listener.onResponse(response.body());
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        });
    }


    protected void search(String filter, Callback<StoryHits> callback) {
        Call<StoryHits> call;

        if (sSortByTime) call = restService.searchByDate(filter);
        else call = restService.search(filter);
        call.enqueue(callback);
    }


    interface RestService {
        @Headers("Cache-Control: max-age=600")
        @GET("news/stories?recent=true")
        Call<StoryHits> searchByDate(@Query("query") String query);


        @Headers("Cache-Control: max-age=600")
        @GET("news/stories")
        Call<StoryHits> search(@Query("query") String query);


        @GET("news/stories/{id}")
        Call<StoryModel> getById(@Path("id") String id);
    }


    protected static class StoryHits {
        @Expose List<StoryModel> docs;
    }
}