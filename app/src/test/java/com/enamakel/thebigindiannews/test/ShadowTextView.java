package com.enamakel.thebigindiannews.test;

import android.graphics.Typeface;
import android.widget.TextView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TextView.class)
public class ShadowTextView extends org.robolectric.shadows.ShadowTextView {
    private Typeface typeface;
    private int lineCount = 0;

    @Implementation
    public void setTypeface(Typeface tf) {
        typeface = tf;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }
    @Implementation
    public int getLineCount() {
        return lineCount;
    }
}
