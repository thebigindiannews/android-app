package com.enamakel.thebigindiannews.data.models;


import com.enamakel.thebigindiannews.data.models.base.BaseModel;
import com.google.gson.annotations.Expose;

import lombok.Data;


@Data
public class ReportModel extends BaseModel<ReportModel> {
    @Expose String story;
    @Expose String reason;
    @Expose Type type;


    public enum Type {
        CONFIDENTIAL,
        IRRELEVANT,
        JUNK,
        BAD_LANGUAGE,
        VULGUR,
        OTHER
    }


    public static ReportModel fromJSON(String json) {
        return gson.fromJson(json, ReportModel.class);
    }
}
