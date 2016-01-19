package com.enamakel.thebigindiannews.test;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.enamakel.thebigindiannews.activities.parent.InjectableActivity;
import com.enamakel.thebigindiannews.R;

public class TestItemActivity extends InjectableActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }
}
