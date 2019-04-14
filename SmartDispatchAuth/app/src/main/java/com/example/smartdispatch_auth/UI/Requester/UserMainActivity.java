package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.RequesterLocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.Hospital.HospitalMapActivity;
import com.example.smartdispatch_auth.UserClient;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

import static com.example.smartdispatch_auth.Constants.ERROR_DIALOG_REQUEST;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

// todo when driver reaches user, he will end the trip. then trip to hospital will be shown.
// todo vehicle detect request object
// todo when requester has an active request disable request form button and display the active request

public class UserMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UserMainActivity";

    // Android widgets
    private TextView mWelcomeText, mAadharText, mPhoneText;

    // Variables
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Requester mRequester;
    private Request mRequest;
    private boolean set = false;
    Source source = Source.DEFAULT;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        mWelcomeText = findViewById(R.id.welcome);
        mAadharText = findViewById(R.id.aadhar);
        mPhoneText = findViewById(R.id.phone);

        findViewById(R.id.look_at_map).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);
        findViewById(R.id.submit_request).setOnClickListener(this);
        findViewById(R.id.driver_phone).setOnClickListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        if(intent.getParcelableExtra("request") != null){
            mRequest = intent.getParcelableExtra("request");
            findViewById(R.id.submit_request).setVisibility(View.GONE);
        }else{
            findViewById(R.id.look_at_map).setVisibility(View.GONE);
            findViewById(R.id.vehicle_layout).setVisibility(View.GONE);
        }

        progress = new ProgressDialog(this);
        progress.setMessage("Loading your data");
        progress.setCancelable(false);
    }

    /*  GPS Service  */

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, RequesterLocationService.class);
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
            if ("com.example.smartdispatch_auth.Services.RequesterLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    /*  Override methods  */

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

                Intent intent = new Intent(UserMainActivity.this, UserMapActivity.class);
                intent.putExtra("request", mRequest);
                startActivity(intent);
                break;
            }

            case R.id.submit_request: {
                Intent intent = new Intent(UserMainActivity.this, RequestForm.class);
                startActivity(intent);
                break;
            }

            case R.id.driver_phone: {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mRequest.getVehicle().getPhone_number()));
                startActivity(intent);
            }
        }
    }

    /* Helper methods */

    private void getUserDetails() {

        if (!set) {
            progress.show();
        }
        if (mRequester == null) {
            mRequester = new Requester();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if(task.getResult().exists()){
                            Log.d(TAG, "onComplete: successfully set the requester client.");
                            mRequester = task.getResult().toObject(Requester.class);
                            Log.d(TAG, "Requester inside getUserDetails: " + mRequester.toString());

                            ((UserClient) (getApplicationContext())).setRequester(mRequester);

                            getLastKnownLocation();
                        }

                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation called.");

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
                    GeoPoint geoPoint = new GeoPoint(0, 0);
                    try {
                        geoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());

                    } catch (NullPointerException e) {
                        Log.d(TAG, "getLastKnownLocation: mLocation is null.");

                    }
                    mRequester.setGeoPoint(geoPoint);

                    progress.dismiss();
                    startLocationService();
                    display();
                }
            }
        });
    }


    public void display() {

        Requester requester = ((UserClient) getApplicationContext()).getRequester();

        mWelcomeText.setText("Welcome to SmartDispatch " + requester.getEmail().substring(0, requester.getEmail().indexOf("@")));
        mAadharText.setText("Aadhar Number: " + requester.getAadhar_number());
        mPhoneText.setText("Phone Number: " + requester.getPhone_number());
        set = true;

        final DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists() && snapshot.getData().get("geoPoint") != null) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    mRequester.setGeoPoint((GeoPoint) snapshot.getData().get("geoPoint"));
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Utilities.checkInternetConnectivity(this)){
            source = Source.CACHE;
        }

        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getUserDetails();
            }
            else{
                getLocationPermission();
            }
        }
    }


    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserDetails();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(UserMainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(UserMainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getUserDetails();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

}

