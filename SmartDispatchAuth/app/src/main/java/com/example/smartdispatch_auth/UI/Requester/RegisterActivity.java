package com.example.smartdispatch_auth.UI.Requester;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.LoginActivity;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Text;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    //widgets
    private EditText mEmail, mPassword, mConfirmPassword, mAadharNumber, mPhoneNumber, mName, mAge, mLicenseNumber, mVehicleNumber;
    private ProgressBar mProgressBar;

    String name, sex, age, email, password, aadhar_number, phone_number, licenseno;
    String authenticator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.input_name);
        mAge = findViewById(R.id.input_age);
        mPhoneNumber = findViewById(R.id.input_phone);
        mAadharNumber = findViewById(R.id.input_aadhar);
        mLicenseNumber = findViewById(R.id.input_licenseno);
        mVehicleNumber = findViewById(R.id.input_vehicle_number);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);

        mProgressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        authenticator = intent.getStringExtra("user");
        switch (authenticator){
            case "requester":{
                mLicenseNumber.setVisibility(View.GONE);
                mVehicleNumber.setVisibility(View.GONE);
            }

            case "vehicle":{
                ((TextView)findViewById(R.id.textEmailReg)).setText("Register Vehicle");
                ((EditText)findViewById(R.id.input_name)).setHint("Driver Name");
                ((EditText)findViewById(R.id.input_age)).setHint("Driver Age");
                ((TextView)findViewById(R.id.input_sex)).setHint("Driver Sex");

            }

            case "hospital":{
                ((TextView)findViewById(R.id.textEmailReg)).setText("Register Hospital");
                ((EditText)findViewById(R.id.input_name)).setHint("Hospital Name");
                mLicenseNumber.setVisibility(View.GONE);
                mVehicleNumber.setVisibility(View.GONE);
                mAge.setVisibility(View.GONE);

                ((TextView)findViewById(R.id.input_sex)).setVisibility(View.GONE);
                ((View)findViewById(R.id.divider)).setVisibility(View.GONE);
                ((RadioGroup)findViewById(R.id.radioGroup)).setVisibility(View.GONE);
            }
        }


        findViewById(R.id.btn_register).setOnClickListener(this);

    }

    public void registerNewEmail(final String email, String password, final String aadhar_number, final String phone_number, final String name, final String sex, final String age) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Requester requester = new Requester(email, FirebaseAuth.getInstance().getUid(), aadhar_number,
                                    phone_number, name, sex, age,
                                    new GeoPoint(0, 0), null, "requester");

                            DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(requester).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()) {
                                        Utilities.hideDialog(mProgressBar); // Since we're going to change the activity
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.putExtra("authenticator", "requester");
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

                name = mName.getText().toString();
                age = mAge.getText().toString();
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                aadhar_number = mAadharNumber.getText().toString();
                phone_number = mPhoneNumber.getText().toString();
                licenseno = mLicenseNumber.getText().toString();

                if(!isEmpty(name) && !isEmpty(age) && !isEmpty(email) && !isEmpty(password) && !isEmpty(sex)
                        && !isEmpty(mConfirmPassword.getText().toString()) && !isEmpty(aadhar_number) && !isEmpty(phone_number)){

                    if(mPassword.getText().toString().equals(mConfirmPassword.getText().toString())){
                        //Initiate registration task
                        Toast.makeText(RegisterActivity.this, "Hello There: Beginning the registration", Toast.LENGTH_SHORT).show();
                        Utilities.showDialog(mProgressBar);
                        registerNewEmail(email, password, aadhar_number, phone_number, name, sex, age);
                    }else{
                        Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.radioGroup:{
                boolean checked = ((RadioButton) v).isChecked();

                switch (v.getId()){
                    case R.id.radio_male:{
                        if(checked)
                            sex = "Male";
                        break;

                    }

                    case R.id.radio_female:{
                        if(checked)
                            sex = "Female";
                        break;

                    }

                    case R.id.radio_other:{
                        if(checked)
                            sex = "Other";
                        break;

                    }

                }
            }

        }
    }

}
