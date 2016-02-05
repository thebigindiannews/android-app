package com.enamakel.thebigindiannews.data.clients.bigindian;


import android.content.Context;

import com.enamakel.thebigindiannews.data.managers.FavoriteManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RetrofitFactory;
import com.enamakel.thebigindiannews.data.managers.SessionManager;
import com.enamakel.thebigindiannews.data.clients.RestService;
import com.enamakel.thebigindiannews.data.models.ReportModel;
import com.enamakel.thebigindiannews.data.models.StoryModel;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ReportsClient extends Base {
    static RestService.Report reportService;


    @Inject
    public ReportsClient(Context context, SessionManager sessionManager,
                         FavoriteManager favoriteManager) {
        super(context, sessionManager, favoriteManager);

        // Initialize retrofit
        Retrofit retrofit = RetrofitFactory.build(BASE_API_URL);
        reportService = retrofit.create(RestService.Report.class);
    }


    public void getItem(String itemId, final ResponseListener<StoryModel> listener) {
        if (listener == null) return;

//        Call<StoryModel> call = storyService.getById(itemId);
//
//        ItemCallbackWrapper wrapper = new ItemCallbackWrapper(listener);
//        favoriteManager.check(contentResolver, itemId, wrapper);
//        sessionManager.isViewed(contentResolver, itemId, wrapper);
//        call.enqueue(wrapper);
    }


    /**
     * Create a new report.
     *
     * @param report   A {@link ReportModel} to create the story with.
     * @param listener Listener to be called once the story is created.
     */
    public void submit(ReportModel report, final ResponseListener<ReportModel> listener) {
        if (listener == null) return;

        Call<ReportModel> call = reportService.create(report);
        call.enqueue(new Callback<ReportModel>() {
            @Override
            public void onResponse(Response<ReportModel> response) {
                listener.onResponse(response.body());
            }


            @Override
            public void onFailure(Throwable t) {
                listener.onError(t != null ? t.getMessage() : "");
            }
        });
    }
}
