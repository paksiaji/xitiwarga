package com.example.xiti_nganjuk_v2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadPostInterface;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.models.Post_model_class;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailPostActivity extends AppCompatActivity {
    CircleImageView imgProfile;
    ImageView imgContent,imgPlay,imgCategory,imgStatus, imgBack, imgComment, imgLike;
    TextView txtProfileName,txtLocation,txtTime,txtTitle,txtDescription,txtLikesCount,txtCommentCount;
    Button btnOpenInGoogleMaps, btnSendMessage;
    DatabaseReference ref;
    Map<String,String> data = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_post);
        initComponent();
        LoadDetailData();
    }

    private void initComponent(){
        imgBack = findViewById(R.id.imgBack);
        imgProfile = findViewById(R.id.imgProfile);
        imgContent = findViewById(R.id.imgContent);
        imgPlay = findViewById(R.id.imgPlay);
        imgCategory = findViewById(R.id.imgCategory);
        imgStatus = findViewById(R.id.imgStatus);
        imgComment = findViewById(R.id.imgComment);
        imgLike = findViewById(R.id.imgLike);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtLocation = findViewById(R.id.txtLocation);
        txtTime = findViewById(R.id.txtTime);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtLikesCount = findViewById(R.id.txtLikesCount);
        txtCommentCount = findViewById(R.id.txtCommentCount);
        btnOpenInGoogleMaps = findViewById(R.id.btnOpenGoogleMaps);
        btnSendMessage = findViewById(R.id.btnSendMessage);
    }

    private void initComponentMethod(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Likes").child(getIntent().getStringExtra("postId"));
                if(imgLike.getTag().equals("Like")){
                    ref.child(auth.getCurrentUser().getUid()).setValue(true);
                    imgLike.setTag("Liked");
                }else{
                    ref.child(auth.getCurrentUser().getUid()).removeValue();
                    imgLike.setTag("Like");
                }
                isLiked(auth.getCurrentUser().getUid(),getIntent().getStringExtra("postId"),imgLike);
            }
        });

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgPlay.getVisibility() == View.VISIBLE){
                    Intent intent = new Intent(DetailPostActivity.this, VideoPreviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FileLocation",imgPlay.getTag().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        imgContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgPlay.getVisibility() == View.VISIBLE){
                    Intent intent = new Intent(DetailPostActivity.this, VideoPreviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("FileLocation",imgPlay.getTag().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailPostActivity.this,CommentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putString("postId",getIntent().getStringExtra("postId"));
                bundle.putString("userPostId",data.get("userId"));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnOpenInGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] location = txtLocation.getTag().toString().split(",");
                String uri = "google.navigation:q="+location[0]+","+location[1];
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailPostActivity.this,ChatRoomActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userId",txtProfileName.getTag().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void LoadDetailData(){
        final ProgressDialog dialog = new ProgressDialog(DetailPostActivity.this);
        dialog.setMessage("Memuat Data...");
        dialog.show();
        dialog.setCancelable(false);
        String postId = getIntent().getStringExtra("postId");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadPostInterface loadPostInterface = retrofit.create(LoadPostInterface.class);
        Call<Post_model_class> call = loadPostInterface.loadPostDetail(postId);
        call.enqueue(new Callback<Post_model_class>() {
            @Override
            public void onResponse(Call<Post_model_class> call, Response<Post_model_class> response) {
                Post_model_class model = response.body();
                data.put("category",model.getCategory());
                data.put("description",model.getDescription());
                data.put("latitude",String.valueOf(model.getLatitude()));
                data.put("longitude",String.valueOf(model.getLongitude()));
                data.put("progress",model.getProgress());
                data.put("timePost",String.valueOf(model.getTimePost()));
                data.put("title",model.getTitle());
                data.put("typePost",model.getType());
                data.put("urlContent",model.getUrlContent());
                data.put("userId",model.getUserId());

                LoadUserProfile(data,dialog);
            }

            @Override
            public void onFailure(Call<Post_model_class> call, Throwable t) {
                dialog.dismiss();
                Log.e("DetailPostActivity", "onFailure: "+t.toString() );
            }
        });
    }

    private void LoadUserProfile(final Map<String,String> data, final ProgressDialog dialog){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(data.get("userId"));

        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                UserModel model = response.body();
                txtProfileName.setText(model.getFirstName()+" "+model.getLastName());
                txtProfileName.setTag(data.get("userId"));
                if(!model.getProfilePic().equals("")){
                    Glide.with(DetailPostActivity.this).load("https://xiti.apps.bentang.id/Images/"+model.getProfilePic()).into(imgProfile);
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();
                if(data.get("userId").equals(auth.getCurrentUser().getUid())){
                    btnSendMessage.setEnabled(false);
                    btnSendMessage.setClickable(false);
                }
                LoadPostDetail(data,dialog);
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                dialog.dismiss();
                Log.e("DetailPostActivity", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadPostDetail(final Map<String,String> data, final ProgressDialog dialog){
        // Show / Hide Play Button
        if(data.get("typePost").equals("Gambar")){
            imgPlay.setVisibility(View.INVISIBLE);
        }else{
            imgPlay.setVisibility(View.VISIBLE);
        }

        //determine category
        switch (data.get("category")){
            case "kdrt" :
                imgCategory.setImageResource(R.drawable.kekerasan);
                break;
            case "pohon tumbang" :
                imgCategory.setImageResource(R.drawable.pohon_tumbang);
                break;
            case "jalan rusak" :
                imgCategory.setImageResource(R.drawable.jalan_rusak);
                break;
            case "bencana alam" :
                imgCategory.setImageResource(R.drawable.bencana_alam);
                break;
            case "kriminalitas" :
                imgCategory.setImageResource(R.drawable.kriminalitas);
                break;
            case "kebakaran" :
                imgCategory.setImageResource(R.drawable.kebakaran);
                break;
        }

        //determine status
        switch (data.get("progress")) {
            case "pending" :
                imgStatus.setImageResource(R.drawable.pending);
                break;
            case "process" :
                imgStatus.setImageResource(R.drawable.process);
                break;
            case "complete" :
                imgStatus.setImageResource(R.drawable.finish);
                break;
        }

        //Get Location
        Geocoder geocoder = new Geocoder(DetailPostActivity.this, Locale.getDefault());
        try {
            Double latitude = Double.valueOf(data.get("latitude"));
            Double longitude = Double.valueOf(data.get("longitude"));
            List<Address> addresses = geocoder.getFromLocation(latitude,longitude,1);
            String addressLine = addresses.get(0).getAddressLine(0);
            String cityName = addressLine.split(",")[2];
            txtLocation.setText(cityName.trim());
            txtLocation.setTag(data.get("latitude")+","+data.get("longitude"));
        } catch (Exception e) {
            e.printStackTrace();
            txtLocation.setText("-");
            txtLocation.setTag(data.get("latitude")+","+data.get("longitude"));
        }

        //Determine Time
        try {
            Date datePost = new Date(Long.parseLong(data.get("timePost")) * 1000);
            Date dateNow  = new Date();
            long interval = dateNow.getTime() - datePost.getTime();
            if(TimeUnit.SECONDS.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                if(TimeUnit.MINUTES.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                    if(TimeUnit.HOURS.convert(interval,TimeUnit.MILLISECONDS) >= 24){
                        txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " Hari Yang lalu");
                    }else{
                        txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " Jam Yang lalu");
                    }
                }else{
                    txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " Menit Yang lalu");
                }
            }else{
                txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " Detik Yang lalu");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get Title
        txtTitle.setText(data.get("title"));

        // Get Description
        txtDescription.setText(data.get("description"));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        isLiked(auth.getCurrentUser().getUid(),getIntent().getStringExtra("postId"),imgLike);

        //Count total Likes
        ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Likes").child(getIntent().getStringExtra("postId")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtLikesCount.setText(dataSnapshot.getChildrenCount()+" Suka");
                ref = FirebaseDatabase.getInstance().getReference();
                ref.child("Comments").child(getIntent().getStringExtra("postId")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        txtCommentCount.setText(dataSnapshot.getChildrenCount()+" Komentar");
                        Glide.with(DetailPostActivity.this).load(data.get("urlContent")).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                dialog.dismiss();
                                imgPlay.setTag(data.get("urlContent"));
                                initComponentMethod();
                                return false;
                            }
                        }).into(imgContent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
