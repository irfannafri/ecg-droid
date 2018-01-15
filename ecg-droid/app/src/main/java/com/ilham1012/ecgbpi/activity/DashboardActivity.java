package com.ilham1012.ecgbpi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.helper.RVAdapter;
import com.ilham1012.ecgbpi.helper.SQLiteHandler;
import com.ilham1012.ecgbpi.helper.SessionManager;
import com.ilham1012.ecgbpi.POJO.EcgRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton fabBtn;
    private RecyclerView recyclerView;
    private List<EcgRecord> ecgRecords;



    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared preferences
     * Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUserTable();

        // Launching the login activity
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        fabBtn = (FloatingActionButton)findViewById(R.id.fab);
        recyclerView = (RecyclerView)findViewById(R.id.list_view_records);

        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView.setHasFixedSize(true);
        //collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        LinearLayoutManager llm = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(llm);

        // Sqlite db handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()){
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        //String email = user.get("email");

        // Displaying the user details on the screen
        collapsingToolbarLayout.setTitle(name);

        initializeData();
        initializeAdapter();

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "Create new recording", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    /**
     * Dummy data for examples only
     */

    private void initializeData(){
        ecgRecords = new ArrayList<>();
        ecgRecords.add(new EcgRecord(1,  1, "2016-02-06 10:38:19", "test", "test.txt"));
        ecgRecords.add(new EcgRecord(2,  1, "2016-02-06 10:47:27", "test 2", "test-2.txt"));
        ecgRecords.add(new EcgRecord(3,  1, "2016-02-06 10:49:20", "Morning Record", "morning.txt"));
        ecgRecords.add(new EcgRecord(4,  1, "2016-02-06 10:50:19", "Let's test", "testttt.txt"));
        ecgRecords.add(new EcgRecord(5,  1, "2016-02-06 10:53:27", "Ok, test again", "test-2000.txt"));
        ecgRecords.add(new EcgRecord(6,  1, "2016-02-06 10:59:20", "Night Record", "night.txt"));
        ecgRecords.add(new EcgRecord(7,  1, "2016-02-06 11:38:19", "test 3", "test3.txt"));
        ecgRecords.add(new EcgRecord(8,  1, "2016-02-06 11:47:27", "test 23", "test-23.txt"));
        ecgRecords.add(new EcgRecord(9,  1, "2016-02-06 11:49:20", "Morning Record 2", "morning2.txt"));
        ecgRecords.add(new EcgRecord(10, 1, "2016-02-06 11:50:19", "Let's test 2", "testttt2.txt"));
        ecgRecords.add(new EcgRecord(11, 1, "2016-02-06 11:53:27", "Ok, test again 2", "test-20002.txt"));
        ecgRecords.add(new EcgRecord(12, 1, "2016-02-06 11:59:20", "Night Record 2", "night2.txt"));
        ecgRecords.add(new EcgRecord(13, 1, "2016-02-06 12:50:19", "Let's test 3", "testttt3.txt"));
        ecgRecords.add(new EcgRecord(14, 1, "2016-02-06 12:53:27", "Ok, test again 3", "test-20003.txt"));
        ecgRecords.add(new EcgRecord(15, 1, "2016-02-06 12:59:20", "Night Record 3", "night3.txt"));
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(ecgRecords);
        recyclerView.setAdapter(adapter);
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
//                gotoSetting();
                return true;
            case R.id.logout:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
