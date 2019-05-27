package com.example.xiti_nganjuk_v2.interfaces;

import com.example.xiti_nganjuk_v2.models.PostCountModel;
import com.example.xiti_nganjuk_v2.models.Post_grid_model_class;
import com.example.xiti_nganjuk_v2.models.Post_model_class;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LoadPostInterface {
    @FormUrlEncoded
    @POST("load_post_count.php")
    Call<List<PostCountModel>> loadPostCount(
            @Field("userId") String userId
    );

    @FormUrlEncoded
    @POST("load_post_grid.php")
    Call<List<Post_grid_model_class>> loadPostForGrid(
            @Field("userId") String userId
    );

    @FormUrlEncoded
    @POST("load_detail_post.php")
    Call<Post_model_class> loadPostDetail(
            @Field("postId") String postId
    );

    @FormUrlEncoded
    @POST("load_post_home.php")
    Call<List<Post_model_class>> loadPostHome(
            @Field("userLatitude") String userLatitude,
            @Field("userLongitude") String userLongitude
    );
}
