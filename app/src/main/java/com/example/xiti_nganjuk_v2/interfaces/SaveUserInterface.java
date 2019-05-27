package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.SaveUserDataResultModel;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SaveUserInterface {
    @FormUrlEncoded
    @POST("register_user.php")
    Call<SaveUserDataResultModel> saveUserData(
            @Field("userId") String userId,
            @Field("birthDate") String birdDate,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("profilePic") String profilePic,
            @Field("gender") String gender
    );

    @FormUrlEncoded
    @POST("update_profile.php")
    Call<SaveUserDataResultModel> updateUserData(
            @Field("userId") String userId,
            @Field("birthDate") String birdDate,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("profilePic") String profilePic,
            @Field("gender") String gender
    );
}
