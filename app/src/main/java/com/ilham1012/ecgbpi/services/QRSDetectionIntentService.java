package com.ilham1012.ecgbpi.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.ilham1012.ecgbpi.app.Constants;
import com.ilham1012.ecgbpi.qrsdetection.ZeroCrossing;


public class QRSDetectionIntentService extends IntentService {
    public static final String TAG = "[QRSDetectionIS]";
    private int samplingR;
    private Intent intent = new Intent();

    public QRSDetectionIntentService() {

        super(QRSDetectionIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(Constants.QRS_ACTION)) {
                handleQRSProcessing(intent);
            }
        }
    }

    private void handleQRSProcessing(Intent intent) {
        double[] buffer = intent.getDoubleArrayExtra(Constants.BITALINO60_BUFFER_DATA);
        samplingR = intent.getIntExtra("samplingR", 10);
        ZeroCrossing zeroCrossing = new ZeroCrossing(buffer, samplingR);
        zeroCrossing.doZeroCrossing();
        int ith = intent.getIntExtra(Constants.ITH_BUFFERWINDOW, 0);
        double[] arrayx = zeroCrossing.y7;
        Intent qrsResult = new Intent();
        qrsResult.setAction(Constants.BROADCAST_QRS_DETECTION_RESULT);
        qrsResult.putExtra(Constants.QRS_DETECTION_RESULT, arrayx);
        qrsResult.putExtra(Constants.ITH_QRS_RESULT, ith);
        for (int i = 0; i < arrayx.length; i++){
            Log.i(TAG, "Buffer Res "+ arrayx[i]);
        }
        sendBroadcast(qrsResult, null);
    }
}
