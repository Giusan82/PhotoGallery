package com.example.android.photogallery;

import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.photogallery.utilities.DataLoader;
import com.example.android.photogallery.utilities.ItemsList;
import com.google.android.gms.security.ProviderInstaller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<ItemsList>>, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int ITEMS_LOADER_ID = 0;
    private static final String SERVER_URL = "https://api.unsplash.com/photos/random/";
    private final static String QUERY_PARAM = "query";//this call a query
    private final static String COUNT_PARAM = "count"; //this set the max number of pictures
    private final static String ORIENTATION_PARAM = "orientation"; //this set the orientation of pictures
    private final static String CLIENT_ID = "client_id"; //this is the api key of unsplash.com

    private String query;
    private RecyclerView recyclerView;
    private GridAdapter adapter;
    private LoaderManager loaderManager;
    private ArrayList<ItemsList> mItems;
    private TextView mEmptyList; //this text is shown the the are no data
    private TextView mEmptyListMessage; //this text is shown the the are no data
    private ImageView mImageEmptyList; //this image is shown the the are no data
    private ProgressBar mLoader; //this loads a circle progress bar as loading bar
    public SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This add a logo in ActionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //Find the id for the respective views
        recyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mEmptyList = (TextView) findViewById(R.id.tv_empty_list);
        mEmptyListMessage = (TextView) findViewById(R.id.tv_empty_list_message);
        mImageEmptyList = (ImageView) findViewById(R.id.iw_empty_list);
        mLoader = (ProgressBar) findViewById(R.id.loading_indicator);

        /**This code solves the error javax.net.ssl.SSLProtocolException: SSL handshake aborted
         * for android version <= 4.4 **/
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();
        if (isConnected()) {
            loaderManager.initLoader(ITEMS_LOADER_ID, null, this);
        } else {
            //display that there is no internet connection
            String title = getString(R.string.no_internet_title);
            String message = getString(R.string.no_internet);
            alertDialogMessage(title, message);
        }

        //Here is determined as the collection of items is displayed
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                this.getResources().getInteger(R.integer.spanCount), StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //the ArrayList is initialized
        mItems = new ArrayList<>();
        //Shows the items list using a custom adapter
        adapter = new GridAdapter(this, mItems);
        recyclerView.setAdapter(adapter);
        //Register MainActivity as an OnPreferenceChangedListener to receive a callback when a SharedPreference has changed.
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * here the Loader are managed by LoaderManager to perform the network request on a background thread
     **/
    @Override
    public Loader<List<ItemsList>> onCreateLoader(int i, Bundle bundle) {
        //when a new Loader is created
        mEmptyList.setVisibility(View.GONE);
        mEmptyListMessage.setVisibility(View.GONE);
        mImageEmptyList.setVisibility(View.GONE);
        mLoader.setVisibility(View.VISIBLE);
        return new DataLoader(this, builderUrl(query).toString());
    }

    @Override
    public void onLoadFinished(Loader<List<ItemsList>> loader, List<ItemsList> data) {
        //when load is finished, here the application’s UI is update with loaded data
        if (isConnected()) {
            clear();
            mLoader.setVisibility(View.GONE);
            //check if the list is empty
            if (data != null && !data.isEmpty()) {
                //if not, add all items into the ArrayList
                mItems.addAll(data);
                //and notify to adapter to update the data
                adapter.notifyDataSetChanged();
            } else {
                clear();
                // if yes, set empty state text to display "No results found."
                String message = getString(R.string.no_found);
                mEmptyList.setText(message);
                mEmptyList.setVisibility(View.VISIBLE);
                mEmptyListMessage.setVisibility(View.VISIBLE);
                mImageEmptyList.setVisibility(View.VISIBLE);
            }
        } else {
            mLoader.setVisibility(View.GONE);
            //display that there is no internet connection
            String title = getString(R.string.no_internet_title);
            String message = getString(R.string.no_internet);
            alertDialogMessage(title, message);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ItemsList>> loader) {
        //when load is reset, here the old data are removed
        clear();
    }

    public void clear() {
        this.mItems.clear();
        adapter.notifyDataSetChanged();
    }

    //this builds the url
    private URL builderUrl(String query) {
        if (query == null || query.isEmpty()) {
            query = "";
        }
        //here get the preferences
        String maxResults = sharedPrefs.getString(getString(R.string.settings_max_results_key),
                getString(R.string.settings_max_results_default));
        String orientation = sharedPrefs.getString(getString(R.string.settings_orientation_key),
                getString(R.string.settings_orientation_default));

        Boolean featured = sharedPrefs.getBoolean(getString(R.string.settings_featured_key), getResources().getBoolean(R.bool.pref_show_featured));
        Uri builtUri = Uri.parse(SERVER_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, query)
                .appendQueryParameter(COUNT_PARAM, maxResults)
                .appendQueryParameter(ORIENTATION_PARAM, orientation)
                .appendQueryParameter(getString(R.string.settings_featured_key), featured.toString())
                .appendQueryParameter(CLIENT_ID, getString(R.string.api_key))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    //determine if connection is active
    private Boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    //this create an ActionsBar menu and add an searchView on ActionsBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.search_view);
        SearchView searchField;
        //this add the searchView to actionBar
        searchField = (SearchView) search.getActionView();
        //set the hint text on searchView
        searchField.setQueryHint(getString(R.string.searchHint));
        //this expand the searchView without click on it
        searchField.setIconified(false);
        searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                /**this active the instant search, but now it is deactivate
                 * because the free API Key added allows only 50 request per hour
                 * Just uncomment this to activate the instant search
                 */
                //search(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String input) {
                search(input);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(String input) {
        //determine if connection is active after the search button is clicked
        if (isConnected()) {
            //restart the loader with the new data
            if (!input.isEmpty()) {
                query = input;
                refresh();
            }
        } else {
            String title = getString(R.string.no_internet_title);
            String message = getString(R.string.no_internet);
            alertDialogMessage(title, message);
        }
    }

    //build an alert dialog message for no internet connection
    public void alertDialogMessage(String title, String message) {
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
        builder.setPositiveButton(getString(R.string.refresh_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                refresh();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //restart the loader
    private void refresh() {
        loaderManager.restartLoader(ITEMS_LOADER_ID, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //Refresh the data when preference are changed
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
