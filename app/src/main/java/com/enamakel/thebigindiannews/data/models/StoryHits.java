package com.enamakel.thebigindiannews.data.models;


import com.google.gson.annotations.Expose;

import java.util.List;

import lombok.Data;


@Data
public class StoryHits {
    @Expose List<StoryModel> docs;
    @Expose int total;
    @Expose int limit;
    @Expose int offset;
}
