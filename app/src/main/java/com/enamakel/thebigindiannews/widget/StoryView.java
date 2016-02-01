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

package com.enamakel.thebigindiannews.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.models.StoryModel;

import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.ViewById;

import java.io.InputStream;

import lombok.Getter;

@EView
public class StoryView extends RelativeLayout implements Checkable {
    private static final int VOTE_DELAY_MILLIS = 500;
    private final int backgroundColor;
    private final int highlightColor;
    private final int tertiaryTextColorResId;
    private final int secondaryTextColorResId;
    private final int promotedColorResId;
    private final boolean isLocal;
    private @Getter boolean isChecked;

    @ViewById View bookmarked;
    @ViewById TextView posted;
    @ViewById TextView title;
    @ViewById TextView source;
    @ViewById View comment;
    @ViewById ViewSwitcher voteSwitcher;
    @ViewById LinearLayout rankContainer;
    @ViewById View buttonMore;
    @ViewById ImageView thumbnail;
    @ViewById TextView description;


    public StoryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public StoryView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StoryView);
        isLocal = ta.getBoolean(R.styleable.StoryView_local, false);
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.textColorTertiary,
                android.R.attr.textColorSecondary,
                R.attr.colorCardBackground,
                R.attr.colorCardHighlight
        });

        tertiaryTextColorResId = ContextCompat.getColor(context, a.getResourceId(0, 0));
        secondaryTextColorResId = ContextCompat.getColor(context, a.getResourceId(1, 0));
        backgroundColor = ContextCompat.getColor(context, a.getResourceId(2, 0));
        highlightColor = ContextCompat.getColor(context, a.getResourceId(3, 0));
        promotedColorResId = ContextCompat.getColor(context, R.color.greenA700);
        inflate(context, isLocal ? R.layout.local_story_view : R.layout.story_view, this);
        setBackgroundColor(backgroundColor);

        ta.recycle();
        a.recycle();
    }


    @Override
    public void setChecked(boolean checked) {
        if (isChecked == checked) return;
        isChecked = checked;
        setBackgroundColor(isChecked ? highlightColor : backgroundColor);
    }


    @Override
    public void toggle() {
        setChecked(!isChecked);
    }


    public void setStory(@NonNull StoryModel story) {
        Log.d("hello", story.getId());
        if (!isLocal) {
//            if (item.getKidCount() > 0) {
//                ((Button) comment).setText(getContext().getResources()
//                        .getQuantityString(R.plurals.comments_count, item.getKidCount(), item.getKidCount()));
//                comment.setVisibility(View.VISIBLE);
//            } else {
            comment.setVisibility(View.GONE);
//            }
        }

        // show The Image in a ImageView
        if (story.getThumbnail().getFilename() != null) {
            rankContainer.setBackgroundColor(Color.parseColor(story.getThumbnail().getColor()));
            new DownloadImageTask(thumbnail).execute(story.getThumbnail().getImage_url());
        }


        title.setText(getContext().getString(R.string.loading_text));
        title.setText(story.getTitle());
        description.setText(story.getExcerpt().replace('\n', ' '));
        source.setText(story.getSource());
        source.setCompoundDrawables(null, null, null, null);
        posted.setText(String.format("read %d times · %s read", story.getClicks_count(),
                story.getReadtime()));
    }


    public void reset() {
        if (!isLocal) bookmarked.setVisibility(INVISIBLE);

        title.setText(getContext().getString(R.string.loading_text));
        posted.setText(R.string.loading_text);
        source.setText(R.string.loading_text);
        source.setCompoundDrawables(null, null, null, null);
        comment.setVisibility(View.GONE);
    }


    public void setViewed(boolean isViewed) {
        if (isLocal) return; // local always means viewed, do not decorate
        title.setTextColor(isViewed ? secondaryTextColorResId : tertiaryTextColorResId);
    }


    public void setFavorite(boolean isFavorite) {
        if (isLocal) return; // local item must be favorite, do not decorate
        bookmarked.setVisibility(isFavorite ? View.VISIBLE : View.INVISIBLE);
    }


    public void setOnCommentClickListener(OnClickListener listener) {
        comment.setOnClickListener(listener);
    }


    public void setUpdated(@NonNull StoryModel story, boolean updated, boolean promoted) {
        if (isLocal) return; // local items do not change

//        if (story.getKidCount() > 0) {
//            ((Button) comment).setText(decorateUpdated(getContext().getResources()
//                            .getQuantityString(R.plurals.comments_count, story.getKidCount(), story.getKidCount()),
//                    story.hasNewKids()));
//        }
    }


    public View getMoreOptions() {
        return buttonMore;
    }


    private Spannable decorateUpdated(String text, boolean updated) {
        SpannableStringBuilder sb = new SpannableStringBuilder(text);

        if (updated) {
            sb.append("*");
            sb.setSpan(new AsteriskSpan(getContext()), sb.length() - 1, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;


        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }


        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }


        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}