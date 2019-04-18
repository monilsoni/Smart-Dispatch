package com.example.smartdispatch_auth.UI.Hospital;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.LocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.Vehicle.VehicleMainActivity;
import com.example.smartdispatch_auth.UserClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import static com.example.smartdispatch_auth.Constants.ERROR_DIALOG_REQUEST;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_INTERNET;

public class HospitalMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HospitalMainActivity";
    IntentFilter filter;
    ProgressDialog progress;
    // vars
    private boolean mLocationPermissionGranted = false;
    private boolean internetState = false, gpsState = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Hospital mHospital;
    private AlertDialog internetAlert, gpsAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_main);

        findViewById(R.id.current_request).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading your data");
        progress.setCancelable(false);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(new HospitalMainActivity.CheckConnectivity(), filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void buildAlertMessageNoGps() {
        if (gpsAlert != null)
            return;

        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        gpsAlert = builder.create();
        gpsAlert.show();

    }

    private void buildAlertMessageNoInternet() {
        if (internetAlert != null)
            return;

        final android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires internet connection to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivityForResult(intent, PERMISSIONS_REQUEST_ENABLE_INTERNET);
                    }
                });
        internetAlert = builder.create();
        internetAlert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    gpsState = true;
                } else {
                    getLocationPermission();
                }
            }

            case PERMISSIONS_REQUEST_ENABLE_INTERNET: {
                if (resultCode == RESULT_OK) {
                    internetState = true;
                }
            }
        }

        if (internetState && gpsState)
            getHospitalDetails();
        else
            Log.d(TAG, "onActivityResult: Did not switch on the network. Send broadcast from here");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isServicesOK()) {
            if (mLocationPermissionGranted && internetState && gpsState) {
                getHospitalDetails();
            } else {
                getLocationPermission();
            }
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HospitalMainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HospitalMainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
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

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
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

    private void getHospitalDetails() {

        progress.show();

        if (mHospital == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            mHospital = new Hospital();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_hospitals))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {

                            mHospital = task.getResult().toObject(Hospital.class);
                            Log.d(TAG, "Hospital inside getHospitalDetails: " + mHospital.toString());
                            mHospital.setTimeStamp(null);

                            ((UserClient) (getApplicationContext())).setHospital(mHospital);

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;


        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "getLastLocation: Successful.");

                    Location mLocation = task.getResult();
                    GeoPoint geoPoint = (mLocation != null) ?
                            new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude()) : new GeoPoint(0, 0);

                    mHospital.setGeoPoint(geoPoint);
                    startLocationService();
                    display();

                }
            }
        });
    }

    public void display() {

        Log.d(TAG, "display: name: " + mHospital.getHospital_name());
        ((TextView) findViewById(R.id.hospital_name)).setText(mHospital.getHospital_name());
        ((TextView) findViewById(R.id.hospital_contact)).setText(mHospital.getContactno());
        progress.dismiss();

    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                HospitalMainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    /*  GPS Service  */

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.smartdispatch_auth.Services.RequesterLocaitonService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.current_request: {
                Intent intent = new Intent(HospitalMainActivity.this, HospitalCurrentRequestActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.sign_out: {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.remove("type");
                editor.apply();

                if (isLocationServiceRunning()) {
                    Intent serviceIntent = new Intent(this, LocationService.class);
                    stopService(serviceIntent);
                }

                ((UserClient) getApplicationContext()).setHospital(null);
                ((UserClient) getApplicationContext()).setRequest(null);

                Intent intent = new Intent(HospitalMainActivity.this, EntryPoint.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            }
        }

    }

    public class CheckConnectivity extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {

            boolean isNotConnected = arg1.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (isNotConnected) {
                internetAlert = null;
                buildAlertMessageNoInternet();
            } else {
                internetState = true;
            }


            final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsState = true;
            } else {
                gpsAlert = null;
                buildAlertMessageNoGps();
            }

            if (gpsState && internetState)
                getHospitalDetails();
        }
    }

}