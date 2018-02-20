package com.ilham1012.ecgbpi.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ilham1012.ecgbpi.R;
import com.ilham1012.ecgbpi.RetrofitInterface.BaseApiService;
import com.ilham1012.ecgbpi.RetrofitInterface.FileUploadService;
import com.ilham1012.ecgbpi.app.AppConfig;
import com.ilham1012.ecgbpi.app.RetrofitClient;
import com.ilham1012.ecgbpi.app.UtilsApi;
import com.ilham1012.ecgbpi.model.MyResponse;
import com.ilham1012.ecgbpi.model.ServerResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by firman on 19/02/18.
 */

public class UploadActivity extends AppCompatActivity {

    private EditText inputRecordName, inputUserId;
    private Button btnUpload;
    String mediaPath;
    public int PICKFILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        inputRecordName = (EditText) findViewById(R.id.etRecordingName);
        inputUserId = (EditText) findViewById(R.id.etUserId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            finish();
            startActivity(intent);
            return;
        }

        findViewById(R.id.btnSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //opening file chooser
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*files/*");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //the image URI
            Uri selectedImage = data.getData();

            //calling the upload file method after choosing the file
            uploadFile(selectedImage);
        }
    }

    private void uploadFile(Uri fileUri) {

        //creating a file
        File file = new File(getRealPathFromURI(fileUri));
        //creating request body for file
        RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)), file);
        RequestBody recording_name = RequestBody.create(MediaType.parse("text/plain"), inputRecordName.getText().toString());
        RequestBody user_id = RequestBody.create(MediaType.parse("text/plain"), inputUserId.getText().toString());
        //The gson builder
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        //creating retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //creating our api
        BaseApiService api = retrofit.create(BaseApiService.class);

        //creating a call and calling the upload image method
        Call<MyResponse> call = api.uploadFile(requestFile, recording_name, user_id);

        //finally performing the call
        call.enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (!response.body().error) {
                    Toast.makeText(getApplicationContext(), "File Uploaded Successfully...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred...", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    /*
    * This method is fetching the absolute path of the image file
    * if you want to upload other kind of files like .pdf, .docx
    * you need to make changes on this method only
    * Rest part will be the same
    * */
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Files.FileColumns.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
