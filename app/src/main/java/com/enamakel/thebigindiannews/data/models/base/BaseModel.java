package com.enamakel.thebigindiannews.data.models.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.concurrent.atomic.AtomicReference;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import lombok.Getter;


public abstract class BaseModel<T> implements Parcelable {
    protected @Getter @Expose String _id;
    protected @Getter final long longId;
    protected static Gson gson;

    private static AtomicReference<Long> currentTime =
            new AtomicReference<>(System.currentTimeMillis());


    static {
        gson = (new GsonBuilder())
                .registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory())
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }


    BaseModel() {
        longId = nextId();
    }
9

    @Override
    public int describeContents() {
        return 0;
    }


    public String getId() {
        return this._id;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        String json = gson.toJson(this);
        dest.writeString(json);
    }


    public static Long nextId() {
        Long prev;
        Long next = System.currentTimeMillis();
        do {
            prev = currentTime.get();
            next = next > prev ? next : prev + 1;
        } while (!currentTime.compareAndSet(prev, next));
        return next;
    }
}
