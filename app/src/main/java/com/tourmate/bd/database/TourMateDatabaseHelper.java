package com.tourmate.bd.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tourmate.bd.models.TouristSpot;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for TourMate BD (Topic 11: Database Manipulation).
 * Manages local persistent storage for user favorites, bookmarks, and travel notes.
 */
public class TourMateDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tourmate_bd.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Favorites
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COL_FAV_ID = "_id";
    public static final String COL_FAV_SPOT_ID = "spot_id";
    public static final String COL_FAV_NAME = "spot_name";
    public static final String COL_FAV_DISTRICT = "district_id";
    public static final String COL_FAV_CATEGORY = "category";
    public static final String COL_FAV_TIMESTAMP = "timestamp";

    // Table: Spot Notes
    public static final String TABLE_NOTES = "spot_notes";
    public static final String COL_NOTE_ID = "_id";
    public static final String COL_NOTE_SPOT_ID = "spot_id";
    public static final String COL_NOTE_TEXT = "note_text";
    public static final String COL_NOTE_TIMESTAMP = "timestamp";

    private static TourMateDatabaseHelper instance;

    public static synchronized TourMateDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TourMateDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public TourMateDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Favorites table
        String createFavTable = "CREATE TABLE " + TABLE_FAVORITES + " ("
                + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FAV_SPOT_ID + " TEXT UNIQUE NOT NULL, "
                + COL_FAV_NAME + " TEXT NOT NULL, "
                + COL_FAV_DISTRICT + " TEXT, "
                + COL_FAV_CATEGORY + " TEXT, "
                + COL_FAV_TIMESTAMP + " INTEGER"
                + ");";
        db.execSQL(createFavTable);

        // Create Spot Notes table
        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + " ("
                + COL_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NOTE_SPOT_ID + " TEXT NOT NULL, "
                + COL_NOTE_TEXT + " TEXT NOT NULL, "
                + COL_NOTE_TIMESTAMP + " INTEGER"
                + ");";
        db.execSQL(createNotesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Favorites CRUD Operations
    // ─────────────────────────────────────────────────────────────────────────

    public boolean addFavorite(TouristSpot spot) {
        if (spot == null || spot.getId() == null) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_SPOT_ID, spot.getId());
        values.put(COL_FAV_NAME, spot.getName());
        values.put(COL_FAV_DISTRICT, spot.getDistrictId());
        values.put(COL_FAV_CATEGORY, spot.getCategory() != null ? spot.getCategory().getDisplayName() : "");
        values.put(COL_FAV_TIMESTAMP, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean removeFavorite(String spotId) {
        if (spotId == null) return false;
        SQLiteDatabase db = getWritableDatabase();
        int affected = db.delete(TABLE_FAVORITES, COL_FAV_SPOT_ID + " = ?", new String[]{spotId});
        return affected > 0;
    }

    public boolean isFavorite(String spotId) {
        if (spotId == null) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COL_FAV_ID},
                COL_FAV_SPOT_ID + " = ?", new String[]{spotId},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public List<String> getAllFavoriteSpotIds() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COL_FAV_SPOT_ID},
                null, null, null, null, COL_FAV_TIMESTAMP + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_SPOT_ID)));
            }
            cursor.close();
        }
        return list;
    }

    public int getFavoriteCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FAVORITES, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spot Personal Notes CRUD
    // ─────────────────────────────────────────────────────────────────────────

    public boolean addNote(String spotId, String noteText) {
        if (spotId == null || noteText == null || noteText.trim().isEmpty()) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTE_SPOT_ID, spotId);
        values.put(COL_NOTE_TEXT, noteText.trim());
        values.put(COL_NOTE_TIMESTAMP, System.currentTimeMillis());
        long result = db.insert(TABLE_NOTES, null, values);
        return result != -1;
    }

    public List<String> getNotesForSpot(String spotId) {
        List<String> notes = new ArrayList<>();
        if (spotId == null) return notes;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COL_NOTE_TEXT},
                COL_NOTE_SPOT_ID + " = ?", new String[]{spotId},
                null, null, COL_NOTE_TIMESTAMP + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                notes.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE_TEXT)));
            }
            cursor.close();
        }
        return notes;
    }

    // Direct cursor accessor for ContentProvider
    public Cursor queryFavorites(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_FAVORITES, projection, selection, selectionArgs, null, null, sortOrder);
    }
}
