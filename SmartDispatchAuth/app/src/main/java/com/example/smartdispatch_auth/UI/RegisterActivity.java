package com.example.smartdispatch_auth.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.Admin.AdminMainActivity;
import com.example.smartdispatch_auth.UI.LoginActivity;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.Timestamp;
import java.util.Date;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    String name, sex, age, email, password, aadhar_number, phone_number, licenseno, vehicleno;
    String authenticator;
    //widgets
    private EditText mEmail, mPassword, mConfirmPassword, mAadharNumber, mPhoneNumber, mName, mAge, mLicenseNumber, mVehicleNumber;
    private ProgressDialog progress;

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

        progress = new ProgressDialog(this);
        progress.setMessage("Registering...");
        progress.setCancelable(false);

        Intent intent = getIntent();
        authenticator = intent.getStringExtra("user");

        switch (authenticator) {
            case "requester": {
                mLicenseNumber.setVisibility(View.GONE);
                mVehicleNumber.setVisibility(View.GONE);
                break;
            }

            case "vehicle": {
                ((TextView) findViewById(R.id.textEmailReg)).setText("Register Vehicle");
                ((EditText) findViewById(R.id.input_name)).setHint("Driver Name");
                ((EditText) findViewById(R.id.input_age)).setHint("Driver Age");
                ((TextView) findViewById(R.id.input_sex)).setHint("Driver Sex");
                break;

            }

            case "hospital": {
                ((TextView) findViewById(R.id.textEmailReg)).setText("Register Hospital");
                ((EditText) findViewById(R.id.input_name)).setHint("Hospital Name");
                mLicenseNumber.setVisibility(View.GONE);
                mVehicleNumber.setVisibility(View.GONE);
                mAadharNumber.setVisibility(View.GONE);
                mAge.setVisibility(View.GONE);

                findViewById(R.id.input_sex).setVisibility(View.GONE);
                findViewById(R.id.divider).setVisibility(View.GONE);
                findViewById(R.id.radioGroup).setVisibility(View.GONE);
                break;

            }
        }


        findViewById(R.id.btn_register).setOnClickListener(this);

    }

    public void registerRequesterNewEmail(final String email, String password, final String aadhar_number, final String phone_number, final String name, final String sex, final String age) {

        progress.show();
        Log.d(TAG, "registerRequesterNewEmail: " + email + aadhar_number);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Date date = new Date();
                            Timestamp time = new Timestamp(date.getTime());

                            Requester requester = new Requester(email, FirebaseAuth.getInstance().getCurrentUser().getUid(), aadhar_number,
                                    phone_number, name, sex, age,
                                    new GeoPoint(0, 0), time, "requester", FirebaseInstanceId.getInstance().getToken());

                            DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            newUserRef.set(requester).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        progress.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("authenticator", "requester");
                                        startActivity(intent);
                                        finish();
                                    } else if (task.getException() != null) {
                                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if (task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Something went wrong with registration.", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }
                });
    }

    public void registerHospitalNewEmail(final String email, String password, final String name, final String contactno) {

        progress.show();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Date date = new Date();
                            Timestamp time = new Timestamp(date.getTime());

                            Hospital hospital = new Hospital(new GeoPoint(0, 0), time, name, email, FirebaseAuth.getInstance().getCurrentUser().getUid(), FirebaseInstanceId.getInstance().getToken(), "hospital", contactno);

                            DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.collection_hospitals))
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            newUserRef.set(hospital).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        progress.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("authenticator", "hospital");
                                        startActivity(intent);
                                        finish();
                                    } else if (task.getException() != null) {
                                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if (task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Something went wrong with registration.", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }
                });
    }

    public void registerVehicleNewEmail(final String email, String password, final String aadhar_number, final String phone_number, final String name, final String licenseno, final String age, final String vehicleno) {

        progress.show();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Date date = new Date();
                            Timestamp time = new Timestamp(date.getTime());
                            Vehicle vehicle = new Vehicle(name, age, vehicleno, phone_number, licenseno, aadhar_number, email, FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    new GeoPoint(0, 0), time, "vehicle", FirebaseInstanceId.getInstance().getToken(), 0);

                            DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                    .collection("Vehicles")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            newUserRef.set(vehicle).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        progress.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("authenticator", "vehicle");
                                        startActivity(intent);
                                        finish();
                                    } else if (task.getException() != null) {
                                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else if (task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Something went wrong with registration.", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();

                    }
                });
    }

    /* Override methods */
    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_male:
                if (checked)
                    sex = "Male";
                break;
            case R.id.radio_female:
                if (checked)
                    sex = "Female";
                break;
            case R.id.radio_other:
                if (checked)
                    sex = "Other";
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {
            name = mName.getText().toString();
            age = mAge.getText().toString();
            email = mEmail.getText().toString();
            password = mPassword.getText().toString();
            aadhar_number = mAadharNumber.getText().toString();
            phone_number = mPhoneNumber.getText().toString();
            licenseno = mLicenseNumber.getText().toString();
            vehicleno = mVehicleNumber.getText().toString();

            if (!isEmpty(name) && !isEmpty(email) && !isEmpty(password) && !isEmpty(mConfirmPassword.getText().toString())
                    && !isEmpty(phone_number)) {


                if (authenticator.equals("requester")) {
                    if (!isEmpty(age) && !isEmpty(sex) && !isEmpty(aadhar_number)) {

                        if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                            Toast.makeText(RegisterActivity.this, "Hello There: Beginning the registration", Toast.LENGTH_SHORT).show();

                            registerRequesterNewEmail(email, password, aadhar_number, phone_number, name, sex, age);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                    }
                } else if (authenticator.equals("vehicle")) {
                    if (!isEmpty(age) && !isEmpty(sex) && !isEmpty(aadhar_number) && !isEmpty(vehicleno)) {
                        if (!email.contains("@smartdispatch.gov.in") || !email.contains("v_"))
                            Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        else if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                            Toast.makeText(RegisterActivity.this, "Hello There: Beginning the registration", Toast.LENGTH_SHORT).show();

                            registerVehicleNewEmail(email, password, aadhar_number, phone_number, name, licenseno, age, vehicleno);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                    }
                } else if (authenticator.equals("hospital")) {
                    if (!email.contains("@smartdispatch.gov.in") || !email.contains("h_"))
                        Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                    else if (mPassword.getText().toString().equals(mConfirmPassword.getText().toString())) {
                        Toast.makeText(RegisterActivity.this, "Hello There: Beginning the registration", Toast.LENGTH_SHORT).show();

                        registerHospitalNewEmail(email, password, name, phone_number);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(RegisterActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
