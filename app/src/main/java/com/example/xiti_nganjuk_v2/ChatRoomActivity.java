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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiti_nganjuk_v2.adapters.Chat_item_adapter;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.interfaces.MessageNotificationInterface;
import com.example.xiti_nganjuk_v2.models.Chat_item_class;
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

public class ChatRoomActivity extends AppCompatActivity {
    RecyclerView rvChat;
    EditText txtMessage;
    CircleImageView imgProfilePic;
    TextView txtProfileName;
    ImageView imgSend, imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        rvChat = findViewById(R.id.rvChat);
        txtMessage = findViewById(R.id.txtMessage);
        imgProfilePic = findViewById(R.id.imgProfile);
        txtProfileName = findViewById(R.id.txtProfileName);
        imgBack = findViewById(R.id.imgBack);
        imgSend = findViewById(R.id.imgSend);
        LoadProfile();
        LoadChat();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txtMessage.getText().toString().equals("")){
                    Date date = new Date();
                    final FirebaseAuth auth = FirebaseAuth.getInstance();
                    String messageText = txtMessage.getText().toString();
                    String messageTime = String.valueOf(date.getTime() / 1000);
                    String messageSender = auth.getCurrentUser().getUid();
                    String messageReceiver = getIntent().getStringExtra("userId");

                    final HashMap<String,Object> chat = new HashMap<>();
                    chat.put("messageText",messageText);
                    chat.put("messageTime",messageTime);
                    chat.put("messageSender",messageSender);
                    chat.put("messageReceiver",messageReceiver);
                    final ProgressDialog dialog = new ProgressDialog(ChatRoomActivity.this);
                    dialog.setMessage("Mengirim Pesan...");
                    dialog.setCancelable(false);
                    dialog.show();
                    DatabaseReference refSender = FirebaseDatabase.getInstance().getReference().child("Chats").child(auth.getCurrentUser().getUid()).child(getIntent().getStringExtra("userId"));
                    refSender.push().setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference refReceiver = FirebaseDatabase.getInstance().getReference().child("Chats").child(getIntent().getStringExtra("userId")).child(auth.getCurrentUser().getUid());
                            refReceiver.push().setValue(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    txtMessage.setText("");
                                    dialog.dismiss();
                                    Toast.makeText(ChatRoomActivity.this, "Berhasil Mengirim Pesan", Toast.LENGTH_SHORT).show();
                                    sendNotification();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(ChatRoomActivity.this, "Gagal Mengirim Pesan", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void sendNotification(){
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JSONObject body = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        try {
            body.put("to","/topics/"+getIntent().getStringExtra("userId"));
            data.put("userId",auth.getCurrentUser().getUid());
            data.put("text","Anda Mempunyai Pesan Baru Dari "+txtProfileName.getText().toString());
            data.put("title","Pesan Baru");
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

    private void LoadProfile(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(getIntent().getStringExtra("userId"));
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                txtProfileName.setText(response.body().getFirstName()+" "+response.body().getLastName());
                if(!response.body().getProfilePic().equals(response.body().getProfilePic())) {
                    Glide.with(ChatRoomActivity.this).load("https://xiti.apps.bentang.id/Images/"+response.body().getProfilePic());
                }else{
                    imgProfilePic.setImageResource(R.drawable.empty_profile);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("ChatRoomActivity", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadChat(){
        final List<Chat_item_class> listChat = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats").child(auth.getCurrentUser().getUid()).child(getIntent().getStringExtra("userId"));
        ref.orderByChild("messageTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String messageId = snapshot.getKey();
                    String messageText = snapshot.child("messageText").getValue().toString();
                    String messageTime = snapshot.child("messageTime").getValue().toString();
                    String messageSender = snapshot.child("messageSender").getValue().toString();
                    String messageReceiver = snapshot.child("messageReceiver").getValue().toString();

                    Chat_item_class chat_item_class = new Chat_item_class(messageId,messageText,messageTime,messageSender,messageReceiver);
                    listChat.add(chat_item_class);
                }

                Chat_item_adapter adapter = new Chat_item_adapter(listChat,ChatRoomActivity.this);
                LinearLayoutManager manager = new LinearLayoutManager(ChatRoomActivity.this);
                rvChat.setLayoutManager(manager);
                rvChat.setAdapter(adapter);
                rvChat.scrollToPosition(listChat.size()-1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
