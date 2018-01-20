package com.ilham1012.ecgbpi.app;

import com.ilham1012.ecgbpi.activity.MyPreferencesActivity;

public class Constants {

    // SETTINGS CONSTANTS
    public static final String KEY_SETTINGS_TYPE = "typeOfSetting";
    public static final String KEY_SETTINGS_DRAW_STATE = "stateOfDraw";

    // IO CONSTANTS
    public static final String TEMP_FILE = "tmp.txt";
    public static final String TEXT_FILE_EXTENTION = ".txt";
    public static final String ZIP_FILE_EXTENTION = ".zip";
    public static final String APP_DIRECTORY = "/Bioplux/";

    //ACTION CONSTANTS
    public static final String QRS_ACTION = "QRS_ACTION";
    public static final String BROADCAST_BITALINO_SGLVALUE = "BITBROADCAST_VALUE_ACTION";
    public static final String BROADCAST_TEMPORARY_DATA = "BITBROADCAST_TEMP_DATA";
    public static final String BROADCAST_QRS_DETECTION_RESULT = "BITBROADCAST_QRS_RESULT";


    //INTENT MESSAGE
    public static final String BITALINO60_BUFFER_DATA = "BUFFER_DATA";
    public static final String BITALINO_SGLVALUE = "BITBROADCAST_VALUE";
    public static final String KEY_TEMPORARY_DATA = "KEY_TEMPORARY_DATA";
    public static final String QRS_DETECTION_RESULT = "QRS_DETECTION_RESULT_DATA";
    public static final String ITH_QRS_RESULT = "ITH_QRS_DETECTION_RESULT";

    //RATE
    public static final Integer WINDOW_SIZE = 60;
    public static final String ITH_BUFFERWINDOW = "ITH_BUFFERWINDOW";

}
