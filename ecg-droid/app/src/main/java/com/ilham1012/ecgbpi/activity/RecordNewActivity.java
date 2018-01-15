package com.ilham1012.ecgbpi.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.helper.SQLiteHandler;
import com.ilham1012.ecgbpi.helper.SessionManager;
import com.ilham1012.ecgbpi.model.DeviceConfiguration;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class RecordNewActivity extends AppCompatActivity {
    public static final String KEY_DURATION = "duration";
    public static final String KEY_RECORDING_NAME = "duration";
    public static final String KEY_CONFIGURATION = "duration";

    private SQLiteHandler db;
    private SessionManager session;
    private ListView listView;
    private SimpleCursorAdapter dataAdapter;

    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    private GraphView graph;

    private boolean menuConnected = false;

    private DeviceConfiguration recordingConfiguration;

    // AUX VARIABLES
    private Context classContext = this;
    private Bundle extras;
    private LayoutInflater inflater;


    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.ecgRecordListView);
        initGraph();

        // Sqlite db handler
        db = new SQLiteHandler(getApplicationContext());
//         initializeData();

        // Session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()){
            logoutUser();
        }
    }

    private boolean connect(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final ProgressDialog progress;
        if(recordingConfiguration.getMacAddress().compareTo("test")!= 0){ // 'test' is used to launch device emulator
            if (mBluetoothAdapter == null) {
                displayInfoToast(getString(R.string.nr_bluetooth_not_supported));
                return false;
            }
            if (!mBluetoothAdapter.isEnabled()){
                showBluetoothDialog();
                return false;
            }
        }

        progress = ProgressDialog.show(this,getResources().getString(R.string.nr_progress_dialog_title),getResources().getString(R.string.nr_progress_dialog_message), true);

        Thread connectionThread =
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            public void run(){
                                progress.dismiss();
//                                if(connectionError){
//                                    displayConnectionErrorDialog(bpErrorCode);
//                                }else{
//                                    Intent intent = new Intent(classContext, BiopluxService.class);
//                                    intent.putExtra(KEY_RECORDING_NAME, recording.getName());
//                                    intent.putExtra(KEY_CONFIGURATION, recordingConfiguration);
//                                    startService(intent);
//                                    bindToService();
                                    startChronometer();
//                                    uiMainbutton.setText(getString(R.string.nr_button_stop));
                                    displayInfoToast(getString(R.string.nr_info_started));
//                                    drawState = false;
//                                }
                            }
                        });
                    }
                });

//        if(recordingConfiguration.getMacAddress().compareTo("test")==0 && !isServiceRunning() && !recordingOverride)
//            connectionThread.start();
//        else if(mBluetoothAdapter.isEnabled() && !isServiceRunning() && !recordingOverride) {
            connectionThread.start();
//        }
        return false;

    }

    private void disconnect(){
        displayInfoToast("Disconnect");
    }

    @Override
    protected void onResume(){
        super.onResume();
        displayListView();
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                String clickedName =
                        cursor.getString(cursor.getColumnIndexOrThrow("recording_name"));
                displayInfoToast(clickedName);

            }
        });
    }


    private void showBluetoothDialog() {
        // Initializes custom title
        TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
        customTitleView.setText(R.string.nr_bluetooth_dialog_title);
        customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));

        // dialogs builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(customTitleView)
                .setMessage(getResources().getString(R.string.nr_bluetooth_dialog_message))
                .setPositiveButton(getString(R.string.nr_bluetooth_dialog_positive_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intentBluetooth = new Intent();
                                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                                classContext.startActivity(intentBluetooth);
                            }
                        });
        builder.setNegativeButton(
                getString(R.string.nc_dialog_negative_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // dialog gets closed
                    }
                });

        // creates and shows bluetooth dialog
        (builder.create()).show();
    }





    private void initGraph(){
        // we get graph view instance
        graph = (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(600);
        viewport.setScalable(true);
        viewport.setScrollable(true);

    }

    /**
     * Initialize records
     * FOR SAMPLE DATA ONLY
     */
    private void initializeData() {
        db.deleteEcgRecordTable();
        db.initializeSampleData();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared preferences
     * Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUserTable();

        // Launching the login activity
        Intent intent = new Intent(RecordNewActivity.this, LoginActivity.class);
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
            case R.id.connect:

                if(menuConnected){
                    item.setTitle("Connect");
                    menuConnected = false;
                    connect();
                }else{
                    item.setTitle("Disconnect");
                    menuConnected = true;
                    disconnect();
                }
                return true;
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

    /**
     * Displays a custom view information toast with the message it receives as
     * parameter
     */
    private void displayInfoToast(String messageToDisplay) {
        Toast.makeText(getApplicationContext(),
                messageToDisplay, Toast.LENGTH_SHORT).show();
    }

    /**
     * Starts Android' chronometer widget to display the recordings duration
     */
    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /**
     * Stops the chronometer and calculates the duration of the recording
     */
    private void stopChronometer() {
        chronometer.stop();
//        long elapsedMiliseconds = SystemClock.elapsedRealtime()
//                - chronometer.getBase();
//        duration = String.format("%02d:%02d:%02d",
//                (int) ((elapsedMiliseconds / (1000 * 60 * 60)) % 24), 	// hours
//                (int) ((elapsedMiliseconds / (1000 * 60)) % 60),	  	// minutes
//                (int) (elapsedMiliseconds / 1000) % 60);				// seconds
    }


}
