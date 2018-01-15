package com.ilham1012.ecgbpi.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.helper.SQLiteHandler;
import com.ilham1012.ecgbpi.helper.SessionManager;

public class DashboardNewActivity extends AppCompatActivity {
    private SQLiteHandler db;
    private SessionManager session;
    private ListView listView;
    private SimpleCursorAdapter dataAdapter;

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.ilham1012.ecgbpi.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "ilham1012.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.ecgRecordListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                displayAlertDialog();


            }
        });

        // Sqlite db handler
        db = new SQLiteHandler(getApplicationContext());

        /**
         * Initialize records
         * FOR SAMPLE DATA ONLY
         */
//        db.deleteEcgRecordTable();
//        db.initializeSampleData();

        // Session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()){
            logoutUser();
        }


    }

    @Override
    protected void onResume(){
        super.onResume();
        displayListView();
    }



    private void displayAlertDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_new_record, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.dialog_recording_name);

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                Intent i = new Intent(DashboardNewActivity.this, RecordActivity.class);
                i.putExtra("recording_name", edt.getText().toString());
                startActivity(i);
//                finish();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();

    }



    private void displayListView(){
        Cursor cursor = db.fetchAllEcgRecords();

        String columns[] = new String[]{
                "recording_name",
                "recording_time"
        };

        int[] toList = new int[]{
            R.id.list_item_title,
            R.id.list_item_subtitle
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.list_item,
                cursor,
                columns,
                toList,
                0);

        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                String clickedName =
                        cursor.getString(cursor.getColumnIndexOrThrow("recording_name"));
                Toast.makeText(getApplicationContext(),
                        clickedName, Toast.LENGTH_SHORT).show();

                Intent i = new Intent(DashboardNewActivity.this, DisplaySignalActivity.class);
                i.putExtra("recording_name", clickedName);
                startActivity(i);
            }
        });
    }






    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared preferences
     * Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUserTable();

        // Launching the login activity
        Intent intent = new Intent(DashboardNewActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                gotoSetting();
                return true;
            case R.id.logout:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Open Setting/Preference activity
     */
    private void gotoSetting() {
        Intent i = new Intent(this, MyPreferencesActivity.class);
        startActivity(i);
    }


    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            return null;
        }
    }

}
