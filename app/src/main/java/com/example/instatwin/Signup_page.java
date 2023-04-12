package com.example.instatwin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ktx.Firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Signup_page extends AppCompatActivity {

    private TextView terms, goToSignin;
    private RadioButton agreeRadioButton;
    private LinearLayout signUpButton;
    private EditText name, email, password;

    // Firebase Connectivity
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        auth = FirebaseAuth.getInstance();

        terms = findViewById(R.id.term);
        goToSignin = findViewById(R.id.goto_signin);

        agreeRadioButton = findViewById(R.id.agree);

        signUpButton = findViewById(R.id.signUpButton);

//        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TermsAndCond.class));
                finish();
            }
        });

        goToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Signin_page.class));
            }
        });

        if(agreeRadioButton.isSelected()){
            signUpButton.setClickable(true);
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                String nameString = name.getText().toString();
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();

                if(!checkIfFieldsAreFilled(emailString, passwordString)){
                    Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
                }
                else{
                    if (!agreeRadioButton.isChecked()) {
                        Toast.makeText(Signup_page.this, "Please select the Terms and Conditions", Toast.LENGTH_SHORT).show();
                        return; // exit the method without executing the sign-up code
                    }
                    else{
                        auth.createUserWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    /*FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                        userRef.child("name").setValue(nameString);
                                    }*/
                                    Toast.makeText(Signup_page.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), SetUpActivity.class));
                                    finish();
                                }else{
                                    Toast.makeText(Signup_page.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private boolean checkIfFieldsAreFilled(
            String emailString, String passwordString
    ) {
        return !emailString.equals("") && !passwordString.equals("");
    }
}