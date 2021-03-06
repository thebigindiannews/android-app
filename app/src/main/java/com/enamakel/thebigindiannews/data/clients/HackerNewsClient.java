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

package com.enamakel.thebigindiannews.data.clients;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.StrikethroughSpan;

import com.enamakel.thebigindiannews.AppUtils;
import com.enamakel.thebigindiannews.BuildConfig;
import com.enamakel.thebigindiannews.R;
import com.enamakel.thebigindiannews.data.providers.managers.FavoriteManager;
import com.enamakel.thebigindiannews.data.providers.managers.ItemManager;
import com.enamakel.thebigindiannews.data.ResponseListener;
import com.enamakel.thebigindiannews.data.RestServiceFactory;
import com.enamakel.thebigindiannews.data.providers.managers.SessionManager;
import com.enamakel.thebigindiannews.data.providers.managers.UserManager;
import com.enamakel.thebigindiannews.data.models.StoryModel;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;


/**
 * Client to retrieve Hacker News content asynchronously
 */
public class HackerNewsClient implements ItemManager, UserManager {
    public static final String BASE_WEB_URL = "https://news.ycombinator.com";
    public static final String WEB_ITEM_PATH = BASE_WEB_URL + "/item?id=%s";
    static final String BASE_API_URL = "https://hacker-news.firebaseio.com/v0/";

    final RestService restService;
    final SessionManager sessionManager;
    final FavoriteManager favoriteManager;
    final ContentResolver contentResolver;


    @Inject
    public HackerNewsClient(Context context, RestServiceFactory factory,
                            SessionManager sessionManager,
                            FavoriteManager favoriteManager) {
        restService = factory.create(BASE_API_URL, RestService.class);
        this.sessionManager = sessionManager;
        this.favoriteManager = favoriteManager;
        contentResolver = context.getApplicationContext().getContentResolver();
    }


    @Override
    public void getStories(String filter, ResponseListener<List<StoryModel>> listener) {

    }

//    public void getStories(@FetchMode String filter, final ResponseListener<Item[]> listener) {
//        if (listener == null) {
//            return;
//        }
//        Call<int[]> call;
//        switch (filter) {
//            case NEW_FETCH_MODE:
//                call = restService.newStories();
//                break;
//            case SHOW_FETCH_MODE:
//                call = restService.showStories();
//                break;
//            case ASK_FETCH_MODE:
//                call = restService.askStories();
//                break;
//            case JOBS_FETCH_MODE:
//                call = restService.jobStories();
//                break;
//            default:
//                call = restService.topStories();
//                break;
//        }
//        call.enqueue(new Callback<int[]>() {
//            @Override
//            public void onResponse(Response<int[]> response, Retrofit retrofit) {
//                listener.onResponse(toItems(response.body()));
//            }
//
//
//            @Override
//            public void onFailure(Throwable t) {
//                listener.onError(t != null ? t.getMessage() : "");
//
//            }
//        });
//    }


//    @Override
//    public void getItem(String itemId, final ResponseListener<Item> listener) {
//        if (listener == null) return;
//
//        ItemCallbackWrapper wrapper = new ItemCallbackWrapper(listener);
//        sessionManager.isViewed(contentResolver, itemId, wrapper);
//        favoriteManager.check(contentResolver, itemId, wrapper);
//        restService.item(itemId).enqueue(wrapper);
//    }


    @Override
    public void getItem(String itemId, ResponseListener<StoryModel> listener) {
//        hackerNewsClient.getItem(itemId, listener);
    }


    @Override
    public void getUser(String username, final ResponseListener<User> listener) {
        if (listener == null) {
            return;
        }
        restService.user(username)
                .enqueue(new Callback<UserItem>() {
                    @Override
                    public void onResponse(Response<UserItem> response, Retrofit retrofit) {
                        UserItem user = response.body();
                        if (user == null) {
                            listener.onResponse(null);
                            return;
                        }
                        user.submittedItems = toItems(user.submitted);
                        listener.onResponse(user);
                    }


                    @Override
                    public void onFailure(Throwable t) {
                        listener.onError(t != null ? t.getMessage() : "");
                    }
                });
    }


    @NonNull
    HackerNewsItem[] toItems(int[] ids) {
        HackerNewsItem[] items = new HackerNewsItem[ids == null ? 0 : ids.length];
        for (int i = 0; i < items.length; i++) {
            HackerNewsItem item = new HackerNewsItem(ids[i]);
            item.rank = i + 1;
            items[i] = item;
        }
        return items;
    }


    interface RestService {
        @Headers("Cache-Control: max-age=600")
        @GET("topstories.json")
        Call<int[]> topStories();


        @Headers("Cache-Control: max-age=600")
        @GET("newstories.json")
        Call<int[]> newStories();


        @Headers("Cache-Control: max-age=600")
        @GET("showstories.json")
        Call<int[]> showStories();


        @Headers("Cache-Control: max-age=600")
        @GET("askstories.json")
        Call<int[]> askStories();


        @Headers("Cache-Control: max-age=600")
        @GET("jobstories.json")
        Call<int[]> jobStories();


        @Headers("Cache-Control: max-age=300")
        @GET("item/{itemId}.json")
        Call<HackerNewsItem> item(@Path("itemId") String itemId);


        @Headers("Cache-Control: max-age=300")
        @GET("user/{userId}.json")
        Call<UserItem> user(@Path("userId") String userId);
    }


    static class HackerNewsItem implements Item {
        static final String FORMAT_LINK_USER = "<a href=\"%1$s://user/%2$s\">%2$s</a>";

        // The item's unique id. Required.
        @Getter long id;
        // true if the item is deleted.
        @Getter boolean deleted;
        // The type of item. One of "job", "story", "comment", "poll", or "pollopt".
        @Getter String type;
        // The username of the item's author.
        @Getter String by;
        // Creation date of the item, in Unix Time.
        @Getter long time;
        // The comment, Ask HN, or poll text. HTML.
        @Getter String text;
        // true if the item is dead.
        @Getter boolean dead;
        // The item's parent. For comments, either another comment or the relevant story. For
        // pollopts, the relevant poll.
        @Getter long parent;
        // The ids of the item's comments, in ranked display order.
        @Getter @Setter long[] kids;
        // The URL of the story.
        @Getter String url;
        // The story's score, or the votes for a pollopt.
        @Getter int score;
        // The title of the story or poll.
        @Getter String title;
        // A list of related pollopts, in display order.
        @Getter long[] parts;
        // In the case of stories or polls, the total comment count.
        @Getter int descendants = -1;

        // view state
        HackerNewsItem[] kidItems;
        @Getter @Setter boolean favorite;
        @Getter @Setter boolean viewed;
        @Getter @Setter int localRevision = -1;
        int level = 0;
        boolean collapsed;
        boolean contentExpanded;
        int rank;
        @Setter @Getter int lastKidCount = -1;
        boolean hasNewDescendants = false;
        HackerNewsItem parentItem;
        boolean voted;

        public static final Creator<HackerNewsItem> CREATOR = new Creator<HackerNewsItem>() {
            @Override
            public HackerNewsItem createFromParcel(Parcel source) {
                return new HackerNewsItem(source);
            }


            @Override
            public HackerNewsItem[] newArray(int size) {
                return new HackerNewsItem[size];
            }
        };


        HackerNewsItem(long id) {
            this.id = id;
        }


        HackerNewsItem(long id, int level) {
            this(id);
            this.level = level;
        }


        HackerNewsItem(Parcel source) {
            id = source.readLong();
            title = source.readString();
            time = source.readLong();
            by = source.readString();
            kids = source.createLongArray();
            url = source.readString();
            text = source.readString();
            type = source.readString();
            favorite = source.readInt() != 0;
            descendants = source.readInt();
            score = source.readInt();
            kidItems = source.createTypedArray(HackerNewsItem.CREATOR);
            favorite = source.readInt() == 1;
            viewed = source.readInt() == 1;
            localRevision = source.readInt();
            level = source.readInt();
            dead = source.readInt() == 1;
            deleted = source.readInt() == 1;
            collapsed = source.readInt() == 1;
            contentExpanded = source.readInt() == 1;
            rank = source.readInt();
            lastKidCount = source.readInt();
            hasNewDescendants = source.readInt() == 1;
            parent = source.readLong();
            parentItem = source.readParcelable(HackerNewsItem.class.getClassLoader());
            voted = source.readInt() == 1;
        }


        @Override
        public void populate(Item info) {
            title = info.getTitle();
            time = info.getTime();
            by = info.getBy();
            kids = info.getKids();
            url = info.getRawUrl();
            text = info.getText();
            type = info.getRawType();
            descendants = info.getDescendants();
            hasNewDescendants = lastKidCount >= 0 && descendants > lastKidCount;
            lastKidCount = descendants;
            parent = Long.parseLong(info.getParent());
            deleted = info.isDeleted();
            dead = info.isDead();
            score = info.getScore();
            viewed = info.isViewed();
            favorite = info.isFavorite();
        }


        @Override
        public String getRawType() {
            return type;
        }


        @Override
        public String getRawUrl() {
            return url;
        }


        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(title);
            dest.writeLong(time);
            dest.writeString(by);
            dest.writeLongArray(kids);
            dest.writeString(url);
            dest.writeString(text);
            dest.writeString(type);
            dest.writeInt(favorite ? 1 : 0);
            dest.writeInt(descendants);
            dest.writeInt(score);
            dest.writeTypedArray(kidItems, 0);
            dest.writeInt(favorite ? 1 : 0);
            dest.writeInt(viewed ? 1 : 0);
            dest.writeInt(localRevision);
            dest.writeInt(level);
            dest.writeInt(dead ? 1 : 0);
            dest.writeInt(deleted ? 1 : 0);
            dest.writeInt(collapsed ? 1 : 0);
            dest.writeInt(contentExpanded ? 1 : 0);
            dest.writeInt(rank);
            dest.writeInt(lastKidCount);
            dest.writeInt(hasNewDescendants ? 1 : 0);
            dest.writeLong(parent);
            dest.writeParcelable(parentItem, flags);
            dest.writeInt(voted ? 1 : 0);
        }


        @Override
        public String getId() {
            return String.valueOf(id);
        }


        @Override
        public long getLongId() {
            return id;
        }


        @Override
        public String getDisplayedTitle() {
            switch (getType()) {
                case COMMENT_TYPE:
                    return text;
                case JOB_TYPE:
                case STORY_TYPE:
                case POLL_TYPE: // TODO poll need to display options
                default:
                    return title;
            }
        }


        @NonNull
        @Override
        public String getType() {
            return !TextUtils.isEmpty(type) ? type : STORY_TYPE;
        }


        @Override
        public Spannable getDisplayedTime(Context context, boolean abbreviate, boolean authorLink) {
            CharSequence relativeTime = "";
            if (abbreviate) {
                relativeTime = AppUtils.getAbbreviatedTimeSpan(time * 1000);
            } else {
                try {
                    relativeTime = DateUtils.getRelativeTimeSpanString(time * 1000,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL);
                } catch (NullPointerException e) {
                    // TODO should properly prevent this
                }
            }
            if (deleted) {
                Spannable spannable = new SpannableString(relativeTime);
                spannable.setSpan(new StrikethroughSpan(), 0, relativeTime.length(),
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                return spannable;
            }
            SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
            if (dead) {
                spannableBuilder.append(context.getString(R.string.dead_prefix)).append(" ");
            }
            spannableBuilder.append(relativeTime);
            if (!TextUtils.isEmpty(by)) {
                spannableBuilder.append(" - ")
                        .append(authorLink ? Html.fromHtml(String.format(FORMAT_LINK_USER,
                                BuildConfig.APPLICATION_ID, by)) : by);
            }
            return spannableBuilder;
        }


        @Override
        public int getKidCount() {
            if (descendants > 0) return descendants;
            return kids != null ? kids.length : 0;
        }


        @Override
        public boolean hasNewKids() {
            return hasNewDescendants;
        }


        @Override
        public String getUrl() {
            switch (getType()) {
                case JOB_TYPE:
                case POLL_TYPE:
                case COMMENT_TYPE:
                    return getItemUrl(getId());
                default:
                    return TextUtils.isEmpty(url) ? getItemUrl(getId()) : url;
            }
        }


        String getItemUrl(String itemId) {
            return String.format(WEB_ITEM_PATH, itemId);
        }


        @Override
        public String getSource() {
            return TextUtils.isEmpty(getUrl()) ? null : Uri.parse(getUrl()).getHost();
        }


        @Override
        public HackerNewsItem[] getKidItems() {
            if (kids == null || kids.length == 0) {
                return new HackerNewsItem[0];
            }

            if (kidItems == null) {
                kidItems = new HackerNewsItem[kids.length];
                for (int i = 0; i < kids.length; i++) {
                    HackerNewsItem item = new HackerNewsItem(kids[i], level + 1);
                    item.rank = i + 1;
                    kidItems[i] = item;
                }
            }

            return kidItems;
        }


        @Override
        public boolean isStoryType() {
            switch (getType()) {
                case STORY_TYPE:
                case POLL_TYPE:
                case JOB_TYPE:
                    return true;
                case COMMENT_TYPE:
                default:
                    return false;
            }
        }


        @Override
        public int getDescendants() {
            return descendants;
        }


        @Override
        public boolean isViewed() {
            return viewed;
        }


        @Override
        public void setIsViewed(boolean isViewed) {
            viewed = isViewed;
        }


        @Override
        public int getLevel() {
            return level;
        }


        @Override
        public String getParent() {
            return String.valueOf(parent);
        }


        @Override
        public Item getParentItem() {
            if (parent == 0) return null;
            if (parentItem == null) parentItem = new HackerNewsItem(parent);
            return parentItem;
        }


        @Override
        public boolean isDeleted() {
            return deleted;
        }


        @Override
        public boolean isDead() {
            return dead;
        }


        @Override
        public int getScore() {
            return score;
        }


        @Override
        public void incrementScore() {
            score++;
            voted = true;
        }


        @Override
        public boolean isVoted() {
            return voted;
        }


        @Override
        public void clearVoted() {
            voted = false;
        }


        @Override
        public boolean isCollapsed() {
            return collapsed;
        }


        @Override
        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
        }


        @Override
        public int getRank() {
            return rank;
        }


        @Override
        public boolean isContentExpanded() {
            return contentExpanded;
        }


        @Override
        public void setContentExpanded(boolean expanded) {
            contentExpanded = expanded;
        }


        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof HackerNewsItem && id == ((HackerNewsItem) o).id;
        }
    }


    static class UserItem implements User {
        public static final Creator<UserItem> CREATOR = new Creator<UserItem>() {
            @Override
            public UserItem createFromParcel(Parcel source) {
                return new UserItem(source);
            }


            @Override
            public UserItem[] newArray(int size) {
                return new UserItem[size];
            }
        };
        String id;
        long delay;
        long created;
        long karma;
        String about;
        int[] submitted;

        // view state
        HackerNewsItem[] submittedItems = new HackerNewsItem[0];


        UserItem(Parcel source) {
            id = source.readString();
            delay = source.readLong();
            created = source.readLong();
            karma = source.readLong();
            about = source.readString();
            submitted = source.createIntArray();
            submittedItems = source.createTypedArray(HackerNewsItem.CREATOR);
        }


        @Override
        public String getId() {
            return id;
        }


        @Override
        public String getAbout() {
            return about;
        }


        @Override
        public long getKarma() {
            return karma;
        }


        @Override
        public String getCreated(Context context) {
            return DateUtils.formatDateTime(context, created * 1000, DateUtils.FORMAT_SHOW_DATE);
        }


        @NonNull
        @Override
        public Item[] getItems() {
            return submittedItems;
        }


        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeLong(delay);
            dest.writeLong(created);
            dest.writeLong(karma);
            dest.writeString(about);
            dest.writeIntArray(submitted);
            dest.writeTypedArray(submittedItems, flags);
        }
    }


    static class ItemCallbackWrapper implements SessionManager.OperationCallbacks,
            FavoriteManager.OperationCallbacks, Callback<HackerNewsItem> {
        final ResponseListener<Item> responseListener;
        Boolean isViewed;
        Boolean isFavorite;
        Item item;
        String errorMessage;
        boolean hasError;
        boolean hasResponse;


        ItemCallbackWrapper(@NonNull ResponseListener<Item> responseListener) {
            this.responseListener = responseListener;
        }


        @Override
        public void onCheckViewedComplete(boolean isViewed) {
            this.isViewed = isViewed;
            done();
        }


        @Override
        public void onCheckFavoriteComplete(boolean isFavorite) {
            this.isFavorite = isFavorite;
            done();
        }


        @Override
        public void onResponse(Response<HackerNewsItem> response, Retrofit retrofit) {
            this.item = response.body();
            this.hasResponse = true;
            done();
        }


        @Override
        public void onFailure(Throwable t) {
            this.errorMessage = t != null ? t.getMessage() : "";
            this.hasError = true;
            done();
        }


        void done() {
            if (isViewed == null) return;
            if (isFavorite == null) return;
            if (!(hasResponse || hasError)) return;

            if (hasResponse) {
                if (item != null) {
                    item.setFavorite(isFavorite);
                    item.setIsViewed(isViewed);
                }
                responseListener.onResponse(item);
            } else responseListener.onError(errorMessage);
        }
    }
}