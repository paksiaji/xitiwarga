package com.example.xiti_nganjuk_v2;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.xiti_nganjuk_v2.adapters.Chat_list_adapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    RecyclerView rvChatList;
    ImageView imgBack,imgChatEmpty;
    List<String> listId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChatList = findViewById(R.id.rvChatList);
        imgBack = findViewById(R.id.imgBack);
        imgChatEmpty = findViewById(R.id.imgChatEmpty);
        listId = new ArrayList<>();
        LoadChatList();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void LoadChatList(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats").child(auth.getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String id = snapshot.getKey();
                    listId.add(id);
                }

                if(listId.size() > 0){
                    imgChatEmpty.setVisibility(View.INVISIBLE);
                    rvChatList.setVisibility(View.VISIBLE);
                }else{
                    imgChatEmpty.setVisibility(View.VISIBLE);
                    rvChatList.setVisibility(View.INVISIBLE);
                }

                Chat_list_adapter adapter = new Chat_list_adapter(listId,ChatListActivity.this);
                LinearLayoutManager manager = new LinearLayoutManager(ChatListActivity.this);
                rvChatList.setLayoutManager(manager);
                rvChatList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
