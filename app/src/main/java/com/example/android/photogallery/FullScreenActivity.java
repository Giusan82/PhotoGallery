package com.example.android.photogallery;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class FullScreenActivity extends AppCompatActivity {

    private PhotoView mPhotoView;
    private ProgressBar mLoader; //this loads a circle progress bar as loading bar
    private String mImageUrl;
    private RelativeLayout mParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        getSupportActionBar().hide();

        //Show the back arrow
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mParent = (RelativeLayout) findViewById(R.id.fullscreen_parent);
        mLoader = (ProgressBar) findViewById(R.id.loading_indicator);
        mPhotoView = (PhotoView) findViewById(R.id.photo_view);
        //this show actionBar when is clicked on the image
        mPhotoView.setOnViewTapListener(mTapListener);
        //set the maximum zoom
        mPhotoView.setMaximumScale(10);
        //this show actionBar when image isn't loaded and is clicked on black screen
        mParent.setOnClickListener(mClickListener);
        if (isConnected()) {
            Intent intent = getIntent();
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                mImageUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
                glideLoad(mImageUrl);
            }
        } else {
            String title = getString(R.string.no_internet_title);
            String message = getString(R.string.no_internet);
            alertDialogMessage(title, message);
        }
    }

    //this set up an alert dialog message
    private void alertDialogMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alertDialog);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_wifi_off);
        builder.setMessage(message);
        builder.setNegativeButton(getString(R.string.close_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void glideLoad(String url) {
        Glide.with(this).load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mLoader.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mLoader.setVisibility(View.GONE);
                        return false;
                    }
                })
                .crossFade().dontTransform().into(mPhotoView);
    }

    private PhotoViewAttacher.OnViewTapListener mTapListener = new PhotoViewAttacher.OnViewTapListener() {
        @Override
        public void onViewTap(View view, float x, float y) {
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    getSupportActionBar().show();
                }

                public void onFinish() {
                    getSupportActionBar().hide();
                }
            }.start();
        }
    };
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    getSupportActionBar().show();
                }

                public void onFinish() {
                    getSupportActionBar().hide();
                }
            }.start();
        }
    };

    //Go back in the previous activity without reloading
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    //determine if connection is active
    private Boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
