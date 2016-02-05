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

package com.enamakel.thebigindiannews.accounts;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
//import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.R;

public class UserServicesClient implements UserServices {
    static final String TAG_OK_HTTP = "OkHttp";
    static final String BASE_WEB_URL = "https://news.ycombinator.com";
    static final String LOGIN_PATH = "login";
    static final String VOTE_PATH = "vote";
    static final String COMMENT_PATH = "comment";
    static final String SUBMIT_PATH = "submit";
    static final String SUBMIT_POST_PATH = "r";
    static final String LOGIN_PARAM_ACCT = "acct";
    static final String LOGIN_PARAM_PW = "pw";
    static final String LOGIN_PARAM_CREATING = "creating";
    static final String LOGIN_PARAM_GOTO = "goto";
    static final String VOTE_PARAM_FOR = "for";
    static final String VOTE_PARAM_WHENCE = "whence";
    static final String VOTE_PARAM_DIR = "dir";
    static final String COMMENT_PARAM_PARENT = "parent";
    static final String COMMENT_PARAM_TEXT = "text";
    static final String SUBMIT_PARAM_TITLE = "title";
    static final String SUBMIT_PARAM_URL = "url";
    static final String SUBMIT_PARAM_TEXT = "text";
    static final String SUBMIT_PARAM_FNID = "fnid";
    static final String SUBMIT_PARAM_FNOP = "fnop";
    static final String VOTE_DIR_UP = "up";
    static final String DEFAULT_REDIRECT = "news";
    static final String CREATING_TRUE = "t";
    static final String DEFAULT_FNOP = "submit-page";
    static final String DEFAULT_SUBMIT_REDIRECT = "newest";
    static final String REGEX_INPUT = "<\\s*input[^>]*>";
    static final String REGEX_VALUE = "value[^\"]*\"([^\"]*)\"";
    static final String HEADER_LOCATION = "location";
    final Handler mUiHandler = new Handler(Looper.getMainLooper());
    final OkHttpClient mClient;

    public UserServicesClient(OkHttpClient okHttpClient) {
        mClient = okHttpClient;
//        HttpLoggingInterceptor interceptor =
//                new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
//                    @Override
//                    public void log(String message) {
//                        Log.d(TAG_OK_HTTP, message);
//                    }
//                });
//        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
//                HttpLoggingInterceptor.Level.NONE);
//        mClient.networkInterceptors().add(interceptor);
        mClient.setFollowRedirects(false);
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        mClient.setCookieHandler(cookieManager);
    }

    @Override
    public void login(final String username, final String password, boolean createAccount,
                      final Callback callback) {
        FormEncodingBuilder formBuilder = new FormEncodingBuilder()
                .add(LOGIN_PARAM_ACCT, username)
                .add(LOGIN_PARAM_PW, password)
                .add(LOGIN_PARAM_GOTO, DEFAULT_REDIRECT);
        if (createAccount) {
            formBuilder.add(LOGIN_PARAM_CREATING, CREATING_TRUE);
        }
        mClient.newCall(new Request.Builder()
                .url(HttpUrl.parse(BASE_WEB_URL)
                        .newBuilder()
                        .addPathSegment(LOGIN_PATH)
                        .build())
                .post(formBuilder.build())
                .build())
                .enqueue(wrap(callback));
    }

    @Override
    public void voteUp(Context context, String itemId, final Callback callback) {
        Pair<String, String> credentials = AppUtils.getCredentials(context);
        if (credentials == null) {
            callback.onDone(false);
            return;
        }
        Toast.makeText(context, R.string.sending, Toast.LENGTH_SHORT).show();
        mClient.newCall(new Request.Builder()
                .url(HttpUrl.parse(BASE_WEB_URL)
                        .newBuilder()
                        .addPathSegment(VOTE_PATH)
                        .build())
                .post(new FormEncodingBuilder()
                        .add(LOGIN_PARAM_ACCT, credentials.first)
                        .add(LOGIN_PARAM_PW, credentials.second)
                        .add(VOTE_PARAM_FOR, itemId)
                        .add(VOTE_PARAM_DIR, VOTE_DIR_UP)
                        .add(VOTE_PARAM_WHENCE, DEFAULT_REDIRECT)
                        .build())
                .build())
                .enqueue(wrap(callback));
    }

    @Override
    public void reply(Context context, String parentId, String text, Callback callback) {
        Pair<String, String> credentials = AppUtils.getCredentials(context);
        if (credentials == null) {
            callback.onDone(false);
            return;
        }
        mClient.newCall(new Request.Builder()
                .url(HttpUrl.parse(BASE_WEB_URL)
                        .newBuilder()
                        .addPathSegment(COMMENT_PATH)
                        .build())
                .post(new FormEncodingBuilder()
                        .add(LOGIN_PARAM_ACCT, credentials.first)
                        .add(LOGIN_PARAM_PW, credentials.second)
                        .add(COMMENT_PARAM_PARENT, parentId)
                        .add(COMMENT_PARAM_TEXT, text)
                        .build())
                .build())
                .enqueue(wrap(callback));
    }

    @Override
    public void submit(Context context, final String title, final String content, final boolean isUrl,
                       final Callback callback) {
        Pair<String, String> credentials = AppUtils.getCredentials(context);
        if (credentials == null) {
            callback.onDone(false);
            return;
        }
        /**
         * The flow:
         * POST /submit with acc, pw
         *  if 302 to /login, considered failed
         * POST /r with fnid, fnop, title, url or text
         *  if 302 to /newest, considered successful
         *  if 302 to /x, considered error, maybe duplicate or invalid input
         *  if 200 or anything else, considered error
         */
        // fetch submit page with given credentials
        mClient.newCall(new Request.Builder()
                .url(HttpUrl.parse(BASE_WEB_URL)
                        .newBuilder()
                        .addPathSegment(SUBMIT_PATH)
                        .build())
                .post(new FormEncodingBuilder()
                        .add(LOGIN_PARAM_ACCT, credentials.first)
                        .add(LOGIN_PARAM_PW, credentials.second)
                        .build())
                .build())
                .enqueue(new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        postError(callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        final boolean redirect = response.code() == HttpURLConnection.HTTP_MOVED_TEMP;
                        if (redirect) {
                            // redirect = failed login
                            postResult(callback, false);
                        } else {
                            // grab fnid from HTML and submit
                            doSubmit(title,
                                    content,
                                    getInputValue(response.body().string(), SUBMIT_PARAM_FNID),
                                    isUrl,
                                    callback);
                        }
                    }
                });
    }

    @WorkerThread
    void doSubmit(String title, String content, String fnid, boolean isUrl, final Callback callback) {
        if (TextUtils.isEmpty(fnid)) {
            postError(callback);
            return;
        }
        mClient.newCall(new Request.Builder()
                .url(HttpUrl.parse(BASE_WEB_URL)
                        .newBuilder()
                        .addPathSegment(SUBMIT_POST_PATH)
                        .build())
                .post(new FormEncodingBuilder()
                        .add(SUBMIT_PARAM_FNID, fnid)
                        .add(SUBMIT_PARAM_FNOP, DEFAULT_FNOP)
                        .add(SUBMIT_PARAM_TITLE, title)
                        .add(isUrl ? SUBMIT_PARAM_URL : SUBMIT_PARAM_TEXT, content)
                        .build())
                .build())
                .enqueue(new com.squareup.okhttp.Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        postError(callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        boolean redirect = response.code() == HttpURLConnection.HTTP_MOVED_TEMP;
                        if (!redirect) {
                            postError(callback);
                            return;
                        }
                        String location = response.header(HEADER_LOCATION);
                        switch (location) {
                            case DEFAULT_SUBMIT_REDIRECT:
                                postResult(callback, true);
                                break;
                            default:
                                postError(callback);
                                break;
                        }
                    }
                });
    }

    com.squareup.okhttp.Callback wrap(final Callback callback) {
        return new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                UserServicesClient.this.postError(callback);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                // redirect = successful submit
                postResult(callback, response.code() == HttpURLConnection.HTTP_MOVED_TEMP);
            }
        };
    }

    void postResult(final Callback callback, final boolean successful) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onDone(successful);
            }
        });
    }

    void postError(final Callback callback) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError();
            }
        });
    }

    String getInputValue(String html, String name) {
        // extract <input ... >
        Matcher matcherInput = Pattern.compile(REGEX_INPUT).matcher(html);
        while (matcherInput.find()) {
            String input = matcherInput.group();
            if (input.contains(name)) {
                // extract value="..."
                Matcher matcher = Pattern.compile(REGEX_VALUE).matcher(input);
                return matcher.find() ? matcher.group(1) : null; // return first match if any
            }
        }
        return null;
    }
}
