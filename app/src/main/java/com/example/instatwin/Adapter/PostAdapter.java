package com.example.instatwin.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instatwin.CommentsActivity;
import com.example.instatwin.Model.Post;
import com.example.instatwin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> elist;
    private Activity context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    public PostAdapter(List<Post> elist, Activity context) {
        this.elist = elist;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.each_post, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = elist.get(position);
        holder.setPostPic(post.getImage());
        holder.setPostCaption(post.getCaption());

        long milliseconds = post.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        holder.setPostDate(date);

        String userId = post.getUser();
        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String username = task.getResult().getString("userid");
                    String image = task.getResult().getString("image");

                    holder.setProfilePic(image);
                    holder.setPostUserName(username);
                }else{
                    Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Like Button Code
        String postId = post.PostId;
        String currentUserId = auth.getCurrentUser().getUid();
        holder.likePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts/"+postId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()){
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+postId+"/Likes").document(currentUserId).set(likesMap);
                        }else{
                            firebaseFirestore.collection("Posts/"+postId+"/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        // Like Button color change to red if clicked
        firebaseFirestore.collection("Posts/"+postId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null){
                    if (value.exists()){
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_24));
                    }
                    else{
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_favorite_border_24));
                    }
                }
            }
        });

        // Likes count
        firebaseFirestore.collection("Posts/"+postId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error == null){
                    if (!value.isEmpty()){
                        int count = value.size();
                        holder.setPostLikes(count);
                    }else{
                        holder.setPostLikes(0);
                    }
                }
            }
        });

        // Comments implementation
        holder.commentsPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("postId", postId);
                context.startActivity(commentIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return elist.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView postPic, commentsPic, likePic;
        CircleImageView profilePic;
        TextView postUserName, postDate, postCaption, postLikes;
        View mView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likePic = itemView.findViewById(R.id.likeBtn);
            commentsPic = itemView.findViewById(R.id.comment_section);
        }

        public void setCommentsPic(){

        }

        public void setPostLikes(int count){
            postLikes = mView.findViewById(R.id.likeCount);
            postLikes.setText(count + " Likes");
        }

        public void setPostPic(String urlPost){
            postPic = mView.findViewById(R.id.postImageEachImage);
            Glide.with(context).load(urlPost).into(postPic);
        }
        public void setProfilePic(String urlProfile){
            profilePic = mView.findViewById(R.id.profilePic);
            Glide.with(context).load(urlProfile).into(profilePic);
        }
        public void setPostUserName(String userName){
            postUserName = mView.findViewById(R.id.userNameEachPost);
            postUserName.setText(userName);
        }
        public void setPostDate(String date){
            postDate = mView.findViewById(R.id.dateTV);
            postDate.setText(date);
        }
        public void setPostCaption(String caption){
            postCaption = mView.findViewById(R.id.caption_TV);
            postCaption.setText(caption);
        }
    }
}
