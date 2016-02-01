package com.enamakel.thebigindiannews.test;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.enamakel.thebigindiannews.activities.base.InjectableActivity;
import com.enamakel.thebigindiannews.util.MultiPaneListener;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;

import static org.mockito.Mockito.mock;

public class ListActivity extends InjectableActivity implements MultiPaneListener {
    public MultiPaneListener multiPaneListener = mock(MultiPaneListener.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    @Override
    public void onItemSelected(ItemManager.WebItem story) {
        multiPaneListener.onItemSelected(story);
    }

    @Override
    public ItemManager.WebItem getSelectedItem() {
        return multiPaneListener.getSelectedItem();
    }

    @Override
    public boolean isMultiPane() {
        return getResources().getBoolean(R.bool.multi_pane);
    }
}
