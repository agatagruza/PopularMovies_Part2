package com.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;


import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE_DESCRIPTION;
import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE_ID;
import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE_RATING;
import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE_RELEASE_DATE;
import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE_TITLE;
import static com.udacity.popularmovies.FavoritesProvider.COLUMN_MOVIE__POSTER;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private GridLayout gridLayoutMain;
    private String[] IDList;
    private String[] pathList;
    private String[] titleList;
    private String[] descriptionList;
    private double[] ratingList;
    private String[] dateList;

    private String sortCriteria;
    private final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    private final String POSTER_SIZE = "w185/";
    private final String KEYWORD_ID = "id";
    private final String KEYWORD_RESULTS = "results";
    private final String KEYWORD_POSTER_PATH = "poster_path";
    private final String KEYWORD_TITLE = "title";
    private final String KEYWORD_OVERVIEW = "overview";
    private final String KEYWORD_VOTE_AVERAGE = "vote_average";
    private final String KEYWORD_RELEASE_DATE = "release_date";
    private Bundle myBundle;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        myBundle = bundle;

        if (!isOnline(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {
            setContentView(R.layout.activity_main);
            gridLayoutMain = findViewById(R.id.grid_layout_main);
            int spanCount = getResources().getInteger(R.integer.span_count);
            gridLayoutMain.setColumnCount(spanCount);

            beforeCreate(bundle);
        }
    }

    // Call Back method to get the Message form Detail Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String movieID = intent.getStringExtra(KEYWORD_ID);
                String movieTitle = intent.getStringExtra(KEYWORD_TITLE);
                String posterImageName = intent.getStringExtra(KEYWORD_POSTER_PATH);
                String movieRating = intent.getStringExtra(KEYWORD_VOTE_AVERAGE);
                String movieReleaseDate = intent.getStringExtra(KEYWORD_RELEASE_DATE);
                String movieDescription = intent.getStringExtra(KEYWORD_OVERVIEW);

                Favorites();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    public void beforeCreate(Bundle bundle) {
        if (bundle != null) {
            Log.d(TAG, "beforeCreate: ");
            IDList = bundle.getStringArray(KEYWORD_ID);
            titleList = bundle.getStringArray(KEYWORD_TITLE);
            pathList = bundle.getStringArray(KEYWORD_POSTER_PATH);
            descriptionList = bundle.getStringArray(KEYWORD_OVERVIEW);
            ratingList = bundle.getDoubleArray(KEYWORD_VOTE_AVERAGE);
            dateList = bundle.getStringArray(KEYWORD_RELEASE_DATE);
            populateALL();
        } else {
            if (sortCriteria == null) {
                Log.d(TAG, "beforeCreate: IS NULL");
                sortCriteria = getString(R.string.sort_by_most_popular);
                new FetchMoviesTask().execute();
            }
        }
    }

    // Function to check internet connectivity
    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return ni.isConnected();
        }
    }

    // Pop up window when no internet access
    public AlertDialog.Builder buildDialog(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.no_connection);
        builder.setMessage(R.string.exit);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //================================== MENU =============================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.sort_most_pop):
                sortCriteria = this.getString(R.string.sort_by_most_popular);
                new FetchMoviesTask().execute();
                return true;
            case (R.id.sort_highest_rated):
                sortCriteria = this.getString(R.string.sort_by_highest_rated);
                new FetchMoviesTask().execute();
                return true;
            case (R.id.sort_favorites):
                sortCriteria = this.getString(R.string.sort_by_favorites);
                //shouldExecuteOnResume = true;
                Favorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(null==myBundle)
//        {
//            Log.d(TAG, "myBundle is null");
//
//        }else{ Log.d(TAG, "myBundle is not null");
//           }
//        beforeCreate(myBundle);
        //Favorites();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        myBundle = bundle;
        //Prevent app crashing when screen is rotated
        //Will be called before onStop() instead of immediately before onPause()
        //Passing an array of String values from one Activity to another

        bundle.putStringArray(KEYWORD_ID, IDList);
        bundle.putStringArray(KEYWORD_TITLE, titleList);
        bundle.putStringArray(KEYWORD_POSTER_PATH, pathList);
        bundle.putStringArray(KEYWORD_OVERVIEW, descriptionList);
        bundle.putDoubleArray(KEYWORD_VOTE_AVERAGE, ratingList);
        bundle.putStringArray(KEYWORD_RELEASE_DATE, dateList);

        super.onSaveInstanceState(bundle);
    }

    private void populateALL() {
        //Setting up GridLayout
        gridLayoutMain.removeAllViews();

        if (null == pathList) {
            Log.d(TAG, "pathList is null");
        } else {
            Log.d(TAG, "pathList in Not null");
        }

        for (int i = 0; i < pathList.length; i++) {
            final String posterURL = POSTER_BASE_URL + POSTER_SIZE + pathList[i];
            ImageView posterImage = new ImageView(this);
            Picasso
                    .with(this)
                    .load(posterURL)
                    .placeholder(R.color.colorPrimary)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImage);
            posterImage.setAdjustViewBounds(true);

            GridLayout.LayoutParams gridLayout = new GridLayout.LayoutParams();
            gridLayout.width = GridLayout.LayoutParams.WRAP_CONTENT;
            gridLayout.height = GridLayout.LayoutParams.WRAP_CONTENT;
            gridLayout.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gridLayout.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            posterImage.setLayoutParams(gridLayout);

            //Using INTENT to move data from MainActivity to DetailActivity
            final String id = IDList[i];
            final String title = titleList[i];
            final String posterImageName = pathList[i];
            final double rating = ratingList[i];
            final String releaseDate = dateList[i];
            final String description = descriptionList[i];

            posterImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent INTENT =
                            new Intent(MainActivity.this, DetailActivity.class);
                    INTENT.putExtra(getString(R.string.movie_id), id);
                    INTENT.putExtra(getString(R.string.movie_title), title);
                    INTENT.putExtra(getString(R.string.movie_poster_url), posterImageName);
                    INTENT.putExtra(getString(R.string.movie_rating), rating);
                    INTENT.putExtra(getString(R.string.movie_release_date), releaseDate);
                    INTENT.putExtra(getString(R.string.movie_description), description);
                    //startActivity(INTENT); //VERY IMPORTANT.
                    startActivityForResult(INTENT, 1);
                    //Will not send INTENT to Detail Activity if you do not startActivity
                }
            });

            gridLayoutMain.addView(posterImage);
        }
    }

    //Getting a movie InputStream from the open connection
    public class FetchMoviesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL movieURL = NetworkUtils.movieURL(sortCriteria);
                return NetworkUtils.getResponseFromHttpUrl(movieURL);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            extractALL(result);
            populateALL();
        }
    }


    //Extracting detailed movie information from JSON InputStream
    private void extractALL(String jsonObject) {
        try {
            JSONObject moviesObject = new JSONObject(jsonObject);
            JSONArray jsonMovieArray = moviesObject.getJSONArray(KEYWORD_RESULTS);
            IDList = new String[jsonMovieArray.length()];
            pathList = new String[jsonMovieArray.length()];
            titleList = new String[jsonMovieArray.length()];
            ratingList = new double[jsonMovieArray.length()];
            dateList = new String[jsonMovieArray.length()];
            descriptionList = new String[jsonMovieArray.length()];

            for (int i = 0; i < jsonMovieArray.length(); i++) {
                IDList[i] = jsonMovieArray.getJSONObject(i).getString(KEYWORD_ID);
                pathList[i] = jsonMovieArray.getJSONObject(i).getString(KEYWORD_POSTER_PATH);
                titleList[i] = jsonMovieArray.getJSONObject(i).getString(KEYWORD_TITLE);
                ratingList[i] = jsonMovieArray.getJSONObject(i).getDouble(KEYWORD_VOTE_AVERAGE);
                dateList[i] = jsonMovieArray.getJSONObject(i).getString(KEYWORD_RELEASE_DATE);
                descriptionList[i] = jsonMovieArray.getJSONObject(i).getString(KEYWORD_OVERVIEW);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Based on Jeriel recommendation from technical webinar
    private void Favorites() {
        Uri uri = FavoritesProvider.FINAL_URI;

        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) { //If in our DB we have favorite movies
            int i = 0;
            int cursorCount = cursor.getCount();

            IDList = new String[cursorCount];
            titleList = new String[cursorCount];
            pathList = new String[cursorCount];
            ratingList = new double[cursorCount];
            dateList = new String[cursorCount];
            descriptionList = new String[cursorCount];
            while (!cursor.isAfterLast()) { //Loop will traverse utill last record of cursor
                //IDList[i] = ((int) ((int) cursor.getValue().getColumnIndex(COLUMN_MOVIE_ID)));

                IDList[i] = cursor
                        .getString(cursor.getColumnIndex(COLUMN_MOVIE_ID));
                titleList[i] = cursor
                        .getString(cursor.getColumnIndex(COLUMN_MOVIE_TITLE));
                pathList[i] = cursor.
                        getString(cursor.getColumnIndex(COLUMN_MOVIE__POSTER));
                ratingList[i] = cursor
                        .getDouble(cursor.getColumnIndex(COLUMN_MOVIE_RATING));
                dateList[i] = cursor.
                        getString(cursor.getColumnIndex(COLUMN_MOVIE_RELEASE_DATE));
                descriptionList[i] = cursor
                        .getString(cursor.getColumnIndex(COLUMN_MOVIE_DESCRIPTION));
                i++;
                cursor.moveToNext();
            }

            cursor.close();
            populateALL();
        } else { //If we did not select any favorites movies.
            gridLayoutMain.removeAllViews(); //VERY IMPORTANT to remove all pre-populated views
            Toast.makeText(MainActivity.this, getString(R.string.no_favorites), Toast.LENGTH_LONG).show();
        }
    }
}

