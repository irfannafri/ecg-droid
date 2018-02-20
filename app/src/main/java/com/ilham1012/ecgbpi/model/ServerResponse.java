package com.ilham1012.ecgbpi.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by firman on 19/02/18.
 */

public class ServerResponse {

    @SerializedName("success")
    boolean success;
    @SerializedName("message")
    String message;

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
