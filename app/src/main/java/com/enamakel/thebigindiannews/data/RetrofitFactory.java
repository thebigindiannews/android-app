package com.enamakel.thebigindiannews.data;


import android.util.Log;

import com.enamakel.thebigindiannews.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;


public class RetrofitFactory {
    private static String TAG = "okhttp";


    static OkHttpClient createClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();

        client.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Customize the request
                Request request = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("x-apikey", BuildConfig.API_KEY)
                        .method(original.method(), original.body())
                        .build();

                okhttp3.Response response = chain.proceed(request);

                // Customize or return the response
                return response;
            }
        });

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(interceptor);

        return client.build();
    }


    static Gson createGson() {
        return (new GsonBuilder())
                .registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory())
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }


    public static Retrofit build(String baseUrl) {
        // Initialize retrofit to use GSON
        return new retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .client(createClient())
                .build();
    }
}
