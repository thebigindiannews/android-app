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

package com.enamakel.thebigindiannews.activities.base;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.clients.FeedbackClient;
import com.enamakel.thebigindiannews.util.AlertDialogBuilder;

import java.lang.ref.WeakReference;

import javax.inject.Inject;


public abstract class DrawerActivity extends InjectableActivity {
    @Inject AlertDialogBuilder alertDialogBuilder;

    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    View drawer;
    Class<? extends Activity> pendingNavigation;
    Bundle pendingNavigationExtras;
    Dialog feedbackDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_drawer);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawer = findViewById(R.id.drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer,
                R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (drawerView.equals(drawer) && pendingNavigation != null) {
                    final Intent intent = new Intent(DrawerActivity.this, pendingNavigation);
                    if (pendingNavigationExtras != null) {
                        intent.putExtras(pendingNavigationExtras);
                        pendingNavigationExtras = null;
                    }
                    // TODO M bug https://code.google.com/p/android/issues/detail?id=193822
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    pendingNavigation = null;
                }
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawer)) closeDrawers();
        else super.onBackPressed();
    }


    @Override
    public void setContentView(int layoutResID) {
        ViewGroup drawerLayout = (ViewGroup) findViewById(R.id.drawer_layout);
        View view = getLayoutInflater().inflate(layoutResID, drawerLayout, false);
        drawerLayout.addView(view, 0);
    }


    public void navigate(Class<? extends Activity> activityClass, @Nullable Bundle extras) {
        pendingNavigation = !getClass().equals(activityClass) ? activityClass : null;
        pendingNavigationExtras = extras;
        closeDrawers();
    }


    public void showFeedback() {
        showFeedbackDialog(getLayoutInflater().inflate(R.layout.dialog_feedback, drawerLayout, false));
        closeDrawers();
    }


    void closeDrawers() {
        drawerLayout.closeDrawers();
    }


    void showFeedbackDialog(View dialogView) {
        AppUtils.setTextWithLinks((TextView) dialogView.findViewById(R.id.feedback_note),
                getString(R.string.feedback_note));
        final TextInputLayout titleLayout = (TextInputLayout)
                dialogView.findViewById(R.id.textinput_title);
        final TextInputLayout bodyLayout = (TextInputLayout)
                dialogView.findViewById(R.id.textinput_body);
        final EditText title = (EditText) dialogView.findViewById(R.id.edittext_title);
        final EditText body = (EditText) dialogView.findViewById(R.id.edittext_body);
        final View sendButton = dialogView.findViewById(R.id.feedback_button);
        feedbackDialog = alertDialogBuilder
                .init(this)
                .setView(dialogView)
                .create();
        dialogView.findViewById(R.id.button_rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.openPlayStore(DrawerActivity.this);
                feedbackDialog.dismiss();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleLayout.setErrorEnabled(false);
                bodyLayout.setErrorEnabled(false);
                if (title.length() == 0) titleLayout.setError(getString(R.string.title_required));
                if (body.length() == 0) bodyLayout.setError(getString(R.string.comment_required));
                if (title.length() == 0 || body.length() == 0) return;

                sendButton.setEnabled(false);

                FeedbackClient.send(title.getText().toString(), body.getText().toString(),
                        new FeedbackCallback(DrawerActivity.this));
            }
        });
        feedbackDialog.show();
    }


    void onFeedbackSent(boolean success) {
        Toast.makeText(DrawerActivity.this,
                success ? R.string.feedback_sent : R.string.feedback_failed,
                Toast.LENGTH_SHORT)
                .show();

        if (feedbackDialog == null || !feedbackDialog.isShowing()) return;

        if (success) feedbackDialog.dismiss();
        else feedbackDialog.findViewById(R.id.feedback_button).setEnabled(true);

    }


    class FeedbackCallback implements FeedbackClient.Callback {
        final WeakReference<DrawerActivity> weakReference;


        public FeedbackCallback(DrawerActivity drawerActivity) {
            weakReference = new WeakReference<>(drawerActivity);
        }


        @Override
        public void onResponse(Boolean success) {
            if (weakReference.get() != null && !weakReference.get().isActivityDestroyed())
                weakReference.get().onFeedbackSent(success);
        }
    }
}
