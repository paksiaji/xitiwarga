package com.example.xiti_nganjuk_v2.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xiti_nganjuk_v2.ChatRoomActivity;
import com.example.xiti_nganjuk_v2.R;
import com.example.xiti_nganjuk_v2.constants.Constants;
import com.example.xiti_nganjuk_v2.interfaces.LoadUserDataInterface;
import com.example.xiti_nganjuk_v2.models.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Chat_list_adapter extends RecyclerView.Adapter<Chat_list_adapter.ViewHolder>{
    List<String> listId;
    Context mContext;

    public Chat_list_adapter(List<String> listChat, Context mContext) {
        this.listId = listChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_row,viewGroup,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.chatListContainer.setTag(listId.get(i));
        LoadUserNameAndPhotoProfile(listId.get(i),viewHolder.txtProfileName,viewHolder.imgProfilePic);
        LoadLastMessageAndTime(listId.get(i),viewHolder.txtLastMessage,viewHolder.txtTime);
        viewHolder.chatListContainer.setTag(listId.get(i));
        viewHolder.txtProfileName.setTag(listId.get(i));
        viewHolder.txtLastMessage.setTag(listId.get(i));
    }

    @Override
    public int getItemCount() {
        return listId.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout chatListContainer;
        CircleImageView imgProfilePic;
        TextView txtProfileName, txtLastMessage, txtTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatListContainer = itemView.findViewById(R.id.chatListContainer);
            imgProfilePic = itemView.findViewById(R.id.imgProfilePic);
            txtProfileName = itemView.findViewById(R.id.txtProfileName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(new String[]{"Hapus"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                            builder1.setTitle("Yakin?");
                            builder1.setMessage("Apakah Anda Yakin Ingin Menghapus Pesan?");
                            builder1.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    ref.child("Chats").child(auth.getCurrentUser().getUid()).child(v.getTag().toString()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(mContext, "Berhasil Menghapus Pesan!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, "Gagal Menghapus Pesan!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                        }
                    }).show();
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ChatRoomActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId",v.getTag().toString());
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    private void LoadUserNameAndPhotoProfile(final String userId, final TextView txtUserName, final CircleImageView imgProfilePic){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LoadUserDataInterface loadUserDataInterface = retrofit.create(LoadUserDataInterface.class);
        Call<UserModel> call = loadUserDataInterface.getUserData(userId);
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                txtUserName.setText(response.body().getFirstName()+" "+response.body().getLastName());
                if(!response.body().getProfilePic().equals("")){
                    Glide.with(mContext).load("https://xiti.apps.bentang.id/Images/"+response.body().getProfilePic()).into(imgProfilePic);
                }else{
                    imgProfilePic.setImageResource(R.drawable.empty_profile);
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                Log.e("Chat_list_adapter", "onFailure: "+t.toString());
            }
        });
    }

    private void LoadLastMessageAndTime(String userId, final TextView txtLastMessage, final TextView txtTime){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats").child(auth.getCurrentUser().getUid()).child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String message = "";
                String time = "";
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    message = snapshot.child("messageText").getValue().toString();
                    time = snapshot.child("messageTime").getValue().toString();
                }

                txtLastMessage.setText(message);
                try {
                    Date datePost = new Date(Long.parseLong(time) * 1000);
                    Date dateNow  = new Date();
                    long interval = dateNow.getTime() - datePost.getTime();
                    if(TimeUnit.SECONDS.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                        if(TimeUnit.MINUTES.convert(interval,TimeUnit.MILLISECONDS) >= 60){
                            if(TimeUnit.HOURS.convert(interval,TimeUnit.MILLISECONDS) >= 24){
                                txtTime.setText(String.valueOf(TimeUnit.DAYS.convert(interval,TimeUnit.MILLISECONDS))+ " hari yang lalu");
                            }else{
                                txtTime.setText(String.valueOf(TimeUnit.HOURS.convert(interval,TimeUnit.MILLISECONDS))+ " jam yang lalu");
                            }
                        }else{
                            txtTime.setText(String.valueOf(TimeUnit.MINUTES.convert(interval,TimeUnit.MILLISECONDS))+ " menit yang lalu");
                        }
                    }else{
                        txtTime.setText(String.valueOf(TimeUnit.SECONDS.convert(interval,TimeUnit.MILLISECONDS))+ " detik yang lalu");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
