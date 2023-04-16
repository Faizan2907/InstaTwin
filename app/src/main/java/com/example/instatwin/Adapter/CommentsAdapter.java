package com.example.instatwin.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instatwin.Model.CommentId;
import com.example.instatwin.Model.Comments;
import com.example.instatwin.Model.Post;
import com.example.instatwin.Model.Users;
import com.example.instatwin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Activity context;
    private List<Comments> list;
    private List<Users> usersList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String currentUserId;
    private String postId; // instance variable for postId


    public CommentsAdapter(Activity context, List<Comments> list, List<Users> usersList, String postId) {
        this.context = context;
        this.list = list;
        this.usersList = usersList;
        this.postId = postId; // store the postId as an instance variable
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.each_comment, parent, false);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        Comments comments = list.get(holder.getAdapterPosition());
        holder.setComment(comments.getComment());

        Users users = usersList.get(holder.getAdapterPosition());
        holder.setUserName(users.getUserid());
        holder.setCircleImageView(users.getImage());

        // Delete comment only
        // Check if the comment is posted by the current user
        if (currentUserId.equals(comments.getUser())) {
            holder.deleteCommentBtn.setVisibility(View.VISIBLE);
            holder.deleteCommentBtn.setClickable(true);
            holder.deleteCommentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Delete Comment")
                            .setMessage("Are you sure you want to delete this comment?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firebaseFirestore.collection("Posts/" + postId + "/Comments").whereEqualTo("user", currentUserId).whereEqualTo("comment", comments.getComment()).get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            firebaseFirestore.collection("Posts/" + postId + "/Comments")
                                                                    .document(document.getId())
                                                                    .delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            // Comment deleted successfully
                                                                            Toast.makeText(context, "Comment deleted!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            // Failed to delete comment
                                                                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        }

                                                    } else {
                                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    list.remove(holder.getAdapterPosition());
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show(); // Add this line to display the alert dialog to the user
                }
            });
        }else{
            holder.deleteCommentBtn.setVisibility(View.GONE); // or INVISIBLE if you want to keep the space
            holder.deleteCommentBtn.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder{

        TextView commentTV, userNameTV;
        CircleImageView circleImageView;
        View mView;
        ImageButton deleteCommentBtn;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            deleteCommentBtn = itemView.findViewById(R.id.deleteComment);
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
