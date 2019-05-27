package com.example.xiti_nganjuk_v2;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.adapters.Comment_item_adapter;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.MessageNotificationInterface;
import com.example.xiti_nganjuk_v2.models.Comment_model_class;
import com.example.xiti_nganjuk_v2.models.NotificationResponseModell;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CommentActivity extends AppCompatActivity {
    RecyclerView rvComment;
    List<Comment_model_class> listComment;
    CircleImageView imgComentatotProfile;
    EditText etComment;
    ImageView imgSend, imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        rvComment = findViewById(R.id.rvComments);
        imgComentatotProfile = findViewById(R.id.imgCommentatorProfile);
        etComment = findViewById(R.id.etComment);
        imgSend = findViewById(R.id.imgSend);
        imgBack = findViewById(R.id.imgBack);
        listComment = new ArrayList<>();
        LoadCommentatorProfile();
        LoadComments(getIntent().getStringExtra("postId"));

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                final FirebaseAuth auth = FirebaseAuth.getInstance();
                if(!etComment.getText().toString().equals("")){
                    String userId = auth.getCurrentUser().getUid();
                    String commentText = etComment.getText().toString();
                    String time = String.valueOf(date.getTime() / 1000);

                    HashMap<String,Object> comment = new HashMap<>();
                    comment.put("userId",userId);
                    comment.put("commentText",commentText);
                    comment.put("time",time);
                    final ProgressDialog dialog = new ProgressDialog(CommentActivity.this);
                    dialog.setMessage("Mengirim Komentar....");
                    dialog.show();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(getIntent().getStringExtra("postId"));
                    ref.push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            etComment.setText("");
                            dialog.dismiss();
                            Toast.makeText(CommentActivity.this, "Berhasil Mengirim Komentar", Toast.LENGTH_SHORT).show();
                            if(!getIntent().getStringExtra("userPostId").equals(auth.getCurrentUser().getUid())){
                                sendNotification();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(CommentActivity.this, "Gagal Mengirim Komentar", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    etComment.setError("Isikan komentar anda sebelum mengirim");
                }
            }
        });
    }

    private void LoadCommentatorProfile(){
        final ProgressDialog dialog = new ProgressDialog(CommentActivity.this);
        dialog.setMessage("Memuat Profil Anda...");
        dialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(auth.getCurrentUser().getUid());
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if(response.body().getProfilePic().equals("")){
                    Log.d("Profile Pic", "Kosong");
                    imgComentatotProfile.setImageResource(R.drawable.empty_profile);
                }else{
                    Glide.with(CommentActivity.this).load("https://xiti.apps.bentang.id/Images/"+response.body().getProfilePic()).into(imgComentatotProfile);
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                dialog.dismiss();
            }
        });
    }

    private void sendNotification(){
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);

        Call<UserModel> call = loadUserDataInterface.getUserData(auth.getCurrentUser().getUid());
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                String username = response.body().getFirstName()+" "+response.body().getLastName();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://fcm.googleapis.com")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                JSONObject body = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject notification = new JSONObject();
                try {
                    body.put("to","/topics/"+getIntent().getStringExtra("userPostId"));
                    data.put("postId",getIntent().getStringExtra("postId"));
                    data.put("text",username+" Mengomentari Laporan Anda");
                    data.put("title","Komentar Baru");
                    body.put("data",data);
                    notification.put("title","Xiti Apps");
                    notification.put("text","Anda Mempunyai Pesan Baru");
                    body.put("notification",notification);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MessageNotificationInterface messageNotification = retrofit.create(MessageNotificationInterface.class);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),body.toString());
                Call<NotificationResponseModell> call1 = messageNotification.sendNotification(requestBody);
                call1.enqueue(new Callback<NotificationResponseModell>() {
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

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("CommentActivity", "onFailure: "+t.toString() );
            }
        });
    }

    private void LoadComments(String postId){
        final ProgressDialog dialog = new ProgressDialog(CommentActivity.this);
        dialog.setMessage("Memuat Komentar...");
        dialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listComment.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String commentId = snapshot.getKey();
                    Log.d("Comment Id", commentId);
                    String commentText = snapshot.child("commentText").getValue().toString();
                    String userId = snapshot.child("userId").getValue().toString();
                    String time = snapshot.child("time").getValue().toString();

                    Comment_model_class comment = new Comment_model_class(commentId,commentText,userId,time);
                    listComment.add(comment);
                }

                dialog.dismiss();
                Comment_item_adapter adapter = new Comment_item_adapter(listComment,CommentActivity.this);
                LinearLayoutManager manager = new LinearLayoutManager(CommentActivity.this);
                manager.setStackFromEnd(true);
                manager.setReverseLayout(true);
                rvComment.setLayoutManager(manager);
                rvComment.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
