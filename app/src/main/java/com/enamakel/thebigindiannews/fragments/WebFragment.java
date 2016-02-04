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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.fragments.base.LazyLoadFragment;
import com.enamakel.thebigindiannews.util.Scrollable;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;


public class WebFragment extends LazyLoadFragment implements Scrollable {
    static final String EXTRA_ITEM = WebFragment.class.getName() + ".EXTRA_ITEM";
    StoryModel story;
    WebView webView;
    TextView textView;
    NestedScrollView scrollView;
    boolean isHackerNewsUrl;
    boolean externalRequired = false;
    @Inject @Named(ActivityModule.HN) ItemManager mItemManager;


    public static WebFragment instantiate(Context context, StoryModel story) {
        final WebFragment fragment = (WebFragment) instantiate(context, WebFragment.class.getName());
        fragment.story = story;
//        fragment.isHackerNewsUrl = AppUtils.isHackerNewsUrl(story);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) story = savedInstanceState.getParcelable(EXTRA_ITEM);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isHackerNewsUrl) return createLocalView(container, savedInstanceState);

        final View view = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_web, container, false);
        scrollView = (NestedScrollView) view.findViewById(R.id.nested_scroll_view);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        webView = (WebView) view.findViewById(R.id.web_view);
        webView.setBackgroundColor(ContextCompat.getColor(getContext(),
                AppUtils.getThemedResId(getContext(), android.R.attr.textColorTertiary)));
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    webView.setBackgroundColor(Color.WHITE);
                    webView.setVisibility(externalRequired ? View.GONE : View.VISIBLE);
                }
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                if (getActivity() == null) {
                    return;
                }
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                    return;
                }
                externalRequired = true;
                webView.setVisibility(View.GONE);
                view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intent);
                    }
                });
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
        setWebViewSettings(webView.getSettings());
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ITEM, story);
    }


    @Override
    public void scrollToTop() {
        scrollView.smoothScrollTo(0, 0);
    }


    @Override
    protected void load() {
        if (isHackerNewsUrl) bindContent();
        else if (story != null) webView.loadUrl(story.getUrl());
    }


    View createLocalView(ViewGroup container, Bundle savedInstanceState) {
        final View view = getLayoutInflater(savedInstanceState)
                .inflate(R.layout.fragment_web_hn, container, false);
        scrollView = (NestedScrollView) view.findViewById(R.id.nested_scroll_view);
        textView = (TextView) view.findViewById(R.id.text);
        return view;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setWebViewSettings(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }
    }


    void onItemLoaded(ItemManager.Item response) {
        AppUtils.setTextWithLinks(textView, response.getText());
    }


    void bindContent() {
        if (story instanceof ItemManager.Item) {
            AppUtils.setTextWithLinks(textView, ((ItemManager.Item) story).getText());
        } else {
//            itemManager.getItem(story.getId(), new StoryResponseListener(this));
        }
    }


    static class ItemResponseListener implements ResponseListener<ItemManager.Item> {
        final WeakReference<WebFragment> weakReference;


        public ItemResponseListener(WebFragment webFragment) {
            weakReference = new WeakReference<>(webFragment);
        }


        @Override
        public void onResponse(ItemManager.Item response) {
            if (weakReference.get() != null && weakReference.get().isAttached())
                weakReference.get().onItemLoaded(response);
        }


        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }
}