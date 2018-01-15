package com.ilham1012.ecgbpi.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by ilham on 11/02/2016.
 */
public class SQLiteHandler extends SQLiteOpenHelper{
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_UID = "uid";


    // Ecg Records table name
    private static final String TABLE_ECG_RECORDING = "ecg_record";

    // Ecg Records Columns names
    private static final String KEY_ECG_ID = "_id";
    private static final String KEY_ECG_USER_ID = "user_id";
    private static final String KEY_ECG_NAME = "recording_name";
    private static final String KEY_ECG_TIME = "recording_time";
    private static final String KEY_ECG_URL = "file_url";




    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE," + KEY_USER_UID + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_ECG_RECORD_TABLE = "CREATE TABLE " + TABLE_ECG_RECORDING + "("
                + KEY_ECG_ID + " INTEGER PRIMARY KEY," + KEY_ECG_USER_ID + " INTEGER,"
                + KEY_ECG_NAME + " TEXT," + KEY_ECG_TIME + " TEXT,"
                + KEY_ECG_URL + " TEXT" + ")";
        db.execSQL(CREATE_ECG_RECORD_TABLE);

//        initializeSampleData();

        Log.d(TAG, "Database tables created");
    }

    public void initializeSampleData() {
        addEcgRecord(1, "2016-02-06 10:38:19", "test", "test.txt");
        addEcgRecord(1, "2016-02-06 10:47:27", "test 2", "test-2.txt");
        addEcgRecord(1, "2016-02-06 10:49:20", "Morning Record", "morning.txt");
        addEcgRecord(1, "2016-02-06 10:50:19", "Let's test", "testttt.txt");
        addEcgRecord(1, "2016-02-06 10:53:27", "Ok, test again", "test-2000.txt");
        addEcgRecord(1, "2016-02-06 10:59:20", "Night Record", "night.txt");
        addEcgRecord(1, "2016-02-06 11:38:19", "test 3", "test3.txt");
        addEcgRecord(1, "2016-02-06 11:47:27", "test 23", "test-23.txt");
        addEcgRecord(1, "2016-02-06 11:49:20", "Morning Record 2", "morning2.txt");
        addEcgRecord(1, "2016-02-06 11:50:19", "Let's test 2", "testttt2.txt");
        addEcgRecord(1, "2016-02-06 11:53:27", "Ok, test again 2", "test-20002.txt");
        addEcgRecord(1, "2016-02-06 11:59:20", "Night Record 2", "night2.txt");
        addEcgRecord(1, "2016-02-06 12:50:19", "Let's test 3", "testttt3.txt");
        addEcgRecord(1, "2016-02-06 12:53:27", "Ok, test again 3", "test-20003.txt");
        addEcgRecord(1, "2016-02-06 12:59:20", "Night Record 3", "night3.txt");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ECG_RECORDING);

        // Create tables again
        onCreate(db);
    }


    /** ---------------------------------------------------------------
     * ------------------------- TABLE 'User' -------------------------
     * ------------------------------------------------------------- */

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, name); // Name
        values.put(KEY_USER_EMAIL, email); // Email
        values.put(KEY_USER_UID, uid); // Email

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUserTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }




    /** ---------------------------------------------------------------
     * ---------------------- TABLE 'Ecg_record' ----------------------
     * ------------------------------------------------------------- */

    /**
     * Storing ecg record in database
     * @param userId
     * @param name
     * @param time
     * @param fileUrl
     * */
    public boolean addEcgRecord(int userId, String time, String name, String fileUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ECG_USER_ID, userId); // Recording User ID
        values.put(KEY_ECG_NAME, name); // Recording Name
        values.put(KEY_ECG_TIME, time); // Recording Time
        values.put(KEY_ECG_URL, fileUrl); // Recording file

        // Inserting Row
        long id = 0;
        id = db.insert(TABLE_ECG_RECORDING, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New ecg record inserted into sqlite: " + id);

        return id > 0;
    }

    /**
     * Fetch all ECG Records
     */
    public Cursor fetchAllEcgRecords() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor mCursor = db.query(TABLE_ECG_RECORDING, new String[]{KEY_ECG_ID,
                        KEY_ECG_USER_ID, KEY_ECG_NAME, KEY_ECG_TIME, KEY_ECG_URL},
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Delete ecg record
     * @param recordId
     * */
    public boolean deleteEcgRecord(int recordId) {
        SQLiteDatabase db = this.getWritableDatabase();

        long delete = 0;
        delete = db.delete(TABLE_ECG_RECORDING, KEY_ECG_ID + " = ", new String[]{String.valueOf(recordId)});
        db.close();

        Log.d(TAG, "Deleted record " + recordId + " from sqlite");

        return delete > 0;
    }



    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteEcgRecordTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_ECG_RECORDING, null, null);
        db.close();

        Log.d(TAG, "Deleted all ecg record from sqlite");
    }

}
