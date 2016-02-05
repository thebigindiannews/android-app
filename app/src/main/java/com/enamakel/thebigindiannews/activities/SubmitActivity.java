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

package com.enamakel.thebigindiannews.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.accounts.UserServices;
import com.enamakel.thebigindiannews.activities.base.InjectableActivity;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.clients.BigIndianClient;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;


public class SubmitActivity extends InjectableActivity {
    static final String HN_GUIDELINES_URL = "https://news.ycombinator.com/newsguidelines.html";
    @Inject UserServices mUserServices;
    @Inject AlertDialogBuilder alertDialogBuilder;
    TextView titleEditText;
    TextView contentEditText;
    TextInputLayout titleLayout;
    TextInputLayout contentLayout;
    boolean sending, loading;

    @Inject BigIndianClient bigIndianClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        titleLayout = (TextInputLayout) findViewById(R.id.textinput_title);
        contentLayout = (TextInputLayout) findViewById(R.id.textinput_content);
        titleEditText = (TextView) findViewById(R.id.edittext_title);
        contentEditText = (TextView) findViewById(R.id.edittext_content);

        titleEditText.setText(getIntent().getStringExtra(Intent.EXTRA_SUBJECT));
        contentEditText.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submit, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_send).setEnabled(!sending);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.menu_send) {
            if (!validate()) return true;

            submit(isUrl());
//            final boolean isUrl = isUrl();
//            alertDialogBuilder
//                    .init(SubmitActivity.this)
//                    .setMessage(isUrl ? R.string.confirm_submit_url :
//                            R.string.confirm_submit_question)
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            submit(isUrl);
//                        }
//                    })
//                    .setNegativeButton(android.R.string.cancel, null)
//                    .create()
//                    .show();
            return true;
        }

        if (item.getItemId() == R.id.menu_guidelines) {
            WebView webView = new WebView(this);
            webView.loadUrl(HN_GUIDELINES_URL);
            alertDialogBuilder
                    .init(this)
                    .setView(webView)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        alertDialogBuilder
                .init(this)
                .setMessage(sending ? R.string.confirm_no_waiting : R.string.confirm_no_submit)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SubmitActivity.super.onBackPressed();
                    }
                })
                .show();
    }


    boolean validate() {
        titleLayout.setErrorEnabled(false);
        contentLayout.setErrorEnabled(false);

        if (titleEditText.length() == 0) titleLayout.setError(getString(R.string.title_required));
        if (contentEditText.length() == 0 || !isUrl())
            contentLayout.setError(getString(R.string.url_text_required));

        return titleEditText.length() > 0 && contentEditText.length() > 0 && isUrl();
    }


    void submit(boolean isUrl) {
        if (sending) {
            Toast.makeText(this, R.string.submit_ongoing, Toast.LENGTH_SHORT).show();
        } else {
            toggleControls(true);
            Toast.makeText(this, R.string.sending, Toast.LENGTH_LONG).show();

            StoryModel storyModel = new StoryModel(titleEditText.getText().toString(),
                    contentEditText.getText().toString());

            bigIndianClient.submit(storyModel, new SubmitCallback(this));
        }
    }


    void onSubmitted(Boolean successful) {
        toggleControls(false);

        if (successful == null || !successful) {
            Toast.makeText(this, R.string.submit_failed, Toast.LENGTH_SHORT).show();
        } else if (successful) {
            Toast.makeText(this, R.string.submit_successful, Toast.LENGTH_SHORT).show();
            if (!isFinishing()) {
                Intent intent = new Intent(this, NewActivity.class);
                intent.putExtra(NewActivity.EXTRA_REFRESH, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent); // TODO should go to story/profile instead?
                finish();
            }
        }
        // TODO If it failed then show login
//        } else if (!isFinishing()) AppUtils.showLogin(this, alertDialogBuilder);

    }


    boolean isUrl() {
        try {
            new URL(contentEditText.getText().toString()); // try parsing
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }


    void toggleControls(boolean sending) {
        if (isFinishing()) return;

        this.sending = sending;
        titleEditText.setEnabled(!sending);
        contentEditText.setEnabled(!sending);
        supportInvalidateOptionsMenu();
    }


    class SubmitCallback implements ResponseListener<StoryModel> {
        final WeakReference<SubmitActivity> weakReference;


        public SubmitCallback(SubmitActivity submitActivity) {
            weakReference = new WeakReference<>(submitActivity);
        }


        @Override
        public void onResponse(StoryModel response) {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed())
                weakReference.get().onSubmitted(response != null && response.getId() != null);

        }


        @Override
        public void onError(String errorMessage) {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed())
                weakReference.get().onSubmitted(null);
        }
    }
}
