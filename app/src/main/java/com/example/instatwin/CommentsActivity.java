package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.instatwin.Adapter.CommentsAdapter;
import com.example.instatwin.Model.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private EditText commentEditText;
    private LinearLayout addCommentButton;
    private RecyclerView commentRecyclerView;

    private FirebaseFirestore firebaseFirestore;

    private String postId;

    private String currUserId;

    private FirebaseAuth auth;

    private CommentsAdapter adapter;
    private List<Comments> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentEditText = findViewById(R.id.addCommentED);
        addCommentButton = findViewById(R.id.addCommentBtn);
        commentRecyclerView = findViewById(R.id.comment_recyclerView);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currUserId = auth.getCurrentUser().getUid();

        postId = getIntent().getStringExtra("postId");

        mList = new ArrayList<>();
        adapter = new CommentsAdapter(CommentsActivity.this, mList);
        commentRecyclerView.setHasFixedSize(true);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentRecyclerView.setAdapter(adapter);

        firebaseFirestore.collection("Posts/"+postId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        Comments comments = documentChange.getDocument().toObject(Comments.class);
                        mList.add(comments);
                        adapter.notifyDataSetChanged();
                    }else{
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });


        addCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEditText.getText().toString();

                if (!comment.isEmpty()){
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("comment", comment);
                    commentMap.put("time", FieldValue.serverTimestamp());
                    commentMap.put("user", currUserId);
                    firebaseFirestore.collection("Posts/"+postId+"/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "Comment Added!", Toast.LENGTH_SHORT).show();
                                commentEditText.setText("");
                            }else{
                                Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(CommentsActivity.this, "Comment is must", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}