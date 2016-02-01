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

import android.os.Build;

import javax.inject.Inject;

import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.data.RestServiceFactory;

import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

public interface FeedbackClient {
    interface Callback {
        void onSent(boolean success);
    }

    void send(String title, String body, Callback callback);

    class Impl implements FeedbackClient {
        private final FeedbackService mFeedbackService;

        @Inject
        public Impl(RestServiceFactory factory) {
            mFeedbackService = factory.create(FeedbackService.GITHUB_API_URL, FeedbackService.class);
        }

        @Override
        public void send(String title, String body, final Callback callback) {
            body = String.format("%s\nDevice: %s %s, SDK: %s, app version: %s",
                    body,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.SDK_INT,
                    BuildConfig.VERSION_CODE);
            mFeedbackService.createGithubIssue(new Issue(title, body))
                    .enqueue(new retrofit.Callback<Object>() {
                        @Override
                        public void onResponse(Response<Object> response, Retrofit retrofit) {
                            callback.onSent(true);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            callback.onSent(false);
                        }
                    });
        }

        interface FeedbackService {
            String GITHUB_API_URL = "https://api.github.com/";

            @POST("repos/thebigindiannews/android-app/issues")
            @Headers("Authorization: token " + BuildConfig.GITHUB_TOKEN)
            Call<Object> createGithubIssue(@Body Issue issue);
        }

        static class Issue {
            private static final String LABEL_FEEDBACK = "feedback";

            private final String title;
            private final String body;
            private final String[] labels;

            private Issue(String title, String body) {
                this.title = title;
                this.body = body;
                this.labels = new String[]{LABEL_FEEDBACK};
            }
        }
    }
}
