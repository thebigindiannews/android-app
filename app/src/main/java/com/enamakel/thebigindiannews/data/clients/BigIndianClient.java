package com.enamakel.thebigindiannews.data.clients;


import android.content.ContentResolver;
import android.content.Context;

import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.google.gson.annotations.Expose;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
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
    static String TAG = "BigIndianClient";

    public static final String BASE_WEB_URL = "https://thebigindian.news";
    public static final String BASE_CDN_URL = "https://cdn.thebigindian.news";
    //    public static final String BASE_WEB_URL = "http://192.168.1.106:3000";
    public static final String BASE_API_URL = BASE_WEB_URL + "/api/";
    public static final String WEB_STORY_PATH = BASE_WEB_URL + "/story/%s";

    final SessionManager sessionManager;
    final FavoriteManager favoriteManager;
    final ContentResolver contentResolver;


    @Inject
    public BigIndianClient(Context context, SessionManager sessionManager,
                           FavoriteManager favoriteManager) {
        // Initialize retrofit
        Retrofit retrofit = RetrofitFactory.build(BASE_API_URL);

        // Create the service!
        restService = retrofit.create(RestService.class);

        // Initialize other stuff
        this.sessionManager = sessionManager;
        this.favoriteManager = favoriteManager;
        contentResolver = context.getApplicationContext().getContentResolver();
    }


    public void getStories(FetchMode mode, int page, final ResponseListener<List<StoryModel>> listener) {
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
                restService.latest("true", page).enqueue(callback);
                break;

            case TOP_STORIES:
                restService.top(page).enqueue(callback);
                break;
        }
    }


    /**
     * Fetch a single story given it's id.
     *
     * @param itemId   Id of the story
     * @param listener Listener to be called with the story
     */
    public void getItem(String itemId, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

        Call<StoryModel> call = restService.getById(itemId);

        ItemCallbackWrapper wrapper = new ItemCallbackWrapper(listener);
        favoriteManager.check(contentResolver, itemId, wrapper);
        sessionManager.isViewed(contentResolver, itemId, wrapper);
        call.enqueue(wrapper);
    }


    /**
     * Create a new story.
     *
     * @param story
     * @param listener
     */
    public void submit(StoryModel story, final ResponseListener<StoryModel> listener) {
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


    public void readStory(final StoryModel story) {
        story.setClicks_count(story.getClicks_count() + 1);
        Call<Object> call = restService.readStory(story.getId());
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Response<Object> response) {
            }


            @Override
            public void onFailure(Throwable t) {

            }
        });
    }


    class ItemCallbackWrapper implements SessionManager.OperationCallbacks,
            FavoriteManager.OperationCallbacks,
            Callback<StoryModel> {
        private final ResponseListener<StoryModel> responseListener;
        private Boolean isViewed;
        private Boolean isFavorite;
        private StoryModel story;
        private String errorMessage;
        private boolean hasError;
        private boolean hasResponse;


        private ItemCallbackWrapper(@NonNull ResponseListener<StoryModel> responseListener) {
            this.responseListener = responseListener;
        }


        @Override
        public void onCheckViewedComplete(boolean isViewed) {
            this.isViewed = isViewed;
            done();
        }


        @Override
        public void onCheckFavoriteComplete(boolean isFavorite) {
            this.isFavorite = isFavorite;
            done();
        }


        void done() {
            if (isViewed == null) return;
            if (isFavorite == null) return;
            if (!(hasResponse || hasError)) return;

            if (hasResponse) {
                if (story != null) {
                    story.setFavorite(isFavorite);
                    story.setViewed(isViewed);
                }
                responseListener.onResponse(story);
            } else responseListener.onError(errorMessage);
        }


        @Override
        public void onResponse(Response<StoryModel> response) {
            this.story = response.body();
            this.hasResponse = true;
            done();
        }


        @Override
        public void onFailure(Throwable t) {
            this.errorMessage = t != null ? t.getMessage() : "";
            this.hasError = true;
            done();
        }
    }


    interface RestService {
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
        Call<Object> readStory(@Path("id") String id);
    }


    class StoryHits {
        @Expose List<StoryModel> docs;
        @Expose int total;
        @Expose int limit;
        @Expose int offset;
    }
}