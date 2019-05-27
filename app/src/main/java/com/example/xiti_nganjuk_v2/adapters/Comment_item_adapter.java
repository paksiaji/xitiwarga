package com.example.xiti_nganjuk_v2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.models.Comment_model_class;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Comment_item_adapter extends RecyclerView.Adapter<Comment_item_adapter.ViewHolder> {

    List<Comment_model_class> listComment;
    Context mContext;

    public Comment_item_adapter(List<Comment_model_class> listComment, Context mContext) {
        this.listComment = listComment;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_item_row,viewGroup,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        //Load image profile
        LoadImage(listComment.get(i).getUserId(),viewHolder.imgProfile);
        //Load name profile
        LoadProfileName(listComment.get(i).getUserId(),viewHolder.txtProfileName);
        //Load comment text
        viewHolder.txtCommentText.setText(listComment.get(i).getCommentText());
        //Load comment time
        LoadTime(listComment.get(i).getTime(), viewHolder.txtTimePost);
    }

    @Override
    public int getItemCount() {
        return listComment.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView txtProfileName, txtCommentText, txtTimePost;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtProfileName = itemView.findViewById(R.id.txtProfileName);
            txtCommentText = itemView.findViewById(R.id.txtCommentText);
            txtTimePost = itemView.findViewById(R.id.txtTimePost);
        }
    }

    private void LoadImage(String userId, final ImageView imgProfile){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(userId);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if(!response.body().getProfilePic().equals("")){
                    Glide.with(mContext).load("https://xiti.apps.bentang.id/Images/"+response.body().getProfilePic()).into(imgProfile);
                }else{
                    imgProfile.setImageResource(R.drawable.empty_profile);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("Comment_item_adapter", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadProfileName(String userId, final TextView txtProfileName){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(userId);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                txtProfileName.setText(response.body().getFirstName()+" "+response.body().getLastName());
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("Comment_item_adapter", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadTime(String time,TextView txtTime){
        try {
            Date datePost = new Date(Long.parseLong(time) * 1000);
            Date dateNow  = new Date();
            long interval = dateNow.getTime() - datePost.getTime();
            if(TimeUnit.SECONDS.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                if(TimeUnit.MINUTES.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                    if(TimeUnit.HOURS.convert(interval,TimeUnit.MILLISECONDS) >= 24){
                        txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " Hari Yang Lalu");
                    }else{
                        txtTime.setText(String.valueOf(TimeUnit.HOURS.convert(interval,TimeUnit.MILLISECONDS))+ " Jam Yang Lalu");
                    }
                }else{
                    txtTime.setText(String.valueOf(TimeUnit.MINUTES.convert(interval,TimeUnit.MILLISECONDS))+ " Menit Yang Lalu");
                }
            }else{
                txtTime.setText(String.valueOf(TimeUnit.SECONDS.convert(interval,TimeUnit.MILLISECONDS))+ " Detik Yang Lalu");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
