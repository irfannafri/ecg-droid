package com.ilham1012.ecgbpi.RetrofitInterface;

import retrofit.Callback;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by ilham1012 on 2/15/16.
 */
public interface FileUploadService {

    @Multipart
    @POST("/ekg_upload")
    void upload(@Part("file") TypedFile file,
                @Part("description") String description,
                Callback<String> cb);
}
