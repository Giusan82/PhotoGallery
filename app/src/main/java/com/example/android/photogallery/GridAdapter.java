package com.example.android.photogallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.android.photogallery.utilities.ItemsList;
import com.example.android.photogallery.utilities.ItemsViewHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GridAdapter extends RecyclerView.Adapter<ItemsViewHolder> {
    public static final String LOG_TAG = GridAdapter.class.getName();
    private ArrayList<ItemsList> mItemList;
    private Context mContext;

    /**
     * Constructor
     */
    public GridAdapter(Activity context, ArrayList<ItemsList> items) {
        mContext = context;
        mItemList = items;
    }

    /**
     * Cache of the view objects for a list items.
     */
    //this creates a viewHolder objects using list.xml layout
    @Override
    public ItemsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int listId = R.layout.list;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(listId, viewGroup, false);
        ItemsViewHolder viewHolder = new ItemsViewHolder(view);
        return viewHolder;
    }

    //here the recycler view is populated with data provided from the class ItemsList
    @Override
    public void onBindViewHolder(final ItemsViewHolder holder, final int position) {
        final ItemsList currentItem = mItemList.get(position);
        if (currentItem.hasTitle()) {
            holder.tv_title.setText(currentItem.getTitle());
        } else {
            holder.tv_title.setVisibility(View.GONE);
        }
        holder.tv_authors.setText(currentItem.getAuthor());
        holder.tv_values.setText(mContext.getString(R.string.tv_values, formatDate(currentItem.getPublishedDate()), currentItem.getLikes(), currentItem.getDownloads(), currentItem.getNumberOfViews()));
        //Load images in cache from remote url
        Glide.with(mContext).load(currentItem.getImage_small()).crossFade().dontTransform().into(holder.iv_list_image);
        //here the views are not recycled. It avoids to see the previous image on the next view during the loading.
        holder.setIsRecyclable(false);
        //this implements a popupmenu for each items
        holder.itemPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu(currentItem, holder);
            }
        });
    }

    //get the total items count
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    //determine if connection is active
    private Boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void popupMenu(final ItemsList currentItem, final ItemsViewHolder holder) {
        final String image = currentItem.getImage_raw();
        //creating a popup menu
        PopupMenu popup = new PopupMenu(mContext, holder.itemPopupMenu);
        //inflating menu from xml resource
        popup.inflate(R.menu.overflow_menu);
        popup.setGravity(Gravity.END);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.photo_zoom:
                        //this open tha FullScreenActivity
                        if (image != null) {
                            Intent intent = new Intent(mContext, FullScreenActivity.class);
                            intent.putExtra(Intent.EXTRA_TEXT, image);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                                mContext.startActivity(intent);
                            }
                        } else { //if the items have no link, a toast message is displayed
                            Toast.makeText(mContext, mContext.getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.photo_download:
                        if (isConnected()) {
                            if (image != null) {
                                Toast.makeText(mContext, mContext.getString(R.string.saving_image), Toast.LENGTH_LONG).show();
                                Glide.with(mContext).load(image).asBitmap().into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                        saveImage(resource, currentItem.getID());
                                    }
                                });
                            } else {//if the items have no link, a toast message is displayed
                                Toast.makeText(mContext, mContext.getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.no_internet_title), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.like:
                        Toast.makeText(mContext, mContext.getString(R.string.out_of_oder), Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        // Force icons on popup menu
        Object menuObject;
        try {
            Field mField = PopupMenu.class.getDeclaredField("mPopup");
            mField.setAccessible(true);
            menuObject = mField.get(popup);
            menuObject.getClass().getDeclaredMethod("setForceShowIcon", new Class[] { boolean.class }).invoke(menuObject, true);
        }catch (Exception e){
            // These exceptions should never happen, but in the case it log the error and show the menu normally.
            Log.w(LOG_TAG + " -> PopupMenu", "Error showing menu icons", e);
            popup.show();
            return;
        }
        //displaying the popup
        popup.show();
    }

    //for getting the date conversion
    public String formatDate(String date) {
        String newFormatData = "";
        if (date.length() >= 10) {
            // Splits the string after 10 char, because the date obtained from server is like this "2017-07-15T21:30:35Z", so this method will give 2017-07-15
            CharSequence splittedDate = date.subSequence(0, 10);
            try {
                Date formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(splittedDate.toString());
                newFormatData = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(formatDate);
            } catch (ParseException e) {
                Log.e(LOG_TAG + " -> formatDate", e.getMessage());
            }
        } else {
            newFormatData = date;
        }
        return newFormatData;
    }

    //this save image into device
    private String saveImage(Bitmap image, String id) {
        String savedPath = null;
        //this set the file name using the id
        String imageName = id + ".jpg";
        //this set the destination folder
        File mStorageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/PhotoGallery");
        boolean existsDir = true;
        //check if folder exist, if not it will be created
        if (!mStorageFolder.exists()) {
            existsDir = mStorageFolder.mkdirs();
        }
        //if existsDir is true
        if (existsDir) {
            //the file image is saved to "mStorageFolder" using "fileName" as image name
            File mImage = new File(mStorageFolder, imageName);
            savedPath = mImage.getAbsolutePath();
            Toast.makeText(mContext, mContext.getString(R.string.saving_image), Toast.LENGTH_LONG).show();
            try {
                OutputStream stream = new FileOutputStream(mImage);
                //the file image is compressed to jpeg
                image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                stream.close();
            } catch (Exception e) {
                Log.e(LOG_TAG + " -> OutputStream", e.getMessage());
            }
            // Add the image to the system gallery
            galleryAddPic(savedPath);
            //when the saving is completed, show a toast message
            Toast.makeText(mContext, mContext.getString(R.string.image_saved), Toast.LENGTH_SHORT).show();
        }
        return savedPath;
    }

    //this pass the image path to the system images gallery that provides of saving it
    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }
}
