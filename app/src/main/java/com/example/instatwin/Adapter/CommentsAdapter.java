package com.example.instatwin.Adapter;

import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instatwin.Model.Comments;
import com.example.instatwin.R;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Activity context;
    private List<Comments> list;

    public CommentsAdapter(Activity context, List<Comments> list) {
        this.context = context;
        this.list = list;
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{

        TextView commentTV;
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment){
            commentTV = mView.findViewById(R.id.userCommentTV);
            commentTV.setText(comment);
        }
    }
}
