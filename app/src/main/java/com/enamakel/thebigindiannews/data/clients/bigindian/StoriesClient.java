package com.enamakel.thebigindiannews.data.clients.bigindian;


import android.content.Context;

import com.enamakel.thebigindiannews.data.FavoriteManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.SessionManager;
import com.enamakel.thebigindiannews.data.clients.FetchMode;
import com.enamakel.thebigindiannews.data.clients.RestService;
import com.enamakel.thebigindiannews.data.models.StoryHits;
import com.enamakel.thebigindiannews.data.models.StoryModel;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class StoriesClient extends Base {
    static RestService.Story storyService;


    @Inject
    public StoriesClient(Context context, SessionManager sessionManager,
                 FavoriteManager favoriteManager) {
        super(context, sessionManager, favoriteManager);

        // Initialize retrofit
        Retrofit retrofit = RetrofitFactory.build(BASE_API_URL);
        storyService = retrofit.create(RestService.Story.class);
    }


    public void get(FetchMode mode, int page, final ResponseListener<List<StoryModel>> listener) {
        if (listener == null) return;

        Callback<StoryHits> callback = new Callback<StoryHits>() {
            @Override
            public void onResponse(Response<StoryHits> response) {
                StoryHits storyHits = response.body();
                listener.onResponse(storyHits.getDocs());
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        };


        switch (mode) {
            case LATEST_STORIES:
                storyService.latest("true", page).enqueue(callback);
                break;

            case TOP_STORIES:
                storyService.top(page).enqueue(callback);
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

        Call<StoryModel> call = storyService.getById(itemId);

        ItemCallbackWrapper wrapper = new ItemCallbackWrapper(listener);
        favoriteManager.check(contentResolver, itemId, wrapper);
        sessionManager.isViewed(contentResolver, itemId, wrapper);
        call.enqueue(wrapper);
    }


    /**
     * Create a new story.
     *
     * @param story    A {@link StoryModel} to create the story with.
     * @param listener Listener to be called once the story is created.
     */
    public void submit(StoryModel story, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

        Call<StoryModel> call = storyService.create(story);
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


    public void read(final StoryModel story) {
        story.setClicks_count(story.getClicks_count() + 1);
        Call<Object> call = storyService.read(story.getId());
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
        final ResponseListener<StoryModel> responseListener;
        Boolean isViewed;
        Boolean isFavorite;
        StoryModel story;
        String errorMessage;
        boolean hasError;
        boolean hasResponse;


        ItemCallbackWrapper(@NonNull ResponseListener<StoryModel> responseListener) {
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
}
