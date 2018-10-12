package com.udacity.popularmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class DetailActivity extends AppCompatActivity {
    private LinearLayout lLayoutTrailers;
    private LinearLayout lLayoutReviews;
    private ImageView posterViewDetail;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvRating;
    private TextView tvReleaseDate;
    private FloatingActionButton myFab;
    private String[] trailerKeysList;
    private String[] trailerNamesList;
    private String[] trailerAuthorsList;
    private String[] trailerReviewsList;
    private String movieID;
    private String movieTitle;
    private String posterImageName;
    private double movieRating;
    private String movieReleaseDate;
    private String movieDescription;
    private final String KEY = "key";
    private final String NAME = "name";
    private final String AUTHOR = "author";
    private final String CONTENT = "content";
    private final String BASE_URL_POSTER = "http://image.tmdb.org/t/p/";
    private final String POSTER_SIZE = "w185/";
    private final String BASE_URL_TRAILER = "http://youtube.com/watch?v=";
    private final String KEYWORD_RESULTS = "results";

    private final String KEYWORD_ID = "id";
    private final String KEYWORD_POSTER_PATH = "poster_path";
    private final String KEYWORD_TITLE = "title";
    private final String KEYWORD_OVERVIEW = "overview";
    private final String KEYWORD_VOTE_AVERAGE = "vote_average";
    private final String KEYWORD_RELEASE_DATE = "release_date";

    private String shouldExecuteOnResume = "YES";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_detail);
        lLayoutTrailers = findViewById(R.id.list_of_trailers);
        lLayoutReviews = findViewById(R.id.list_of_reviews);
        tvTitle = findViewById(R.id.movie_title);
        posterViewDetail = findViewById(R.id.poster_image);
        tvRating = findViewById(R.id.movie_average_rating);
        tvReleaseDate = findViewById(R.id.movie_release_date);
        tvDescription = findViewById(R.id.movie_description);
        myFab = findViewById(R.id.fab);

        //Using INTENT to retrieve all movie data from MainActivity
        Intent intent = getIntent();
        Bundle movieBundle = intent.getExtras();
        if (movieBundle != null) {
            movieID = movieBundle.getString(getString(R.string.movie_id));
            movieTitle = movieBundle.getString(getString(R.string.movie_title));
            posterImageName = movieBundle.getString(getString(R.string.movie_poster_url));
            movieRating = movieBundle.getDouble(getString(R.string.movie_rating));
            movieReleaseDate = movieBundle.getString(getString(R.string.movie_release_date));
            movieDescription = movieBundle.getString(getString(R.string.movie_description));
        }

        populateALL();
        new trailersALL().execute();
        new reviewsALL().execute();
    }

    private void populateALL() {
        final String mPosterUrl = BASE_URL_POSTER + POSTER_SIZE + posterImageName;
        Picasso
                .with(this)
                .load(mPosterUrl)
                .placeholder(R.color.colorPrimary)
                .error(R.drawable.ic_launcher_background)
                .into(posterViewDetail);

        tvTitle.setText(movieTitle);
        tvDescription.setText(movieDescription);
        tvRating.setText(getString(R.string.movie_vote_average, movieRating));
        tvReleaseDate.setText(movieReleaseDate.substring(0, 4));

        if (isFavorite(movieID)) {
            myFab.setImageResource(R.drawable.ic_favorite_white_56dp);
        }
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite(movieID)) {
                    String sLocation = COLUMN_MOVIE_ID + " = " + movieID;
                    getContentResolver().delete(FavoritesProvider.FINAL_URI,
                            sLocation, null);

                    myFab.setImageResource(R.drawable.ic_favorite_border_white_56dp);
                    Snackbar.make(v, "Movie was just REMOVED from Favorites", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    ReadDB();

                    Intent INTENT_BACK = new Intent();
                    INTENT_BACK.putExtra(KEYWORD_ID, movieID);
                    INTENT_BACK.putExtra(KEYWORD_TITLE, movieTitle);
                    INTENT_BACK.putExtra(KEYWORD_POSTER_PATH, posterImageName);
                    INTENT_BACK.putExtra(KEYWORD_VOTE_AVERAGE, movieRating);
                    INTENT_BACK.putExtra(KEYWORD_RELEASE_DATE, movieReleaseDate);
                    INTENT_BACK.putExtra(KEYWORD_OVERVIEW, movieDescription);
                    setResult(Activity.RESULT_OK, INTENT_BACK);
                    finish();//finishing activity

                } else {
                    ContentValues contentValue = new ContentValues();
                    contentValue.put(COLUMN_MOVIE_ID, movieID);
                    contentValue.put(COLUMN_MOVIE_TITLE, movieTitle);
                    contentValue.put(COLUMN_MOVIE__POSTER, posterImageName);
                    contentValue.put(COLUMN_MOVIE_RATING, movieRating);
                    contentValue.put(COLUMN_MOVIE_RELEASE_DATE, movieReleaseDate);
                    contentValue.put(COLUMN_MOVIE_DESCRIPTION, movieDescription);
                    getContentResolver().insert(FavoritesProvider.FINAL_URI, contentValue);
                    myFab.setImageResource(R.drawable.ic_favorite_white_56dp);
                    Snackbar.make(v, "Movie was just ADDED to Favorites", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void ReadDB() {
        Uri URI = FavoritesProvider.FINAL_URI;
        Cursor cursor = getContentResolver()
                .query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) { //while loop will traverse utill last record of cursor

                movieID = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_ID));
                movieTitle = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_TITLE));
                posterImageName = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE__POSTER));
                movieRating = cursor.getDouble(cursor.getColumnIndex(COLUMN_MOVIE_RATING));
                movieReleaseDate = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_RELEASE_DATE));
                movieDescription = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_DESCRIPTION));

                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    //Checking if movie is already in favorites DB
    //Based on Jeriel recommendation from technical webinar
    public boolean isFavorite(String id) {
        Uri URI = FavoritesProvider.FINAL_URI;
        Cursor cursor = getContentResolver()
                .query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) { //while loop will traverse utill last record of cursor
                String movieID = cursor
                        .getString(cursor.getColumnIndex(COLUMN_MOVIE_ID));
                if (id.equals(movieID)) {
                    return true;
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return false;
    }

    public class trailersALL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL trailersRequestUrl = NetworkUtils.trailersURL(movieID);
                return NetworkUtils.getResponseFromHttpUrl(trailersRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            extractTrailerALL(result);
            loadTrailerALL();
        }
    }

    public class reviewsALL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL reviewsRequestUrl = NetworkUtils.reviewsURL(movieID);
                return NetworkUtils.getResponseFromHttpUrl(reviewsRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            extractReviewsALL(result);
            loadReviewALL();
        }
    }

    public void extractTrailerALL(String trailersInputStream) {
        try {
            JSONObject jsonTrailersObject = new JSONObject(trailersInputStream);
            JSONArray trailersResults = jsonTrailersObject.getJSONArray(KEYWORD_RESULTS);
            trailerKeysList = new String[trailersResults.length()];
            trailerNamesList = new String[trailersResults.length()];
            for (int i = 0; i < trailersResults.length(); i++) {
                trailerKeysList[i] = trailersResults.getJSONObject(i).optString(KEY);
                trailerNamesList[i] = trailersResults.getJSONObject(i).optString(NAME);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadTrailerALL() {
        if (trailerKeysList.length == 0) {
            TextView tvNOTrailers = new TextView(this);
            tvNOTrailers.setText(R.string.no_trailers);
            lLayoutTrailers.addView(tvNOTrailers);
        } else {
            for (int i = 0; i < trailerKeysList.length; i++) {
                Button bnTrailerItem = new Button(this);
                bnTrailerItem.setText(trailerNamesList[i]);
                bnTrailerItem.setPadding(0, 30, 0, 30);
                bnTrailerItem.setTextSize(15);
                bnTrailerItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_black_56dp, 0, 0, 0);
                bnTrailerItem.getBackground().setAlpha(5);
                final String trailerUrl = BASE_URL_TRAILER + trailerKeysList[i];
                bnTrailerItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //INTENT describes an action to perform (watching YouTube)
                        // System launches the appropriate activity from another application
                        //It's based on Udacity Webpages, Maps, and Sharing Exercise
                        Uri YTlink = Uri.parse(trailerUrl);
                        Intent YTIntent = new Intent(Intent.ACTION_VIEW, YTlink);
                        if (YTIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(YTIntent);
                        }
                    }
                });
                lLayoutTrailers.addView(bnTrailerItem);
            }
        }
    }

    public void extractReviewsALL(String reviewsResponse) {
        try {
            JSONObject jsonReviewsObject = new JSONObject(reviewsResponse);
            JSONArray reviewsResults = jsonReviewsObject.getJSONArray(KEYWORD_RESULTS);
            trailerAuthorsList = new String[reviewsResults.length()];
            trailerReviewsList = new String[reviewsResults.length()];
            for (int i = 0; i < reviewsResults.length(); i++) {
                trailerAuthorsList[i] = reviewsResults.getJSONObject(i).optString(AUTHOR);
                trailerReviewsList[i] = reviewsResults.getJSONObject(i).optString(CONTENT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void loadReviewALL() {

        TextView tvAuthor = new TextView(this);
        TextView tvContext = new TextView(this);

        if (trailerReviewsList.length == 0) {
            TextView tvNOReviews = new TextView(this);
            tvNOReviews.setText(R.string.no_reviews);
            lLayoutReviews.addView(tvNOReviews);
        } else {
            for (int i = 0; i < trailerAuthorsList.length; i++) {

                TextView tvAuthorName = new TextView(this);
                tvAuthorName.setText(trailerAuthorsList[i]);
                tvAuthorName.setPadding(0, 0, 0, 30);
                tvAuthorName.setTextSize(20);
                tvAuthorName.setTypeface(null, Typeface.BOLD);

                TextView tvComment = new TextView(this);
                tvComment.setText(trailerReviewsList[i]);
                tvComment.setPadding(0, 0, 0, 30);
                tvComment.setTextSize(15);
                tvComment.setTypeface(null, Typeface.ITALIC);

                lLayoutReviews.addView(tvAuthorName);
                lLayoutReviews.addView(tvComment);
            }
        }
    }

}
