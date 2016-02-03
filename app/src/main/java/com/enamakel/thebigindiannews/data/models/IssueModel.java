package com.enamakel.thebigindiannews.data.models;


/**
 * Created by robert on 2/3/16.
 */
public class IssueModel {
    private static final String LABEL_FEEDBACK = "feedback";

    private final String title;
    private final String body;
    private final String[] labels;


    public IssueModel(String title, String body) {
        this.title = title;
        this.body = body;
        this.labels = new String[]{LABEL_FEEDBACK};
    }
}
