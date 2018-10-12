package com.udacity.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    public static final int VERSION_NUMBER = 1;
    public static final String TABLE_NAME = "favorites";

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME
                        + " ("
                        + FavoritesProvider.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + FavoritesProvider.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                        + FavoritesProvider.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                        + FavoritesProvider.COLUMN_MOVIE__POSTER + " TEXT, "
                        + FavoritesProvider.COLUMN_MOVIE_DESCRIPTION + " TEXT, "
                        + FavoritesProvider.COLUMN_MOVIE_RATING + " REAL, "
                        + FavoritesProvider.COLUMN_MOVIE_RELEASE_DATE + " TIMESTAMP);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
