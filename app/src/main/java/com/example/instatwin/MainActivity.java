package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.instatwin.Adapter.PostAdapter;
import com.example.instatwin.Model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;

    private Toolbar mainToolBar;

    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;

    private Dialog deleteDialog;

    private PostAdapter adapter;
    private List<Post> list;

    private Query query;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolBar = findViewById(R.id.mainToolBar);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("InstaTwin");

        deleteDialog = new Dialog(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        list = new ArrayList<>();
        adapter = new PostAdapter(list, MainActivity.this);
        mRecyclerView.setAdapter(adapter);

        fab = findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddPostActivity.class));
            }
        });

        if (auth.getCurrentUser() != null){
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean isBottom = !mRecyclerView.canScrollVertically(1);
                    if (isBottom){
                        Toast.makeText(MainActivity.this, "Reached to bottom", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            query = firebaseFirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING);
            listenerRegistration = query.addSnapshotListener(MainActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    for (DocumentChange doc : value.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){
                            String postId = doc.getDocument().getId();
                            Post post = doc.getDocument().toObject(Post.class).withId(postId);
                            list.add(post);
                            adapter.notifyDataSetChanged();
                        }else{
                            adapter.notifyDataSetChanged();
                        }
                    }
                    listenerRegistration.remove();
                }
            });
        }
    }

    private void confirmLogout() {
        deleteDialog.setContentView(R.layout.confirm_signout);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CardView confirmCard = deleteDialog.findViewById(R.id.delete_card);
        deleteDialog.show();

        FrameLayout confirmButton = deleteDialog.findViewById(R.id.confirm_logout);
        FrameLayout cancelButton = deleteDialog.findViewById(R.id.cancel_logout);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, Signin_page.class));
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currUSer = auth.getCurrentUser();
        if(currUSer == null){
            startActivity(new Intent(getApplicationContext(), Signin_page.class));
            finish();
        }else{
            String currUserId = auth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if (!task.getResult().exists()){
                            startActivity(new Intent(MainActivity.this, SetUpActivity.class));
                            finish();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.profile_menu){
            startActivity(new Intent(MainActivity.this, SetUpActivity.class));
        }else if(item.getItemId() == R.id.signout_menu){
            confirmLogout();
        }
        return true;
    }
}