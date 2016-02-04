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

package com.enamakel.thebigindiannews.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.models.StoryModel;
import com.enamakel.thebigindiannews.fragments.ReadabilityFragment;
import com.enamakel.thebigindiannews.fragments.WebFragment;


public class StoryPagerAdapter extends FragmentStatePagerAdapter {
    private final Fragment[] fragments = new Fragment[3];
    private final Context context;
    private final StoryModel story;
    private final boolean showArticle;


    public StoryPagerAdapter(Context context, FragmentManager fm,
                             StoryModel item, boolean showArticle) {
        super(fm);
        this.context = context;
        this.story = item;
        this.showArticle = showArticle;
    }


    @Override
    public Fragment getItem(int position) {
        if (fragments[position] != null) return fragments[position];

//        if (position == 0) {
//            Bundle args = new Bundle();
//            args.putParcelable(ItemFragment.EXTRA_ITEM, story);
//            return Fragment.instantiate(context,
//                    ItemFragment.class.getName(), args);
//        }

//        if (position == getCount() - 1) {
        if (position == 1) {
            Bundle readabilityArgs = new Bundle();
            readabilityArgs.putParcelable(ReadabilityFragment.EXTRA_ITEM, story);
            return Fragment.instantiate(context,
                    ReadabilityFragment.class.getName(), readabilityArgs);
        }

        return WebFragment.instantiate(context, story);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        fragments[position] = (Fragment) super.instantiateItem(container, position);
        return fragments[position];
    }


    @Override
    public int getCount() {
        return 2;
//        if (true/*story.isStoryType()*/) return showArticle ? 3 : 2;
//        else return 1;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) return context.getString(R.string.article);
        else return context.getString(R.string.readability);
//        if (position == 0) {
//            if (story instanceof StoryModel) {
////                int count = ((StoryModel) story).getKidCount();
//                int count = 0;
//                return context.getResources()
//                        .getQuantityString(R.plurals.comments_count, count, count);
//            }
//            return context.getString(R.string.title_activity_item);
//        }
//        if (position == getCount() - 1) return context.getString(R.string.readability);
//        return context.getString(R.string.article);
    }
}
