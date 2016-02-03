package com.enamakel.thebigindiannews.data.models;


import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.google.gson.annotations.Expose;

import lombok.Data;


/**
 * Created by robert on 2/3/16.
 */
@Data
public class IssueModel extends BaseCardModel<IssueModel> {
    static final String LABEL_FEEDBACK = "feedback";

    @Expose final String title;
    @Expose final String body;
    @Expose final String[] labels;


    public IssueModel(String title, String body) {
        this.title = title;
        this.body = body;
        this.labels = new String[]{LABEL_FEEDBACK};
    }
}