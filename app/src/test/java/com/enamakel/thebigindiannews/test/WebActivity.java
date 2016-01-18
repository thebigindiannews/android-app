package com.enamakel.thebigindiannews.test;

import android.os.Bundle;

import com.enamakel.thebigindiannews.InjectableActivity;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.WebFragment;
import com.enamakel.thebigindiannews.data.ItemManager;

public class WebActivity extends InjectableActivity {
    public static final String EXTRA_ITEM = WebActivity.class.getName() + ".EXTRA_ITEM";
    public WebFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        ItemManager.WebItem item = getIntent().getParcelableExtra(EXTRA_ITEM);
        fragment = WebFragment.instantiate(this, item);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content,
                        fragment,
                        WebFragment.class.getName())
                .commit();
    }
}
