package com.example.xiti_nganjuk_v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.xiti_nganjuk_v2.DetailPostActivity;
import com.example.xiti_nganjuk_v2.models.Post_grid_model_class;
import com.example.xiti_nganjuk_v2.R;

import java.util.List;

public class Post_item_grid_adapter extends RecyclerView.Adapter<Post_item_grid_adapter.ViewHolder>{
    private List<Post_grid_model_class> listPost;
    private Context mContext;

    public Post_item_grid_adapter(List<Post_grid_model_class> listPost, Context mContext) {
        this.listPost = listPost;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item_grid_row,viewGroup,false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.ic_sync_black_24dp);
        Glide.with(mContext).load(listPost.get(i).getUrlContent()).apply(options).into(viewHolder.imgContent);
        viewHolder.imgContent.setTag(R.id.imgContent,listPost.get(i).getPostId());
        viewHolder.txtStatus.setText(listPost.get(i).getStatus());
        viewHolder.txtStatus.setTag(listPost.get(i).getPostId());
        if(listPost.get(i).getStatus().equals("Pending")){
            viewHolder.ctStatus.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        }else if(listPost.get(i).getStatus().equals("Process")){
            viewHolder.ctStatus.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        }else{
            viewHolder.ctStatus.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        }

        viewHolder.ctStatus.setTag(listPost.get(i).getPostId());
        viewHolder.ctAll.setTag(listPost.get(i).getPostId());
    }

    @Override
    public int getItemCount() {
        return listPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContent;
        ConstraintLayout ctStatus, ctAll;
        TextView txtStatus;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            imgContent = itemView.findViewById(R.id.imgContent);
            ctStatus = itemView.findViewById(R.id.ctStatus);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            ctAll = itemView.findViewById(R.id.ctContainerAll);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailPostActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("postId",v.getTag().toString());
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
