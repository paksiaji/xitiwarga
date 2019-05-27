package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.UserModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LoadUserDataInterface {
    @FormUrlEncoded
    @POST("load_user_data.php")
    Call<UserModel> getUserData(
            @Field("userId") String userId
    );
}
