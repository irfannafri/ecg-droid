package com.ilham1012.ecgbpi.helper;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Hafid on 11/25/2017.
 */

public class FileWriterECG {
    public static String TAG = "[FileWriterECG]";

    private File root, dir, file;
    private int count;
    FileOutputStream f;
    PrintWriter pw;

    public FileWriterECG(String UriFile) {
        try {
            count = 0;
            File root = android.os.Environment.getExternalStorageDirectory();
            Log.i(TAG, "External file system root: " + root);

            // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

            File dir = new File(root.getAbsolutePath() + "/ecgbpi/ecgrecord/");
            dir.mkdirs();
            file = new File(dir, UriFile);

            f = new FileOutputStream(file);
            pw = new PrintWriter(f);
            pw.print("[");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            f = null;
            pw = null;
        }
    }

    public void writeDataToFile(double data) {
        pw.print(data + ",");
    }

    public void stopWriting() {
        try {
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
    }
}
