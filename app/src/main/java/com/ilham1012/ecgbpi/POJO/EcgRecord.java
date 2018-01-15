package com.ilham1012.ecgbpi.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by ilham on 12/02/2016.
 */
public class EcgRecord implements Serializable {
    int recording_id;
    private int user_id;
    private String recording_time;
    private String recording_name;
    private String file_url;

    public EcgRecord(){

    }

    public EcgRecord(int recording_id, int user_id, String recording_time, String recording_name, String file_url){
        this.recording_id = recording_id;
        this.user_id = user_id;
        this.recording_time = recording_time;
        this.recording_name = recording_name;
        this.file_url = file_url;
    }

    public void setUserId(int user_id){
        this.user_id = user_id;
    }

    public int getUserId(){
        return this.user_id;
    }

    public void setRecordingTime(String recording_time){
        this.recording_time = recording_time;
    }

    public String getRecordingTime(){
        return this.recording_time;
    }

    public void setRecordingName(String recording_name){
        this.recording_name = recording_name;
    }

    public String getRecordingName(){
        return this.recording_name;
    }

    public void setFileUrl(String file_url){
        this.file_url = file_url;
    }

    public String getFileUrl(){
        return this.file_url;
    }
}
