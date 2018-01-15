package com.ilham1012.ecgbpi.helper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ilham1012.ecgbpi.R;

/**
 * Created by ilham on 11/02/2016.
 */
public class CustomViewHolder extends RecyclerView.ViewHolder {
    protected TextView title;
    protected TextView subtitle;

    public CustomViewHolder(View view){
        super(view);
        this.title = (TextView) view.findViewById(R.id.list_item_title);
        this.subtitle = (TextView) view.findViewById(R.id.list_item_subtitle);
    }
}
