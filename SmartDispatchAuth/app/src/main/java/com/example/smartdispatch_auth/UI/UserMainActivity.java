package com.example.smartdispatch_auth.UI;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.User;
import com.example.smartdispatch_auth.Models.UserLocation;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.LocationService;
import com.example.smartdispatch_auth.SetUpActivity;
import com.example.smartdispatch_auth.UserClient;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import static com.example.smartdispatch_auth.Constants.ERROR_DIALOG_REQUEST;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_INTERNET;

public class UserMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserMainActivity";

    // Android widgets
    private TextView mWelcomeText, mLocationText, mAadharText, mPhoneText;
    private ProgressBar mProgressBar;

    // Variables
    private ListenerRegistration mUserListEventListener;
    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private boolean set = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWelcomeText = findViewById(R.id.welcome);
        mLocationText = findViewById(R.id.location);
        mAadharText = findViewById(R.id.aadhar);
        mPhoneText = findViewById(R.id.phone);
        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.look_at_map).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);

        getUserDetails();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

                Intent intent = new Intent(UserMainActivity.this, MapActivity.class);
                intent.putParcelableArrayListExtra(getString(R.string.intent_user_list), mUserList);
                intent.putParcelableArrayListExtra(getString(R.string.intent_user_locations), mUserLocations);
                startActivity(intent);
            }
        }
    }

    /* Helper methods */

    private void getUserDetails() {

        if (!set) {
            Utilities.showDialog(mProgressBar);
        }
        if (mUserLocation == null) {
            mUserLocation = new UserLocation();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        User user = task.getResult().toObject(User.class);
                        mUserLocation.setUser(user);
                        ((UserClient) (getApplicationContext())).setUser(user);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /* The get Last Location method may return null if it has not been a long time since the app started. */

        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getLastLocation: Successful.");


                    Location mLocation = task.getResult();
                    GeoPoint geoPoint = null;
                    try{
                        geoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
                    }catch (NullPointerException e){
                        Log.d(TAG, "getLastKnownLocation: mLocation is null.");
                    }
                    mUserLocation.setGeoPoint(geoPoint);
                    mUserLocation.setTimeStamp(null);
                    saveUserLocation();
                    startLocationService();
                }
            }
        });
    }

    private void saveUserLocation() {
        if (mUserLocation != null) {
            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_locations))
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveUserLocation: \ninserted user location into database." +
                                "\n latitude: " + mUserLocation.getGeoPoint().getLatitude() +
                                "\n longitude: " + mUserLocation.getGeoPoint().getLongitude());

                        getUsers();
                        display();
                    } else {
                        Log.d(TAG, "saveUserLocation not successful. ");
                    }
                }
            });
        }

    }

    private void getUsers() {

        CollectionReference usersRef = FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users));

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    mUserList.add(user);
                                    getUserLocation(user);
                                } else {
                                    Log.d(TAG, "user Null");
                                }
                            }

                            Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                        }
                    }
                });
    }

    private void getUserLocation(final User user) {

        if (user != null) {
            Log.d(TAG, "getUserLocation: " + user.toString());

            DocumentReference locationRef = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.collection_user_locations))
                    .document(user.getUser_id());

            locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserLocation userLocation = task.getResult().toObject(UserLocation.class);
                        if (userLocation != null) {
                            mUserLocations.add(userLocation);
                            Log.d(TAG, userLocation.toString());
                        }
                    }
                }
            });
        } else {
            Log.d(TAG, "getUserLocation: user is null.");
        }


    }

    public void display() {

        User user = ((UserClient) getApplicationContext()).getUser();

        Utilities.hideDialog(mProgressBar);
        mWelcomeText.setText("Welcome to SmartDispatch " + user.getEmail().substring(0, user.getEmail().indexOf("@")));
        mAadharText.setText("Aadhar Number: " + user.getAadhar_number());
        mPhoneText.setText("Phone Number: " + user.getPhone_number());
        mLocationText.setText("Latitude: " + mUserLocation.getGeoPoint().getLatitude() + ", Longitude: " + mUserLocation.getGeoPoint().getLongitude());
        set = true;
    }


}

