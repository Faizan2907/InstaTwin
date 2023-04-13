package com.example.instatwin.Adapter;

import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instatwin.Model.Comments;
import com.example.instatwin.Model.Users;
import com.example.instatwin.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Activity context;
    private List<Comments> list;
    private List<Users> usersList;

    public CommentsAdapter(Activity context, List<Comments> list, List<Users> usersList) {
        this.context = context;
        this.list = list;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment, parent, false);
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comments comments = list.get(position);
        holder.setComment(comments.getComment());

        Users users = usersList.get(position);
        holder.setUserName(users.getUserid());
        holder.setCircleImageView(users.getImage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{

        TextView commentTV, userNameTV;
        CircleImageView circleImageView;
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment){
            commentTV = mView.findViewById(R.id.userCommentTV);
            commentTV.setText(comment);
        }

        public void setUserName(String userName){
            userNameTV = mView.findViewById(R.id.userNameTV);
            userNameTV.setText(userName);
        }

        public void setCircleImageView(String profilePic){
            circleImageView = mView.findViewById(R.id.profilePicComment);
            Glide.with(context).load(profilePic).into(circleImageView);
        }
    }
}
