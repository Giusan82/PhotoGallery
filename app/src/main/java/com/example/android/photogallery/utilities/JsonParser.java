package com.example.android.photogallery.utilities;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.photogallery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JsonParser {
    private static final String LOG_TAG = JsonParser.class.getSimpleName();

    /**
     * Return a list of {@link ItemsList} objects that has been built up from parsing a JSON response.
     */
    public static List<ItemsList> parsingData(Context context, String jsonResponse) {
        ArrayList<ItemsList> list = new ArrayList<>();

        // Try to parse the JSON Response.
        try {
            //This creates the root JSONObject by calling jsonResponse
            JSONArray base = new JSONArray(jsonResponse);
            String id = "";
            String title = "";
            String name = "";
            String image_small = "";
            String image_raw = "";
            int views = 0;
            int downloads = 0;
            int likes = 0;
            String date = "";
            for (int i = 0; i < base.length(); i++) {
                //this gets the items ID
                if (base.getJSONObject(i).has("id")) {
                    id = base.getJSONObject(i).getString("id");
                }
                //this gets a description, used a title in the list
                if (base.getJSONObject(i).has("description")) {
                    title = base.getJSONObject(i).getString("description");
                }
                //this gets the user name owner of picture
                if (base.getJSONObject(i).has("user")) {
                    JSONObject user = base.getJSONObject(i).getJSONObject("user");
                    name = user.getString("name");
                }
                //this gets the picture url at 400p and 200p
                if (base.getJSONObject(i).has("urls")) {
                    image_small = base.getJSONObject(i).getJSONObject("urls").getString("small");
                    image_raw = base.getJSONObject(i).getJSONObject("urls").getString("raw");
                }
                //this gets the number of views
                if (base.getJSONObject(i).has("views")) {
                    views = base.getJSONObject(i).getInt("views");
                }
                //this gets the number of downloads
                if (base.getJSONObject(i).has("downloads")) {
                    downloads = base.getJSONObject(i).getInt("downloads");
                }
                //this gets the number of likes
                if (base.getJSONObject(i).has("likes")) {
                    likes = base.getJSONObject(i).getInt("likes");
                }
                //this gets when the pictures were created
                if (base.getJSONObject(i).has("created_at")) {
                    date = base.getJSONObject(i).getString("created_at");
                }
                ItemsList items = new ItemsList(id, title, name, image_small, image_raw, views, downloads, likes, date);
                list.add(items);
                //this sorts the list
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

                String orderBy = sharedPrefs.getString(
                        context.getString(R.string.settings_order_by_key),
                        context.getString(R.string.settings_order_by_default)
                );
                switch (orderBy) {
                    case "popular":
                        //this compare data obtained from parsing json and sort it by number of likes
                        Collections.sort(list, new Comparator<ItemsList>() {
                            @Override
                            public int compare(ItemsList arg1, ItemsList arg2) {
                                int A = arg1.getLikes();
                                int B = arg2.getLikes();
                                if (A < B) {
                                    return 1;
                                } else if (B < A) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        break;
                    //this compare data obtained from parsing json and sort it by number of views
                    case "most_viewed":
                        Collections.sort(list, new Comparator<ItemsList>() {
                            @Override
                            public int compare(ItemsList arg1, ItemsList arg2) {
                                int A = arg1.getNumberOfViews();
                                int B = arg2.getNumberOfViews();
                                if (A < B) {
                                    return 1;
                                } else if (B < A) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        break;
                    case "most_downloaded":
                        //this compare data obtained from parsing json and sort it by number of downloads
                        Collections.sort(list, new Comparator<ItemsList>() {
                            @Override
                            public int compare(ItemsList arg1, ItemsList arg2) {
                                int A = arg1.getDownloads();
                                int B = arg2.getDownloads();
                                if (A < B) {
                                    return 1;
                                } else if (B < A) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        break;
                    case "oldest":
                        //this compare data obtained from parsing json and sort it by published date in ascending order
                        Collections.sort(list, new Comparator<ItemsList>() {
                            @Override
                            public int compare(ItemsList arg1, ItemsList arg2) {
                                long A = formatDate(arg1.getPublishedDate());
                                long B = formatDate(arg2.getPublishedDate());
                                if (A > B) {
                                    return 1;
                                } else if (B > A) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        break;
                    //this compare data obtained from parsing json and sort it by published date in descending order
                    case "latest":
                        Collections.sort(list, new Comparator<ItemsList>() {
                            @Override
                            public int compare(ItemsList arg1, ItemsList arg2) {
                                long A = formatDate(arg1.getPublishedDate());
                                long B = formatDate(arg2.getPublishedDate());
                                if (A < B) {
                                    return 1;
                                } else if (B < A) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        break;
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG + " -> JSONException", "Problem with parsing JSON");
        }
        return list;
    }

    //for getting the date conversion in milliseconds
    public static long formatDate(String date) {
        long milliseconds = 0;
        if (date.length() >= 10) {
            // Splits the string after 10 char, because the date obtained from server is like this "2017-07-15T21:30:35Z", so this method will give 2017-07-15
            CharSequence splittedDate = date.subSequence(0, 10);
            try {
                Date formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(splittedDate.toString());
                milliseconds = formatDate.getTime();
            } catch (ParseException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return milliseconds;
    }
}
