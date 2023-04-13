package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private LinearLayout saveButton;
    private EditText userId;
    private TextView logOut, buttonTV, userNameOnProfile;

    private Dialog deleteDialog;

    private Uri mImageUri;

    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private String Uid;
    private ProgressBar progressBar;
    private boolean isPhotoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

//        Toolbar setUpToolBar = findViewById(R.id.setUpToolBar);
//        setSupportActionBar(setUpToolBar);
//        getSupportActionBar().setTitle("Profile");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userNameOnProfile = findViewById(R.id.userNameOnProfile);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        Uid = auth.getCurrentUser().getUid();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        profileImage = findViewById(R.id.profileImage);
        saveButton = findViewById(R.id.saveButton);
        userId = findViewById(R.id.userID);

        deleteDialog = new Dialog(this);

        logOut = findViewById(R.id.logout);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogout();
            }
        });

        buttonTV = findViewById(R.id.buttonTextView);

        auth = FirebaseAuth.getInstance();

        firebaseFirestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String user = task.getResult().getString("userid");
                        String imageUrl = task.getResult().getString("image");
//                        userId.setText(user);
                        userNameOnProfile.setText(user);
                        mImageUri = Uri.parse(imageUrl);

                        Glide.with(SetUpActivity.this).load(imageUrl).into(profileImage);
                    }
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetUpActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SetUpActivity.this);
                    }
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String user = userId.getText().toString();
                StorageReference imageRef = storageReference.child("Profile_pic").child(Uid+".jpg");

                if(isPhotoSelected){
                    if(!user.isEmpty() && mImageUri != null){
                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()){
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveToFireStore(task, user, uri);
                                        }
                                    });
                                }else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity.this, "Please select the picture and add a UserId", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    saveToFireStore(null, user, mImageUri);
                }
            }
        });
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
                startActivity(new Intent(SetUpActivity.this, Signin_page.class));
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

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String user, Uri downloadUri) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", user);
        map.put("image", downloadUri.toString());

        firebaseFirestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, "Profile Setting saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpActivity.this, MainActivity.class));
                    finish();
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                mImageUri = result.getUri();
                profileImage.setImageURI(mImageUri);

                isPhotoSelected = true;
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}