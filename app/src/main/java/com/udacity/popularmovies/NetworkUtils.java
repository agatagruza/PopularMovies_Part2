package com.udacity.popularmovies;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

//Class based on Udacity T02.03-Exercise-DisplayUrl and Fething the HTTP Requests
public class NetworkUtils {
    private static final String MOVIE_BASE_URL = "https://api.themoviedb.org/";
    private static final String MOVIE_API_VERSION = "3";
    private static final String KEY_WORD_MOVIE = "movie";
    private static final String KEY_WORD_VIDEOS = "videos";
    private static final String KEY_WORD_REVIEWS = "reviews";
    private static final String API_KEY = "api_key";


    //================================= MOVIE URL ======================================//
    public static URL movieURL(String sortType) {
        //Will produce Android Uri but our method requires a Java URL
        Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE_API_VERSION)
                .appendPath(KEY_WORD_MOVIE)
                .appendPath(sortType)
                .appendQueryParameter(API_KEY, BuildConfig.ApiKey)
                .build();
        //Converting newly build Uri to a Java URL
        //by passing it as a string parameter to Java URL constructor.
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    //================================= TRAILER URL ===================================//
    public static URL trailersURL(String id) {
        Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE_API_VERSION)
                .appendPath(KEY_WORD_MOVIE)
                .appendPath(id)
                .appendPath(KEY_WORD_VIDEOS)
                .appendQueryParameter(API_KEY, BuildConfig.ApiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    //================================= REVIEWS URL ==================================//
    public static URL reviewsURL(String id) {
        Uri uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE_API_VERSION)
                .appendPath(KEY_WORD_MOVIE)
                .appendPath(id)
                .appendPath(KEY_WORD_REVIEWS)
                .appendQueryParameter(API_KEY, BuildConfig.ApiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream input = urlConnection.getInputStream();

            Scanner scanner = new Scanner(input);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
