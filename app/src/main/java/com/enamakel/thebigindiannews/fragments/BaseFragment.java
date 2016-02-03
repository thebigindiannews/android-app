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


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.enamakel.thebigindiannews.NewsApplication;
import com.enamakel.thebigindiannews.util.Injectable;
import com.enamakel.thebigindiannews.util.MenuTintDelegate;


/**
 * Base fragment which performs injection using parent's activity object graphs if any
 */
public abstract class BaseFragment extends Fragment {
    protected final MenuTintDelegate menuTintDelegate = new MenuTintDelegate();
    boolean isAttached;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof Injectable) ((Injectable) getActivity()).inject(this);
        menuTintDelegate.onActivityCreated(getActivity());
    }


    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAttached()) { // TODO http://b.android.com/80783
            createOptionsMenu(menu, inflater);
            menuTintDelegate.onOptionsMenuCreated(menu);
        }
    }


    @Override
    public final void onPrepareOptionsMenu(Menu menu) {
        // TODO http://b.android.com/80783
        if (isAttached()) prepareOptionsMenu(menu);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        NewsApplication.getRefWatcher(getActivity()).watch(this);
    }


    public boolean isAttached() {
        return isAttached;
    }


    protected void createOptionsMenu(Menu menu, MenuInflater inflater) {
        // override to create options menu
    }


    protected void prepareOptionsMenu(Menu menu) {
        // override to prepare options menu
    }
}
