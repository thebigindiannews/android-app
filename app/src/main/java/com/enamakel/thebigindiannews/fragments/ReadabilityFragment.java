/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enamakel.thebigindiannews.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.clients.ReadabilityClient;
import com.enamakel.thebigindiannews.data.clients.ReadabilityClient2;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.Preferences;
import com.enamakel.thebigindiannews.util.Scrollable;

import java.lang.ref.WeakReference;

import javax.inject.Inject;


public class ReadabilityFragment extends LazyLoadFragment implements Scrollable {
    public static final String EXTRA_ITEM = ReadabilityFragment.class.getName() + ".EXTRA_ITEM";
    static final String STATE_CONTENT = "state:content";
    static final String STATE_TEXT_SIZE = "state:textSize";
    static final String STATE_TYPEFACE_NAME = "state:typefaceName";
    static final String FORMAT_HTML_COLOR = "%06X";

    NestedScrollView scrollView;
    WebView webView;
    ProgressBar progressBar;
    View emptyView;
    @Inject ReadabilityClient readabilityClient;
    String content;
    float textSize;
    String[] textSizeOptionValues;
    String typefaceName;
    String[] fontOptionValues;
    boolean isAttached;
    String textColor;
    String textLinkColor;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            textSize = savedInstanceState.getFloat(STATE_TEXT_SIZE);
            content = savedInstanceState.getString(STATE_CONTENT);
            typefaceName = savedInstanceState.getString(STATE_TYPEFACE_NAME);
        } else {
            textSize = toHtmlPx(Preferences.Theme.resolvePreferredReadabilityTextSize(getActivity()));
            typefaceName = Preferences.Theme.getReadabilityTypeface(getActivity());
        }

        textColor = toHtmlColor(android.R.attr.textColorPrimary);
        textLinkColor = toHtmlColor(android.R.attr.textColorLink);
    }


    @Override
    protected void createOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_font_options, menu);
        textSizeOptionValues = getResources().getStringArray(R.array.pref_text_size_values);
        fontOptionValues = getResources().getStringArray(R.array.font_values);
        SubMenu subMenu = menu.findItem(R.id.menu_font_size).getSubMenu();
        String[] options = getResources().getStringArray(R.array.text_size_options);
        String initialTextSize = Preferences.Theme.getPreferredReadabilityTextSize(getActivity());

        for (int i = 0; i < options.length; i++) {
            MenuItem item = subMenu.add(R.id.menu_font_size_group, Menu.NONE, i, options[i]);
            item.setChecked(TextUtils.equals(initialTextSize, textSizeOptionValues[i]));
        }

        subMenu.setGroupCheckable(R.id.menu_font_size_group, true, true);
        subMenu = menu.findItem(R.id.menu_font).getSubMenu();
        options = getResources().getStringArray(R.array.font_options);
        String initialTypeface = Preferences.Theme.getReadabilityTypeface(getActivity());
        for (int i = 0; i < options.length; i++) {
            MenuItem item = subMenu.add(R.id.menu_font_group, Menu.NONE, i, options[i]);
            item.setChecked(TextUtils.equals(initialTypeface, fontOptionValues[i]));
        }
        subMenu.setGroupCheckable(R.id.menu_font_group, true, true);
    }


    @Override
    protected void prepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_font_options).setVisible(!TextUtils.isEmpty(content));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_font_size) return true;

        if (item.getGroupId() == R.id.menu_font_size_group) {
            item.setChecked(true);
            String choice = textSizeOptionValues[item.getOrder()];
            textSize = toHtmlPx(Preferences.Theme.resolveTextSize(choice));
            Preferences.Theme.savePreferredReadabilityTextSize(getActivity(), choice);
            render();
        } else if (item.getGroupId() == R.id.menu_font_group) {
            item.setChecked(true);
            typefaceName = fontOptionValues[item.getOrder()];
            Preferences.Theme.savePreferredReadabilityTypeface(getActivity(), typefaceName);
            render();
        }

        return true;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_readability, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        scrollView = (NestedScrollView) view.findViewById(R.id.nested_scroll_view);
        webView = (WebView) view.findViewById(R.id.content);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(ContextCompat.getColor(getActivity(),
                AppUtils.getThemedResId(getActivity(), R.attr.colorCardBackground)));
        emptyView = view.findViewById(R.id.empty);
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(STATE_TEXT_SIZE, textSize);
        outState.putString(STATE_CONTENT, content);
        outState.putString(STATE_TYPEFACE_NAME, typefaceName);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }


    @Override
    public void scrollToTop() {
        scrollView.smoothScrollTo(0, 0);
    }


    @Override
    protected void load() {
        if (TextUtils.isEmpty(content)) parse();
        else bind();
    }


    private void parse() {
        StoryModel item = getArguments().getParcelable(EXTRA_ITEM);
        if (item == null) return;
        progressBar.setVisibility(View.VISIBLE);
        ReadabilityClient2.parse(item.getId(), item.getUrl(), new ReadabilityCallback(this));
//        readabilityClient.parse(item.getId(), item.getUrl(), new ReadabilityCallback(this));
    }


    private void onParsed(String content) {
        this.content = content;
        bind();
    }


    private void bind() {
        if (!isAttached) return;

        progressBar.setVisibility(View.GONE);
        getActivity().supportInvalidateOptionsMenu();
        if (!TextUtils.isEmpty(content)) render();
        else emptyView.setVisibility(View.VISIBLE);
    }


    private void render() {
        webView.loadDataWithBaseURL(null, wrap(content), "text/html", "UTF-8", null);
    }


    private String wrap(String html) {
        return getString(R.string.readability_html,
                typefaceName,
                textSize,
                textColor,
                textLinkColor,
                html,
                toHtmlPx(getResources().getDimension(R.dimen.activity_vertical_margin)),
                toHtmlPx(getResources().getDimension(R.dimen.activity_horizontal_margin)));
    }


    private String toHtmlColor(@AttrRes int colorAttr) {
        return String.format(FORMAT_HTML_COLOR, 0xFFFFFF & ContextCompat.getColor(getActivity(),
                AppUtils.getThemedResId(getActivity(), colorAttr)));
    }


    private float toHtmlPx(@StyleRes int textStyleAttr) {
        return toHtmlPx(AppUtils.getDimension(getActivity(), textStyleAttr, R.attr.contentTextSize));
    }


    private float toHtmlPx(float dimen) {
        return dimen / getResources().getDisplayMetrics().density;
    }


    private static class ReadabilityCallback implements ReadabilityClient2.Callback {
        private final WeakReference<ReadabilityFragment> readabilityFragment;


        public ReadabilityCallback(ReadabilityFragment readabilityFragment) {
            this.readabilityFragment = new WeakReference<>(readabilityFragment);
        }


        @Override
        public void onResponse(String content) {
            if (readabilityFragment.get() != null && readabilityFragment.get().isAttached())
                readabilityFragment.get().onParsed(content);
        }
    }
}
