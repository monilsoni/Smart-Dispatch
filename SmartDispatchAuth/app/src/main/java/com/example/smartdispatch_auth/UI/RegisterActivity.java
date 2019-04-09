package com.example.smartdispatch_auth.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.User;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    //widgets
    private EditText mEmail, mPassword, mConfirmPassword, mAadharNumber, mPhoneNumber;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mAadharNumber = findViewById(R.id.input_aadhar);
        mPhoneNumber = findViewById(R.id.input_phone);

        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btn_register).setOnClickListener(this);

    }

    public void registerNewEmail(final String email, String password, final String aadhar_number, final String phone_number) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            User user = new User(email, FirebaseAuth.getInstance().getUid(), aadhar_number, phone_number);

                            DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        Utilities.hideDialog(mProgressBar); // Since we're going to change the activity
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else if(task.getException() != null){
                                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(RegisterActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if(task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(RegisterActivity.this, "Something went wrong with registration.", Toast.LENGTH_SHORT).show();
                        }
                        Utilities.hideDialog(mProgressBar);
                    }
                });
    }

    /* Override methods */

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_register:{

                String email, password, aadhar_number, phone_number;
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                aadhar_number = mAadharNumber.getText().toString();
                phone_number = mPhoneNumber.getText().toString();

                //check for null valued EditText fields
                if(!isEmpty(email)
                        && !isEmpty(password)
                        && !isEmpty(mConfirmPassword.getText().toString())
                        && !isEmpty(aadhar_number)
                        && !isEmpty(phone_number)){

                    //check if passwords match
                    if(mPassword.getText().toString().equals(mConfirmPassword.getText().toString())){
                        //Initiate registration task
                        Toast.makeText(RegisterActivity.this, "Hello There: Beginning the registration", Toast.LENGTH_SHORT).show();
                        Utilities.showDialog(mProgressBar);
                        registerNewEmail(email, password, aadhar_number, phone_number);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
