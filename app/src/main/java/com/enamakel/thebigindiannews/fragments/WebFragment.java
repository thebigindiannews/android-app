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

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

import com.enamakel.thebigindiannews.ActivityModule;
import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.models.base.BaseCardModel;
import com.enamakel.thebigindiannews.util.Scrollable;
import com.enamakel.thebigindiannews.data.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;

public class WebFragment extends LazyLoadFragment implements Scrollable {

    private static final String EXTRA_ITEM = WebFragment.class.getName() + ".EXTRA_ITEM";
    private BaseCardModel mItem;
    private WebView mWebView;
    private TextView mText;
    private NestedScrollView mScrollView;
    private boolean mIsHackerNewsUrl;
    private boolean mExternalRequired = false;
    @Inject @Named(ActivityModule.HN) ItemManager mItemManager;

    public static WebFragment instantiate(Context context, BaseCardModel item) {
        final WebFragment fragment = (WebFragment) instantiate(context, WebFragment.class.getName());
        fragment.mItem = item;
//        fragment.mIsHackerNewsUrl = AppUtils.isHackerNewsUrl(story);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mItem = savedInstanceState.getParcelable(EXTRA_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mIsHackerNewsUrl) {
            return createLocalView(container, savedInstanceState);
        }

        final View view = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_web, container, false);
        mScrollView = (NestedScrollView) view.findViewById(R.id.nested_scroll_view);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    mWebView.setBackgroundColor(Color.WHITE);
                    mWebView.setVisibility(mExternalRequired ? View.GONE : View.VISIBLE);
                }
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
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
                mExternalRequired = true;
                mWebView.setVisibility(View.GONE);
                view.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intent);
                    }
                });
            }
        });
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
        setWebViewSettings(mWebView.getSettings());
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ITEM, mItem);
    }

    @Override
    public void scrollToTop() {
        mScrollView.smoothScrollTo(0, 0);
    }

    @Override
    protected void load() {
        if (mIsHackerNewsUrl) {
            bindContent();
        } else if (mItem != null) {
//            mWebView.loadUrl(mItem.getUrl());
        }
    }

    private View createLocalView(ViewGroup container, Bundle savedInstanceState) {
        final View view = getLayoutInflater(savedInstanceState)
                .inflate(R.layout.fragment_web_hn, container, false);
        mScrollView = (NestedScrollView) view.findViewById(R.id.nested_scroll_view);
        mText = (TextView) view.findViewById(R.id.text);
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setWebViewSettings(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }
    }

    private void onItemLoaded(ItemManager.Item response) {
        AppUtils.setTextWithLinks(mText, response.getText());
    }

    private void bindContent() {
        if (mItem instanceof ItemManager.Item) {
            AppUtils.setTextWithLinks(mText, ((ItemManager.Item) mItem).getText());
        } else {
//            itemManager.getItem(mItem.getId(), new ItemResponseListener(this));
        }
    }

    private static class ItemResponseListener implements ResponseListener<ItemManager.Item> {
        private final WeakReference<WebFragment> mWebFragment;

        public ItemResponseListener(WebFragment webFragment) {
            mWebFragment = new WeakReference<>(webFragment);
        }

        @Override
        public void onResponse(ItemManager.Item response) {
            if (mWebFragment.get() != null && mWebFragment.get().isAttached()) {
                mWebFragment.get().onItemLoaded(response);
            }
        }

        @Override
        public void onError(String errorMessage) {
            // do nothing
        }
    }
}
