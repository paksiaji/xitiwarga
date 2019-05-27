package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.UploadResultModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface VideoUploadInterface {
    @Multipart
    @POST("upload_video.php")
    Call<UploadResultModel> uploadVideoToServer(@Part MultipartBody.Part video);
}
