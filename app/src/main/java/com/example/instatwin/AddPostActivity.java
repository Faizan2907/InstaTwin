package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    private LinearLayout addPostButton;
    private EditText captionText;
    private ImageView postImage, backImage;
    private ProgressBar progressBar;
    private Uri postImageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String currUserId;
    private Toolbar mainToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

//        mainToolBar = findViewById(R.id.addPostToolBar);
//        setSupportActionBar(mainToolBar);
//        getSupportActionBar().setTitle("Add Post");

        addPostButton = findViewById(R.id.addPostButton);
        captionText = findViewById(R.id.captionText);
        postImage = findViewById(R.id.postImageAdd);
        backImage = findViewById(R.id.imgBackAddPost);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                finish();
            }
        });

        progressBar = findViewById(R.id.progressBarAddPost);
        progressBar.setVisibility(View.INVISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currUserId = auth.getCurrentUser().getUid();

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .start(AddPostActivity.this);
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String caption = captionText.getText().toString();
                if (!caption.isEmpty() && postImageUri != null){
                    StorageReference postRef = storageReference.child("post_image").child(FieldValue.serverTimestamp().toString()+".jpg");
                    postRef.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        HashMap<String, Object> postMap = new HashMap<>();
                                        postMap.put("image", uri.toString());
                                        postMap.put("user", currUserId);
                                        postMap.put("caption", caption);
                                        postMap.put("time", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()){
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                                                    finish();
                                                }else{
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddPostActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(AddPostActivity.this, "Please add Image and write caption", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                postImageUri = result.getUri();
                postImage.setImageURI(postImageUri);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, result.getError().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}