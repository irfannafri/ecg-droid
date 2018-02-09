package com.ilham1012.ecgbpi.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.ilham1012.ecgbpi.R;
import com.jjoe64.graphview.Viewport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class DisplaySignalActivity extends AppCompatActivity {
    public static final String TAG = "[DisplaySignalActivity]";
    CombinedChart rawChart;
    String recordingName;
    LineDataProvider dataProvider;
    ILineDataSet dataSet;
    float fillMin = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_signal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        recordingName = extras.getString("recording_name");

        rawChart = (CombinedChart) findViewById(R.id.rawChart);
        plot();
    }


    public void plot() {
        JSONArray jArray, jArrayP;
        try {
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ecgbpi/ecgrecord/");

            File yourFile = new File(dir, recordingName + ".json");
            FileInputStream stream = new FileInputStream(yourFile);
            String jString = null;
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                /* Instead of using default, pass in a decoder. */
            jString = Charset.defaultCharset().decode(bb).toString();
            stream.close();
            jArray = new JSONArray(jString);
            float chartMaxY = dataProvider.getYChartMax();
            float chartMinY = dataProvider.getYChartMin();
            File yourFileP = new File(dir, "p_" + recordingName + ".json");
            FileInputStream streamP = new FileInputStream(yourFileP);
            String jStringP = null;
            FileChannel fcP = streamP.getChannel();
            MappedByteBuffer bbP = fcP.map(FileChannel.MapMode.READ_ONLY, 0, fcP.size());
                /* Instead of using default, pass in a decoder. */
            jStringP = Charset.defaultCharset().decode(bbP).toString();
            streamP.close();
            Log.i(TAG, jStringP);
            jArrayP = new JSONArray(jStringP);
            LineData dataLine = dataProvider.getLineData();
            List<Entry> entries = new ArrayList<>();
            List<Entry> entriesP = new ArrayList<>();

            if (jArray != null) {
                for (int i = 0; i < jArray.length() - 1; i++) {
                    entries.add(new Entry(i, (float) jArray.getDouble(i)));
                }
            }

            if (jArrayP != null) {
                for (int i = 0; i < jArrayP.length(); i++) {
                    int x = jArrayP.getInt(i);
                    if (x < jArray.length() - 1) {

                        if (dataSet.getYMax() > 0 && dataSet.getYMin() < 0) {
                            fillMin = 0;
                        } else {
                            float y = (float) jArray.getDouble(x);
                            entriesP.add(new Entry(x, y));
                            float max, min;
                            if (dataLine.getYMax() > 0)
                                max = 0f;
                            else
                                max = chartMaxY;
                            if (dataLine.getYMin() < 0)
                                min = 220f;
                            else
                                min = chartMinY;
                           fillMin = dataSet.getYMin() >= 0 ? min : max;
                        }
                    }
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "ECG");
            dataSet.setDrawValues(false);
            dataSet.setDrawCircles(false);
            dataSet.setColor(Color.MAGENTA);

            ScatterDataSet dataSetP = new ScatterDataSet(entriesP, "Peak");
            dataSetP.setDrawValues(false);
            dataSetP.setColor(Color.BLUE);

            CombinedData data = new CombinedData();

            LineData lineData = new LineData(dataSet);
            ScatterData scatterData = new ScatterData(dataSetP);
            data.setData(lineData);
            data.setData(scatterData);

            rawChart.setData(data);
            rawChart.invalidate();
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
