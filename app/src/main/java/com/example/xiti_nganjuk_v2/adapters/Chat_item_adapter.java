package com.example.xiti_nganjuk_v2.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiti_nganjuk_v2.models.Chat_item_class;
import com.example.xiti_nganjuk_v2.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Chat_item_adapter extends RecyclerView.Adapter<Chat_item_adapter.ViewHolder> {
    int TYPE_LEFT = 0;
    int TYPE_RIGHT = 1;

    private List<Chat_item_class> listChat;
    private Context mContext;

    public Chat_item_adapter(List<Chat_item_class> listChat, Context mContext) {
        this.listChat = listChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        View v;
        if(i == TYPE_RIGHT){
            v = LayoutInflater.from(mContext).inflate(R.layout.chat_item_layout_right,viewGroup,false);
        }else{
            v = LayoutInflater.from(mContext).inflate(R.layout.chat_item_layout_left,viewGroup,false);
        }

        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.txtMessage.setTag(listChat.get(i).getMessageId()+","+listChat.get(i).getMessageSender()+","+listChat.get(i).getMessageReceiver());
        viewHolder.txtMessage.setText(listChat.get(i).getMessageText());
        viewHolder.container.setTag(listChat.get(i).getMessageId()+","+listChat.get(i).getMessageSender()+","+listChat.get(i).getMessageReceiver()+","+viewHolder.txtMessage.getText().toString());

    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtMessage;
        RelativeLayout container;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtChat);
            container = itemView.findViewById(R.id.messageContainer);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(new String[]{"Salin","Hapus"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0 :
                                    if(itemView instanceof TextView){
                                        copyMessage(((TextView)itemView).getText().toString());
                                    }else{
                                        String text = itemView.getTag().toString().split(",")[3];
                                        copyMessage(text);
                                    }
                                    break;
                                case 1 :
                                    deleteMessage(itemView.getTag().toString());
                                    break;
                            }
                        }
                    }).show();
                    return true;
                }
            });
        }

        private void deleteMessage(String tag){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String messageId = tag.split(",")[0];
            String messageSender = tag.split(",")[1];
            String messageReceiver = tag.split(",")[2];
            String currentUserId = auth.getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            final ProgressDialog dialog = new ProgressDialog(mContext);
            dialog.setMessage("Menghapus Pesan...");
            dialog.show();
            if(messageSender.equals(currentUserId)){
                ref.child("Chats").child(currentUserId).child(messageReceiver).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(mContext, "Berhasil Menghapus Data!", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                ref.child("Chats").child(currentUserId).child(messageSender).child(messageId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(mContext, "Berhasil Menghapus Data!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void copyMessage(String textToCopy){
            Object clipBoardService = mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            final ClipboardManager clipboardManager = (ClipboardManager) clipBoardService;
            ClipData clipData = ClipData.newPlainText("Source Text",textToCopy);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(mContext, "Berhasil Menyalin Text!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(listChat.get(position).getMessageSender().equals(auth.getCurrentUser().getUid())){
            return TYPE_RIGHT;
        }else{
            return TYPE_LEFT;
        }
    }
}
