package com.ilham1012.ecgbpi.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by ilham1012 on 2/14/16.
 */
public class DataContentProvider extends ContentProvider {
    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    @Override
    public boolean onCreate() {
        return true;
    }
    /*
     * Return no type for MIME type
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
    /*
     * query() always returns no results
     *
     */
    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        return null;
    }
    /*
     * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
    /*
     * delete() always returns "no rows affected" (0)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        return 0;
    }

//    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//    private static final int USER_ID = 1;
//    private static final String AUTHORITY = "com.ilham1012.ecgbpi.provider";
//
//    // Ecg Records table name
//    private static final String TABLE_ECG_RECORDING = "ecg_record";
//
//    static {
//        uriMatcher.addURI(AUTHORITY, TABLE_ECG_RECORDING, USER_ID);
//    }
//
//    private SQLiteHandler dbHelper;
//
//    @Override
//    public boolean onCreate() {
//        dbHelper = new SQLiteHandler(getContext());
//        return true;
//    }
//
//    @Override
//    public Cursor query(Uri uri, String[] columns, String selection,
//                        String[] selectionArgs, String sortOrder) {
//        List segments;
//        segments = uri.getPathSegments();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        switch (uriMatcher.match(uri)) {
//            case USER_ID:
//                return db.query(
//                        TABLE_ECG_RECORDING
//                        columns,
//                        buildSelection(
//                                DataContract.Team.COLUMN_NAME_LEAGUE_ID + "=? AND “ +
//                                DataContract.Team._ID + "=?", selection),
//                        buildSelectionArgs(
//                                new String[] {segments.get(1), segments.get(3)},
//                                selectionArgs),
//                        null, null, sortOrder);
//            default:
//                throw new RuntimeException("No content provider URI match.");
//        }
//    }
//
//    @Override
//    public String getType(Uri uri) {
//        switch (uriMatcher.match(uri)) {
//            case USER_ID:
//                return "vnd.android.cursor.item/vnd.com.ilham1012.provider.ecgrecord";
//            default:
//                throw new RuntimeException("No content provider URI match.");
//        }
//    }
//
//    private String buildSelection(String baseSelection, String selection) {
//        if (TextUtils.isEmpty(selection)) {
//            return baseSelection;
//        }
//        return TextUtils.concat(baseSelection,
//                " AND (",
//                selection, ")").toString();
//    }
//
//    private String[] buildSelectionArgs(String[] baseArgs,
//                                        String[] selectionArgs) {
//        if (selectionArgs == null || selectionArgs.length == 0) {
//            return baseArgs;
//        }
//        return ArrayUtils.addAll(baseArgs, selectionArgs);
//    }
}