package com.tourmate.bd.components;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tourmate.bd.data.DataRepository;
import com.tourmate.bd.database.TourMateDatabaseHelper;
import com.tourmate.bd.models.TouristSpot;

import java.util.List;

/**
 * Custom Content Provider for TourMate BD (Topic 10: Content Providers).
 * Exposes tourist spots and saved favorites data via standard Android Content URIs.
 * Allows components and external apps to safely query and interact with TourMate data.
 */
public class TourMateContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.tourmate.bd.provider";
    public static final Uri CONTENT_URI_FAVORITES = Uri.parse("content://" + AUTHORITY + "/favorites");
    public static final Uri CONTENT_URI_SPOTS = Uri.parse("content://" + AUTHORITY + "/spots");

    private static final int CODE_FAVORITES = 100;
    private static final int CODE_FAVORITE_ID = 101;
    private static final int CODE_SPOTS = 200;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "favorites", CODE_FAVORITES);
        uriMatcher.addURI(AUTHORITY, "favorites/#", CODE_FAVORITE_ID);
        uriMatcher.addURI(AUTHORITY, "spots", CODE_SPOTS);
    }

    private TourMateDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = TourMateDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = uriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case CODE_FAVORITES:
                cursor = dbHelper.queryFavorites(projection, selection, selectionArgs, sortOrder);
                break;

            case CODE_FAVORITE_ID:
                String id = uri.getLastPathSegment();
                cursor = dbHelper.queryFavorites(projection, TourMateDatabaseHelper.COL_FAV_ID + " = ?",
                        new String[]{id}, sortOrder);
                break;

            case CODE_SPOTS:
                // Expose in-memory spots catalog as a Cursor
                String[] columns = {"_id", "spot_id", "name", "name_bn", "district_id"};
                MatrixCursor matrixCursor = new MatrixCursor(columns);
                List<TouristSpot> spots = DataRepository.getInstance().getSpotsByDistrict("sylhet_dist");
                int index = 1;
                for (TouristSpot s : spots) {
                    matrixCursor.addRow(new Object[]{index++, s.getId(), s.getName(), s.getNameBn(), s.getDistrictId()});
                }
                cursor = matrixCursor;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null && cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_FAVORITES:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".favorites";
            case CODE_FAVORITE_ID:
                return "vnd.android.cursor.item/" + AUTHORITY + ".favorites";
            case CODE_SPOTS:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".spots";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != CODE_FAVORITES) {
            throw new IllegalArgumentException("Insert only supported on favorites URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(TourMateDatabaseHelper.TABLE_FAVORITES, null, values);

        if (id > 0 && getContext() != null) {
            Uri itemUri = ContentUris.withAppendedId(CONTENT_URI_FAVORITES, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uriMatcher.match(uri) != CODE_FAVORITES && uriMatcher.match(uri) != CODE_FAVORITE_ID) {
            throw new IllegalArgumentException("Delete only supported on favorites: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (uriMatcher.match(uri) == CODE_FAVORITE_ID) {
            String id = uri.getLastPathSegment();
            rowsDeleted = db.delete(TourMateDatabaseHelper.TABLE_FAVORITES,
                    TourMateDatabaseHelper.COL_FAV_ID + " = ?", new String[]{id});
        } else {
            rowsDeleted = db.delete(TourMateDatabaseHelper.TABLE_FAVORITES, selection, selectionArgs);
        }

        if (rowsDeleted > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uriMatcher.match(uri) != CODE_FAVORITES) {
            throw new IllegalArgumentException("Update not supported: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = db.update(TourMateDatabaseHelper.TABLE_FAVORITES, values, selection, selectionArgs);
        if (rowsUpdated > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
