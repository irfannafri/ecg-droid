package com.ilham1012.ecgbpi.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bitalino.comm.BITalinoDevice;
import com.bitalino.comm.BITalinoFrame;
import com.ilham1012.ecgbpi.POJO.EcgRecord;
import com.ilham1012.ecgbpi.activity.RecordActivity;
import com.ilham1012.ecgbpi.app.Constants;
import com.ilham1012.ecgbpi.helper.FileWriterECG;
import com.ilham1012.ecgbpi.helper.SQLiteHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BitalinoIntentService extends IntentService {
    public static final String TAG = "[BitalinoIntentService]";
    private BluetoothDevice dev = null;
    private BluetoothSocket sock = null;
    private InputStream is = null;
    private OutputStream os = null;
    private BITalinoDevice bitalino;
    private SQLiteHandler db;
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int[] selChannels = {2};
    public boolean isRunning = true;
    private double[] buffer = new double[Constants.BUFFER_WINDOW_SIZE];
    private EcgRecord ecgRecord;
    private FileWriterECG fileWriterECG;

    public BitalinoIntentService() {
        super("BitalinoIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {

    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {

    }

    public void openConnection() throws Exception {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String mac = "98:D3:31:90:3E:00";
        //String mac = "98:D3:31:B2:BB:7D";
        String strBluetoothDevice = SP.getString("bluetooth", mac); //mac;//

        final String remoteDevice = strBluetoothDevice;

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        dev = btAdapter.getRemoteDevice(remoteDevice);

        Log.d(TAG, "Stopping Bluetooth discovery.");
        btAdapter.cancelDiscovery();

        sock = dev.createRfcommSocketToServiceRecord(MY_UUID);
        sock.connect();


        bitalino = new BITalinoDevice(Constants.SAMPLING_RATE, selChannels); // new int[]{0, 1, 2, 3, 4, 5});
        bitalino.open(sock.getInputStream(), sock.getOutputStream());
        bitalino.start();
    }

    public void closeConnection() throws Exception {
        bitalino.stop();
        sock.close();
        isRunning = false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            openConnection();
            runBitalinoSVC();
            isRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runBitalinoSVC() {
        try {
            int counter = 0;
            String temp = "";
            boolean isFirst = true;
            int ith_bw = 0;
            while (isRunning) {
                if (counter % Constants.BUFFER_WINDOW_SIZE == 0) {
                    counter = 0;
                    if (!isFirst) {
                        Intent bitIntent = new Intent(this, QRSDetectionIntentService.class);
                        bitIntent.setAction(Constants.QRS_ACTION);
                        bitIntent.putExtra(Constants.BITALINO60_BUFFER_DATA, buffer);
                        bitIntent.putExtra(Constants.ITH_BUFFERWINDOW, ith_bw);
                        ith_bw++;
                        startService(bitIntent);
                    }
                }
                BITalinoFrame[] frames = bitalino.read(Constants.SAMPLING_RATE);

                for (BITalinoFrame frame : frames) {
                    //buffer[counter] untuk keperluan pengolahan data
                    double mV = convertmVolt(frame.getAnalog(selChannels[0]));
                    buffer[counter] = mV;

                    //bufmin untuk keperluan tampilan dan penyimpanan
                    double buffmin = mV;

                    Intent broadcasterIntent = new Intent();
                    //temp = temp + buffmin + ",";
                    if (fileWriterECG != null) fileWriterECG.writeDataToFile(buffmin);
                    broadcasterIntent.setAction(Constants.BROADCAST_BITALINO_SGLVALUE);
                    broadcasterIntent.putExtra(Constants.KEY_TEMPORARY_DATA, temp);
                    broadcasterIntent.putExtra(Constants.BITALINO_SGLVALUE, buffmin);
                    sendBroadcast(broadcasterIntent, null);
                    counter++;
                }
                isFirst = false;
                Log.i(TAG, "Intent Service Running");
            }
        } catch (Exception e) {
            Log.e(TAG, "There was an error.", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service Binded");
        ecgRecord = (EcgRecord) intent.getSerializableExtra("ECG_RECORD");
        startRecording();
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service Unbinded");
        stopRecording();
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        try {
            //sendTemporaryData(temp);
            isRunning = false;
            closeConnection();
            //mHandler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void startRecording() {
        db = new SQLiteHandler(getBaseContext());
        fileWriterECG = new FileWriterECG(ecgRecord.getFileUrl());
    }

    private void stopRecording() {
        db.addEcgRecord(ecgRecord.getUserId(), ecgRecord.getRecordingTime(), ecgRecord.getRecordingName(), ecgRecord.getFileUrl());
        db.close();
        checkExternalMedia();
        //writeToSDFile();
        if (fileWriterECG != null) fileWriterECG.stopWriting();
    }

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

    double Vcc = 3.3;
    int n = 10;
    double powN = Math.pow(2, n);
    int GECG = 1100;

    private double convertmVolt(int ECGB){
        double ECGV;
        ECGV = ((((ECGB/powN)-0.5)*Vcc)/GECG);
        return  ECGV * 1000;
    }
}
