package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.UploadResultModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageUploadInterface {
    @Multipart
    @POST("upload_image.php")
    Call<UploadResultModel> uploadImageToServer(@Part MultipartBody.Part image);
}
