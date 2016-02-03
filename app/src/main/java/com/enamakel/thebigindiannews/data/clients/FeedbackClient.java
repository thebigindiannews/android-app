package com.enamakel.thebigindiannews.data.clients;


import android.os.Build;
import android.util.Log;

import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.models.IssueModel;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


/**
 * This client is used to communicate with the Github API to submit issues
 */
public class FeedbackClient {
    static FeedbackService feedbackService;
    static String GITHUB_API_URL = "https://api.github.com/";


    static {
        // Initialize retrofit
        retrofit2.Retrofit retrofit = RetrofitFactory.build(GITHUB_API_URL);

        // Create the service!
        feedbackService = retrofit.create(FeedbackService.class);
    }


    public static void send(String title, String body, final FeedbackClient.Callback listener) {
        body = String.format("%s\nDevice: %s %s, SDK: %s, app version: %s",
                body,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.VERSION.SDK_INT,
                BuildConfig.VERSION_CODE);

        // Create the issue
        IssueModel issueModel = new IssueModel(title, body);

        // Submit to Github!
        feedbackService.createGithubIssue(issueModel)
                .enqueue(new retrofit2.Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response) {
                        Log.d("fuck", response.message());
                        listener.onResponse(true);
                    }


                    @Override
                    public void onFailure(Throwable t) {
                        listener.onResponse(false);
                    }
                });
    }


    interface FeedbackService {
        @POST("repos/thebigindiannews/android-app/issues")
        @Headers("Authorization: token " + BuildConfig.GITHUB_TOKEN)
        Call<Object> createGithubIssue(@Body IssueModel issue);
    }


    public interface Callback {
        void onResponse(Boolean status);
    }
}