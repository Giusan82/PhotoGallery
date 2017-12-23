package com.example.android.photogallery.utilities;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.photogallery.R;

//This class calls all views added in the list.xml
public class ItemsViewHolder extends RecyclerView.ViewHolder {
    public TextView tv_title;
    public TextView tv_authors;
    public ImageView iv_list_image;
    public TextView tv_values;
    public TextView itemPopupMenu;

    //Costructor used for calling the views
    public ItemsViewHolder(View view) {
        super(view);
        this.tv_title = (TextView) view.findViewById(R.id.tv_title);
        this.tv_authors = (TextView) view.findViewById(R.id.tv_author);
        this.iv_list_image = (ImageView) view.findViewById(R.id.iv_list_image);
        this.tv_values = (TextView) view.findViewById(R.id.tv_values);
        itemPopupMenu = (TextView) itemView.findViewById(R.id.item_popupmenu);
    }
}
