package com.example.xiti_nganjuk_v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.xiti_nganjuk_v2.CommentActivity;
import com.example.xiti_nganjuk_v2.DetailPostActivity;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.MessageNotificationInterface;
import com.example.xiti_nganjuk_v2.models.NotificationResponseModell;
import com.example.xiti_nganjuk_v2.models.Post_model_class;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.ViewProfileActivity;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Post_item_adapter extends RecyclerView.Adapter<Post_item_adapter.ViewHolder> {
    private List<Post_model_class> listPostModel;
    private Context mContext;

    public Post_item_adapter(List<Post_model_class> listModel, Context mContext) {
        this.listPostModel = listModel;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Post_item_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item_row,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final Post_item_adapter.ViewHolder viewHolder, final int i) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        //Loading the content
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.ic_sync_black_24dp);
        Glide.with(mContext).load(listPostModel.get(i).getUrlContent()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).apply(options).into(viewHolder.imgContent);
        //Loading the title or hide it if the title is null
        if(listPostModel.get(i).getTitle().equals("")){
            viewHolder.txtPostTitle.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.txtPostTitle.setVisibility(View.VISIBLE);
            viewHolder.txtPostTitle.setText(listPostModel.get(i).getTitle());
        }
        //Loading Username and Post Time
        LoadUserName(listPostModel.get(i).getUserId(),viewHolder.txtName);
        //Load time post
        LoadTime(listPostModel.get(i).getTimePost(),viewHolder.txtTime);
        //Loading the city location
        getCityLocation(listPostModel.get(i).getLatitude(),listPostModel.get(i).getLongitude(),viewHolder.txtLocation);
        //Loading the description or hide it if the description is null
        if(listPostModel.get(i).getDescription().equals("")){
            viewHolder.txtDescription.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.txtDescription.setVisibility(View.VISIBLE);
            viewHolder.txtDescription.setText(listPostModel.get(i).getDescription());
        }
        //Loading Like status
        isLiked(auth.getCurrentUser().getUid(),listPostModel.get(i).getPostId(),viewHolder.imgLike);

        //Loading Status
        String status = listPostModel.get(i).getProgress();
        if(status.equals("Pending")){
            viewHolder.imgStatus.setImageResource(R.drawable.pending);
        }else if(status.equals("Process")){
            viewHolder.imgStatus.setImageResource(R.drawable.process);
        }else{
            viewHolder.imgStatus.setImageResource(R.drawable.finish);
        }

        // Show / hide play button depending on post type
        if(listPostModel.get(i).getType().equals("Gambar")){
            viewHolder.imgPlay.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.imgPlay.setVisibility(View.VISIBLE);
        }

        //Loading Category
        String category = listPostModel.get(i).getCategory();
        switch (category){
            case "kdrt" :
                viewHolder.imgCategory.setImageResource(R.drawable.kekerasan);
                break;
            case "pohon tumbang" :
                viewHolder.imgCategory.setImageResource(R.drawable.pohon_tumbang);
                break;
            case "jalan rusak" :
                viewHolder.imgCategory.setImageResource(R.drawable.jalan_rusak);
                break;
            case "bencana_alam" :
                viewHolder.imgCategory.setImageResource(R.drawable.bencana_alam);
                break;
            case "kriminalitas" :
                viewHolder.imgCategory.setImageResource(R.drawable.kriminalitas);
                break;
            case "kebakaran" :
                viewHolder.imgCategory.setImageResource(R.drawable.kebakaran);
                break;
        }

        viewHolder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("User Id", listPostModel.get(i).getUserId());
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Log.d("Current User Id ",auth.getCurrentUser().getUid());
                if(!listPostModel.get(i).getUserId().equals(auth.getCurrentUser().getUid())){
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId",listPostModel.get(i).getUserId());
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

        viewHolder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Likes").child(listPostModel.get(i).getPostId());
                if(viewHolder.imgLike.getTag().equals("Like")){
                    ref.child(auth.getCurrentUser().getUid()).setValue(true);
                    viewHolder.imgLike.setTag("Liked");
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
                    Call<UserModel> call = loadUserDataInterface.getUserData(auth.getCurrentUser().getUid());
                    call.enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            if(!auth.getCurrentUser().getUid().equals(listPostModel.get(i).getUserId())){
                                String username = response.body().getFirstName()+" "+response.body().getLastName();
                                sendNotification(username,listPostModel.get(i).getUserId());
                            }
                        }

                        @Override
                        public void onFailure(Call<UserModel> call, Throwable t) {
                            Log.e("Post_item_adapter", "onFailure: "+t.toString());
                        }
                    });
                }else{
                    ref.child(auth.getCurrentUser().getUid()).removeValue();
                    viewHolder.imgLike.setTag("Like");
                }
            }
        });


        viewHolder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId",listPostModel.get(i).getPostId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString("postId",listPostModel.get(i).getPostId());
                bundle.putString("userPostId",listPostModel.get(i).getUserId());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });



        viewHolder.imgContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("postId",listPostModel.get(i).getPostId());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        viewHolder.imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,DetailPostActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("postId",listPostModel.get(i).getPostId());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPostModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout rowContainer;
        TextView txtPostTitle, txtName, txtLocation, txtDescription, txtTime;
        ImageView imgContent, imgLike, imgComment, imgShare, imgStatus, imgCategory, imgPlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainer);

            txtPostTitle = itemView.findViewById(R.id.txtPostTitle);
            txtName = itemView.findViewById(R.id.txtName);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtDescription = itemView.findViewById(R.id.txtDesc);
            txtTime = itemView.findViewById(R.id.txtTime);

            imgContent = itemView.findViewById(R.id.imgPost);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgShare = itemView.findViewById(R.id.imgShare);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            imgPlay = itemView.findViewById(R.id.imgPlay);
        }
    }

    private void sendNotification(String username,String userId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JSONObject body = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            body.put("to","/topics/"+userId);
            data.put("text",username+" Menyukai Laporan Anda");
            data.put("title","Suka");
            body.put("data",data);
            notification.put("title","Xiti Apps");
            notification.put("text","Anda Mempunyai Pesan Baru");
            body.put("notification",notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MessageNotificationInterface messageNotification = retrofit.create(MessageNotificationInterface.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),body.toString());
        Call<NotificationResponseModell> call = messageNotification.sendNotification(requestBody);
        call.enqueue(new Callback<NotificationResponseModell>() {
            @Override
            public void onResponse(Call<NotificationResponseModell> call, Response<NotificationResponseModell> response) {
                Log.i("ChatRoomActivity", "Berhasil Mengirim Pesan: "+response.body().getMessage_id());
            }

            @Override
            public void onFailure(Call<NotificationResponseModell> call, Throwable t) {
                Log.e("ChatRoomActivity", "Gagal Send Notifikasi: "+t.toString());
            }
        });
    }

    private void LoadUserName(final String userId, final TextView txtName){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(userId);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                txtName.setTag(userId);
                txtName.setText(response.body().getFirstName()+" "+response.body().getLastName());
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("PostItemAdapter", "onFailure: "+t.toString());
            }
        });
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                txtName.setTag(userId);
//               txtName.setText(dataSnapshot.child("FirstName").getValue().toString()+" "+dataSnapshot.child("LastName").getValue().toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private void LoadTime(String time, TextView txtTime){
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

    private void getCityLocation(double latitude,double longitude,TextView txtLocation){
        Geocoder geocoder = new Geocoder(mContext,Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            String addressLine = addresses.get(0).getAddressLine(0);
            String cityName = addressLine.split(",")[2];
            txtLocation.setText(cityName);
        } catch (Exception e) {
            txtLocation.setText("-");
            e.printStackTrace();
        }
    }

    private void isLiked(final String userId, String postId, final ImageView imgLike){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists()){
                    imgLike.setImageResource(R.drawable.liked);
                    imgLike.setTag("Liked");
                }else{
                    imgLike.setImageResource(R.drawable.unliked);
                    imgLike.setTag("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
