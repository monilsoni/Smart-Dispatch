package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.LocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UserClient;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserMainActivity";

    // Android widgets
    private TextView mWelcomeText, mLocationText, mAadharText, mPhoneText;
    private ProgressBar mProgressBar;

    // Variables
    private ListenerRegistration mUserListEventListener;
    private FusedLocationProviderClient mFusedLocationClient;
    private Requester mRequester;
    private ArrayList<Requester> mUserList = new ArrayList<>();
    private boolean set = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        mWelcomeText = findViewById(R.id.welcome);
        mLocationText = findViewById(R.id.location);
        mAadharText = findViewById(R.id.aadhar);
        mPhoneText = findViewById(R.id.phone);
        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.look_at_map).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);
        findViewById(R.id.submit_request).setOnClickListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserDetails();
    }

    /*  GPS Service  */

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                UserMainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    /*  Override methods  */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUserListEventListener != null) {
            mUserListEventListener.remove();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out: {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(UserMainActivity.this, EntryPoint.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            }

            case R.id.look_at_map: {
                if (mProgressBar.isShown()) {
                    Toast.makeText(this, "Please wait for the application to load, thank you!", Toast.LENGTH_SHORT).show();
                    break;
                }

                Intent intent = new Intent(UserMainActivity.this, UserMapActivity.class);
                intent.putParcelableArrayListExtra(getString(R.string.intent_user_list), mUserList);
                intent.putExtra("requester", mRequester);
                Log.d(TAG, mRequester.toString());
                startActivity(intent);
                break;
            }

            case R.id.submit_request: {
                Intent intent = new Intent(UserMainActivity.this, RequestForm.class);
                startActivity(intent);
                break;
            }
        }
    }

    /* Helper methods */

    private void getUserDetails() {

        if (!set) {
            Utilities.showDialog(mProgressBar);
        }
        if (mRequester == null) {
            mRequester = new Requester();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the requester client.");
                        mRequester = task.getResult().toObject(Requester.class);
                        Log.d(TAG, "Requester inside getUserDetails: " + mRequester.toString());
                        ((UserClient) (getApplicationContext())).setRequester(mRequester);
                        getLastKnownLocation();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation called.");


        /* The get Last Location method may return null if it has not been a long time since the app started. */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getLastLocation: Successful.");

                    Location mLocation = task.getResult();
                    mRequester.setTimeStamp(null);
                    try {
                        GeoPoint geoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
                        mRequester.setGeoPoint(geoPoint);

                        mUserList.add(mRequester);
                        display();
                        startLocationService();

                    } catch (NullPointerException e) {
                        Log.d(TAG, "getLastKnownLocation: mLocation is null.");
                    }

                }
            }
        });
    }


    public void display() {

        Requester requester = ((UserClient) getApplicationContext()).getRequester();

        Utilities.hideDialog(mProgressBar);
        mWelcomeText.setText("Welcome to SmartDispatch " + requester.getEmail().substring(0, requester.getEmail().indexOf("@")));
        mAadharText.setText("Aadhar Number: " + requester.getAadhar_number());
        mPhoneText.setText("Phone Number: " + requester.getPhone_number());
        mLocationText.setText("Latitude: " + requester.getGeoPoint().getLatitude() + ", Longitude: " + requester.getGeoPoint().getLongitude());
        set = true;
    }
}

