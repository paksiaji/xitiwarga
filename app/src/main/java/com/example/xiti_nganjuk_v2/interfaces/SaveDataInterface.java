package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.SaveDataResultModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SaveDataInterface {
    @FormUrlEncoded
    @POST("input_data.php")
    Call<SaveDataResultModel> saveDataToServer(
            @Field("postId") String postId,
            @Field("category") String category,
            @Field("description") String description,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("progress") String progress,
            @Field("timePost") String timePost,
            @Field("title") String title,
            @Field("typePost") String typePost,
            @Field("urlContent") String urlContent,
            @Field("userId") String userId
            );
}
