package com.enamakel.thebigindiannews.data.models.base;

import com.google.gson.annotations.Expose;

import java.util.Date;

import lombok.Getter;

/**
 * Created by robert on 1/30/16.
 */
public abstract class BaseCardModel<T> extends BaseModel<T> {
    protected @Expose @Getter String created_by;
    protected @Expose @Getter double activity_hotness;
    protected @Expose @Getter double hotness;
//    protected @Expose @Getter Date created_at;
}
