package com.ilham1012.ecgbpi.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.ilham1012.ecgbpi.POJO.EcgRecord;
import com.ilham1012.ecgbpi.RetrofitInterface.EcgRecordService;
import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.app.Constants;
import com.ilham1012.ecgbpi.helper.FileWriterECG;
import com.ilham1012.ecgbpi.helper.SQLiteHandler;
import com.ilham1012.ecgbpi.RetrofitInterface.FileUploadService;
import com.ilham1012.ecgbpi.services.BitalinoIntentService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import roboguice.activity.RoboActivity;

public class RecordActivity extends RoboActivity implements ServiceConnection {

    private static final String TAG = "MainActivity";
    //    private static final String API_BASE_URL = "http://192.168.2.131:8888/test_api/api";  //@rumah
//    private static final String API_BASE_URL = "http://192.168.31.18/ecgbpi/api";         //@desain-bpi
//    private static final String API_BASE_URL = "http://192.168.1.34/ecgbpi/api";            //@kosan
    private static final String API_BASE_URL = "http://ecgbpi.azurewebsites.net/api";
    private static final boolean UPLOAD = true;
    private final static int[] selChannels = {2};
    private IntentFilter mIntentFilterQRS, mIntentFilterValue;
    /*
     * http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
     * #createRfcommSocketToServiceRecord(java.util.UUID)
     *
     * "Hint: If you are connecting to a Bluetooth serial board then try using the
     * well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you
     * are connecting to an Android peer then please generate your own unique
     * UUID."
     */
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView tvLog;
    private boolean recordingOn = false;
    private boolean testInitiated = false;
    private Button startBtn;
    //    private Button stopBtn;
    private EcgRecord ecgRecord;
    private SQLiteHandler db;
    private Long tsLong;
    private String tempData = "";
    private boolean isWaiting = true;

    //    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    //    private GraphView graph;
    private LineChart rawChart;
    private Integer[] buffer = new Integer[60];
    private Intent intentSvc;
    private FileWriterECG fileWriterECG, fileWriterECGPeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ecgRecord = new EcgRecord();

        ecgRecord.setUserId(1);

        Bundle extras = getIntent().getExtras();
        ecgRecord.setRecordingName(extras.getString("recording_name"));
        ecgRecord.setFileUrl(ecgRecord.getRecordingName() + ".json");

        this.setTitle(ecgRecord.getRecordingName());

        db = new SQLiteHandler(getBaseContext());

        initGraph();
        tvLog = (TextView) findViewById(R.id.log);
        startBtn = (Button) findViewById(R.id.btnStartRecord);
        startBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recordingOn) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
        intentSvc = new Intent(this, BitalinoIntentService.class);
        intentSvc.putExtra("ECG_RECORD", ecgRecord);
        mIntentFilterValue = new IntentFilter();
        mIntentFilterValue.addAction(Constants.BROADCAST_BITALINO_SGLVALUE);
        mIntentFilterQRS = new IntentFilter();
        mIntentFilterQRS.addAction(Constants.BROADCAST_QRS_DETECTION_RESULT);
    }

    private BroadcastReceiver receiverBitValue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double val = intent.getDoubleExtra(Constants.BITALINO_SGLVALUE, 0);
            if (isWaiting) {
                isWaiting = false;
                tvLog.setText("Recording process...");
            }
            addEntry(val);
        }
    };

    private BroadcastReceiver receiverQRSResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] result = intent.getDoubleArrayExtra(Constants.QRS_DETECTION_RESULT);
            int ith_result = intent.getIntExtra(Constants.ITH_QRS_RESULT, 0);
            if (fileWriterECGPeak != null) {
                for (int i = 0; i < result.length; i++) {
                    if (result[i] != 0)
                        fileWriterECGPeak.writeDataToFile(ith_result * Constants.BUFFER_WINDOW_SIZE + ((int) result[i] - 1));
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiverQRSResult, mIntentFilterQRS);
        registerReceiver(receiverBitValue, mIntentFilterValue);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiverQRSResult);
        unregisterReceiver(receiverBitValue);
        super.onDestroy();
    }

    private void initGraph() {
        rawChart = (LineChart) findViewById(R.id.rawChart);
        LineData data = new LineData();
        rawChart.setData(data);
    }

    private void addEntry(double newdata) {
        int GRAPH_WIDTH = 1000;
        LineData data = rawChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) newdata), 0);
            rawChart.notifyDataSetChanged();

            rawChart.setVisibleXRangeMaximum(GRAPH_WIDTH);
            rawChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "ECG");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setColor(Color.MAGENTA);
        return set;
    }

    private void startRecording() {
        tsLong = System.currentTimeMillis() / 1000;
        ecgRecord.setRecordingTime(tsLong.toString());

        Toast.makeText(getApplicationContext(),
                "Record start at " + ecgRecord.getRecordingTime(), Toast.LENGTH_LONG).show();
        isWaiting = true;
        tvLog.setText("Waiting");
        fileWriterECGPeak = new FileWriterECG("p_" + ecgRecord.getFileUrl());
        startService(intentSvc);
        bindService(intentSvc, RecordActivity.this, Context.BIND_AUTO_CREATE);
        startBtn.setText("Stop Recording");
        recordingOn = true;
    }

    private void stopRecording() {
        tvLog.setText("Recording Stopped");
        unbindService(RecordActivity.this);
        stopService(intentSvc);
        startBtn.setText("Start Recording");
        recordingOn = false;
        tvLog.setText("Waiting");

        Toast.makeText(getApplicationContext(),
                "Record " + ecgRecord.getRecordingName() + " has been saved", Toast.LENGTH_LONG).show();
        if (fileWriterECGPeak != null) fileWriterECGPeak.stopWriting();
    }

    /**
     * Method to check whether external media available and writable. This is adapted from
     * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
     */

    private void checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        Log.i(TAG, "External Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);
    }

    /**
     * Method to write ascii text characters to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     */

    private void writeToSDFile() {

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        Log.i(TAG, "External file system root: " + root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File(root.getAbsolutePath() + "/ecgbpi/ecgrecord/");
        dir.mkdirs();
        File file = new File(dir, ecgRecord.getFileUrl());

        try {
            FileOutputStream f = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(f);
            pw.print("[");
            pw.print(tempData);
            pw.print("0 ]");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "File written to " + file);
    }

    private void uploadFile() {

        File root = android.os.Environment.getExternalStorageDirectory();

        try {
            RestAdapter restAdapter2 = new RestAdapter.Builder()
                    .setEndpoint(API_BASE_URL)
                    .build();
            FileUploadService fileUploadService = restAdapter2.create(FileUploadService.class);
            TypedFile typedFile = new TypedFile("multipart/form-data", new File(root.getAbsolutePath() + "/ecgbpi/ecgrecord/" + ecgRecord.getFileUrl()));
            String description = "hello, this is description speaking";

            fileUploadService.upload(typedFile, description, new Callback<String>() {
                @Override
                public void success(String s, Response response) {
                    Log.e("Upload", "success " + response.getStatus());
                }

                @Override
                public void failure(RetrofitError error) {
//                    String json =  new String(((TypedByteArray)error.getResponse().getBody()).getBytes());
//                    Log.v("failure", json.toString());
                    Log.e("Upload", "error " + error.getMessage());
                }
            });
        } catch (RetrofitError error) {
            Log.e("Upload", "error.... " + error.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private class PostRecordingTask extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            postRecording();
            return null;
        }

        private void postRecording() {
            if (UPLOAD) {
                try {
                    System.setProperty("http.keepAlive", "false");
                    // instantiate reading service client
                    RestAdapter restAdapter2 = new RestAdapter.Builder()
                            .setEndpoint(API_BASE_URL)
                            .build();
                    EcgRecordService service2 = restAdapter2.create(EcgRecordService.class);

                    // upload reading
                    Response response2 = service2.uploadReading(ecgRecord);
                    Log.e(TAG, "Response2 : " + response2.getStatus());
                    assert response2.getStatus() == 200;
                } catch (RetrofitError error) {
                    Log.e(TAG, "POST recording error : " + error.getMessage());
                }
            }
        }
    }

}
