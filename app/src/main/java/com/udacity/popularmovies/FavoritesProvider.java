package com.udacity.popularmovies;

import android.arch.lifecycle.LiveData;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

//Class based on Udacity project Sunshine tutorials as well as Android official documentation

public class FavoritesProvider extends ContentProvider {

    //Parts of URI
    public static final String PREFIX = "content://";
    public static final String AUTHORITY = "com.udacity.popularmovies";
    public static final Uri BASE_URI = Uri.parse(PREFIX + AUTHORITY);
    public static final Uri FINAL_URI = BASE_URI.buildUpon()
            .appendPath(FavoritesDbHelper.TABLE_NAME)
            .build();

    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_MOVIE_TITLE = "title";
    public static final String COLUMN_MOVIE__POSTER = "poster_key";
    public static final String COLUMN_MOVIE_DESCRIPTION = "description";
    public static final String COLUMN_MOVIE_RATING = "rating";
    public static final String COLUMN_MOVIE_RELEASE_DATE = "release_date";


    private FavoritesDbHelper DBHelper;

    @Override
    public boolean onCreate() {
        DBHelper = new FavoritesDbHelper(this.getContext());
        return true;
    }

    //======================================== QUERY ==============================================
    @Nullable
    @Override
    //Query the table and returning a Cursor over the result set.
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                                       @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        cursor = DBHelper.getReadableDatabase().query(
                FavoritesDbHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        //Watch a content URI for changes.
        //This can be the URI of a specific data row (for example,
        //"content://my_provider_type/23"), or a a generic URI for a content type.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    //======================================== INSERT =============================================
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final SQLiteDatabase DB = DBHelper.getWritableDatabase();
        long movieID = DB.insert(FavoritesDbHelper.TABLE_NAME, null, values);

        if (movieID > 0) {
            Uri result = ContentUris.withAppendedId(uri, movieID);
            getContext().getContentResolver().notifyChange(result, null);
            return result;
        } else {
            return null;
        }
    }


    //======================================== DELETE =============================================
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase database = DBHelper.getWritableDatabase();
        if (null == selection) {
            selection = "1";
        }
        int deleted = database.delete(FavoritesDbHelper.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }


    //======================================== UPDATE =============================================
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }


    //======================================== GET TYPE ===========================================
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}
