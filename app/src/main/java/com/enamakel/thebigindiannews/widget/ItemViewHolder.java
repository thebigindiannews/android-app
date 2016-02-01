package com.enamakel.thebigindiannews.widget;

import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.*;

import com.enamakel.thebigindiannews.R;

/**
 * Created by robert on 2/1/16.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {
    final android.widget.TextView postedTextView;
    final android.widget.TextView contentTextView;
    final android.widget.TextView readMoreTextView;
    final Button commentButton;
    final View moreButton;
    final View contentView;


    public ItemViewHolder(View itemView) {
        super(itemView);
        postedTextView = (android.widget.TextView) itemView.findViewById(R.id.posted);
        postedTextView.setMovementMethod(LinkMovementMethod.getInstance());
        contentTextView = (android.widget.TextView) itemView.findViewById(R.id.text);
        readMoreTextView = (android.widget.TextView) itemView.findViewById(R.id.more);
        commentButton = (Button) itemView.findViewById(R.id.comment);
        commentButton.setVisibility(View.GONE);
        moreButton = itemView.findViewById(R.id.button_more);
        contentView = itemView.findViewById(R.id.content);
    }
}
