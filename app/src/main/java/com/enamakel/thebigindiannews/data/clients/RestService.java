package com.enamakel.thebigindiannews.data.clients;


import com.enamakel.thebigindiannews.data.models.ReportModel;
import com.enamakel.thebigindiannews.data.models.StoryHits;
import com.enamakel.thebigindiannews.data.models.StoryModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by robert on 2/6/16.
 */
public interface RestService {
    public interface Report {
        @POST("news/reports")
        Call<ReportModel> create(@Body ReportModel report);
    }


    public interface Story {
        @Headers("Cache-Control: max-age=600")
        @GET("news/stories")
        Call<StoryHits> latest(@Query("recent") String recent, @Query("page") int page);


        @Headers("Cache-Control: max-age=600")
        @GET("news/stories")
        Call<StoryHits> top(@Query("page") int page);


        @Headers("Cache-Control: max-age=600")
        @GET("news/stories/{id}")
        Call<StoryModel> getById(@Path("id") String id);


        @POST("news/stories")
        Call<StoryModel> create(@Body StoryModel story);


        @POST("news/stories/{id}/open")
        Call<Object> read(@Path("id") String id);
    }
}
