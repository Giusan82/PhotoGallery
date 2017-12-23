package com.example.android.photogallery.utilities;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class DataLoader extends AsyncTaskLoader<List<ItemsList>>{

    private String mUrl; //Query Url
    private Context mContext;
    private List<ItemsList> mList;

    /**Costructor
     *
     * @param context of the activity
     * @param url to load data from server
     */
    public DataLoader(Context context, String url) {
        super(context);
        mContext = context;
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        if(mList != null){
            deliverResult(mList);
        }else{
            forceLoad();
        }
    }

    @Override
    public List<ItemsList> loadInBackground() {
        if(mUrl == null){
            Log.e("DataLoader", "Url is null");
            return null;
        }else{
            return ConnectionsUrl.fetchData(mContext, mUrl);
        }
    }

    @Override
    public void deliverResult(List<ItemsList> data) {
        mList = data;
        super.deliverResult(data);
    }
}
