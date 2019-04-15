package com.example.smartdispatch_auth.UI.Hospital;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestDisp;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.HospitalLocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.Requester.UserMainActivity;
import com.example.smartdispatch_auth.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class HospitalMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HospitalMainActivity";
    // vars
    private FusedLocationProviderClient mFusedLocationClient;
    private Hospital mHospital;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_main);

        findViewById(R.id.current_request).setOnClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.sign_out).setOnClickListener(this);


    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isMapsEnabled()) {
            getHospitalDetails();
        }
    }

    /*  GPS Service  */

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, HospitalLocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                HospitalMainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.smartdispatch_auth.Services.HospitalLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void getHospitalDetails() {

        if (mHospital == null) {
            mHospital = new Hospital();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_hospital))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the requester client.");
                        mHospital = task.getResult().toObject(Hospital.class);
                        Log.d(TAG, "Hospital inside getHospitalDetails: " + mHospital.toString());
                        ((UserClient) (getApplicationContext())).setHospital(mHospital);

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getLastLocation: Successful.");

                    Location mLocation = task.getResult();
                    mHospital.setTimeStamp(null);
                    GeoPoint geoPoint = new GeoPoint(0, 0);
                    try {
                        geoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());

                    } catch (NullPointerException e) {
                        Log.d(TAG, "getLastKnownLocation: mLocation is null.");

                    }
                    mHospital.setGeoPoint(geoPoint);

                    /* just add the requester to the list. It does not matter what the geopoint is
                     * since the UserMapActivity is going to fetch the location anyway */
                    startLocationService();
                }
            }
        });
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                getHospitalDetails();
            }
        }

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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.current_request){
            Intent intent = new Intent(HospitalMainActivity.this, HospitalCurrentRequestActivity.class);
            startActivity(intent);
        }else if(v.getId() == R.id.sign_out){
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(HospitalMainActivity.this, EntryPoint.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(HospitalMainActivity.this, "Youzaa", Toast.LENGTH_SHORT).show();
        }

    }
}