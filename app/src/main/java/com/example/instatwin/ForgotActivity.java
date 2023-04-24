package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText email;
    private LinearLayout sendMailButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        backButton = findViewById(R.id.imgBack);
        email = findViewById(R.id.emailForForgetPassword);
        sendMailButton = findViewById(R.id.sendMailButton);

        auth = FirebaseAuth.getInstance();

        sendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();

                if (!checkIfFieldsAreFilled(emailString)){
                    Toast.makeText(getApplicationContext(), "Please add the proper email address", Toast.LENGTH_LONG).show();
                }else{
                    auth.sendPasswordResetEmail(emailString)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ForgotActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ForgotActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotActivity.this, Signin_page.class));
                finish();
            }
        });
    }

    private boolean checkIfFieldsAreFilled(
            String emailString
    ) {
        return !emailString.equals("");
    }
}