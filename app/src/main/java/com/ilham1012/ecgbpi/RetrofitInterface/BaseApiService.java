package com.ilham1012.ecgbpi.RetrofitInterface;

import android.database.Observable;

import com.ilham1012.ecgbpi.model.MyResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit.mime.TypedFile;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by firman on 19/02/18.
 */

public interface BaseApiService {


    String BASE_URL = "http://192.168.43.68/ecgbpi/api/";


    @Multipart
    @POST("Api.php?apicall=upload")
    Call<MyResponse> uploadFile(@Part("files\"; filename=\"myfile.json\" ") RequestBody file_url,
                                @Part("recording_name") RequestBody recording_name,
                                @Part("user_id") RequestBody user_id);


}
