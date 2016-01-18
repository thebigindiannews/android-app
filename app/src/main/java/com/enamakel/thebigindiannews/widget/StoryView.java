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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.ItemManager;

public class StoryView extends RelativeLayout implements Checkable {
    private static final int VOTE_DELAY_MILLIS = 500;
    private final int mBackgroundColor;
    private final int mHighlightColor;
    private final int mTertiaryTextColorResId;
    private final int mSecondaryTextColorResId;
    private final int mPromotedColorResId;
    private final TextView mRankTextView;
    private final TextView mScoreTextView;
    private final View mBookmarked;
    private final TextView mPostedTextView;
    private final TextView mTitleTextView;
    private final TextView mSourceTextView;
    private final View mCommentButton;
    private final boolean mIsLocal;
    private final ViewSwitcher mVoteSwitcher;
    private final View mMoreButton;
    private boolean mChecked;


    public StoryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public StoryView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StoryView);
        mIsLocal = ta.getBoolean(R.styleable.StoryView_local, false);
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.textColorTertiary,
                android.R.attr.textColorSecondary,
                R.attr.colorCardBackground,
                R.attr.colorCardHighlight
        });
        mTertiaryTextColorResId = ContextCompat.getColor(context, a.getResourceId(0, 0));
        mSecondaryTextColorResId = ContextCompat.getColor(context, a.getResourceId(1, 0));
        mBackgroundColor = ContextCompat.getColor(context, a.getResourceId(2, 0));
        mHighlightColor = ContextCompat.getColor(context, a.getResourceId(3, 0));
        mPromotedColorResId = ContextCompat.getColor(context, R.color.greenA700);
        inflate(context, mIsLocal ? R.layout.local_story_view : R.layout.story_view, this);
        setBackgroundColor(mBackgroundColor);
        mVoteSwitcher = (ViewSwitcher) findViewById(R.id.vote_switcher);
        mRankTextView = (TextView) findViewById(R.id.rank);
        mScoreTextView = (TextView) findViewById(R.id.score);
        mBookmarked = findViewById(R.id.bookmarked);
        mPostedTextView = (TextView) findViewById(R.id.posted);
        mTitleTextView = (TextView) findViewById(R.id.title);
        mSourceTextView = (TextView) findViewById(R.id.source);
        mCommentButton = findViewById(R.id.comment);
        mMoreButton = findViewById(R.id.button_more);
        ta.recycle();
        a.recycle();
    }


    @Override
    public void setChecked(boolean checked) {
        if (mChecked == checked) {
            return;
        }
        mChecked = checked;
        setBackgroundColor(mChecked ? mHighlightColor : mBackgroundColor);
    }


    @Override
    public boolean isChecked() {
        return mChecked;
    }


    @Override
    public void toggle() {
        setChecked(!mChecked);
    }


    public void setStory(@NonNull ItemManager.WebItem story) {
        if (!mIsLocal && story instanceof ItemManager.Item) {
            ItemManager.Item item = (ItemManager.Item) story;
            if (item.isVoted()) {
                item.clearVoted();
                animateVote(item.getScore());
            } else {
                mRankTextView.setText(String.valueOf(item.getRank()));
                mScoreTextView.setText(getContext().getResources()
                        .getQuantityString(R.plurals.score, item.getScore(), item.getScore()));
            }
            if (item.getKidCount() > 0) {
                ((Button) mCommentButton).setText(getContext().getResources()
                        .getQuantityString(R.plurals.comments_count, item.getKidCount(), item.getKidCount()));
                mCommentButton.setVisibility(View.VISIBLE);
            } else {
                mCommentButton.setVisibility(View.GONE);
            }
        }
        mTitleTextView.setText(getContext().getString(R.string.loading_text));
        mTitleTextView.setText(story.getDisplayedTitle());
        mPostedTextView.setText(story.getDisplayedTime(getContext(), true, false));
        switch (story.getType()) {
            case ItemManager.Item.JOB_TYPE:
                mSourceTextView.setText(null);
                mSourceTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_work_white_18dp, 0, 0, 0);
                break;
            case ItemManager.Item.POLL_TYPE:
                mSourceTextView.setText(null);
                mSourceTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_poll_white_18dp, 0, 0, 0);
                break;
            default:
                mSourceTextView.setText(story.getSource());
                mSourceTextView.setCompoundDrawables(null, null, null, null);
                break;
        }
    }


    public void reset() {
        if (!mIsLocal) {
            mRankTextView.setText(R.string.loading_text);
            mScoreTextView.setText(R.string.loading_text);
            mBookmarked.setVisibility(INVISIBLE);
        }

        mTitleTextView.setText(getContext().getString(R.string.loading_text));
        mPostedTextView.setText(R.string.loading_text);
        mSourceTextView.setText(R.string.loading_text);
        mSourceTextView.setCompoundDrawables(null, null, null, null);
        mCommentButton.setVisibility(View.GONE);
    }


    public void setViewed(boolean isViewed) {
        if (mIsLocal) return; // local always means viewed, do not decorate
        mTitleTextView.setTextColor(isViewed ? mSecondaryTextColorResId : mTertiaryTextColorResId);
    }


    public void setPromoted(boolean isPromoted) {
        if (mIsLocal) return; // local items do not change rank
        mRankTextView.setTextColor(isPromoted ? mPromotedColorResId : mTertiaryTextColorResId);
    }


    public void setFavorite(boolean isFavorite) {
        if (mIsLocal) return; // local item must be favorite, do not decorate
        mBookmarked.setVisibility(isFavorite ? View.VISIBLE : View.INVISIBLE);
    }


    public void setOnCommentClickListener(View.OnClickListener listener) {
        mCommentButton.setOnClickListener(listener);
    }


    public void setUpdated(@NonNull ItemManager.Item story, boolean updated, boolean promoted) {
        if (mIsLocal) return; // local items do not change

        mRankTextView.setText(
                decorateUpdated(String.valueOf(story.getRank()), updated));

        setPromoted(promoted);
        if (story.getKidCount() > 0) {
            ((Button) mCommentButton).setText(decorateUpdated(getContext().getResources()
                            .getQuantityString(R.plurals.comments_count, story.getKidCount(), story.getKidCount()),
                    story.hasNewKids()));
        }
    }


    private void animateVote(final int newScore) {
        if (mIsLocal) return; // local items do not animate

        mVoteSwitcher.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // no op
            }


            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVoteSwitcher.showNext();
                    }
                }, VOTE_DELAY_MILLIS);
                mScoreTextView.setText(getContext().getResources()
                        .getQuantityString(R.plurals.score, newScore, newScore));
                mVoteSwitcher.getInAnimation().setAnimationListener(null);
            }


            @Override
            public void onAnimationRepeat(Animation animation) {
                // no op
            }
        });
        mVoteSwitcher.showNext();
    }


    public View getMoreOptions() {
        return mMoreButton;
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
}