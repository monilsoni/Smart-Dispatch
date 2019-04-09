package com.example.smartdispatch_auth;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.example.smartdispatch_auth.Constants.ERROR_DIALOG_REQUEST;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_INTERNET;


public class SetUpActivity extends AppCompatActivity {

    private static final String TAG = "SetUpActivity";

    // widgets
    private TextView internetCheck, gpsCheck;

    // vars
    private boolean mLocationPermissionGranted = false;
    private AlertDialog internetAlert, gpsAlert;
    private boolean gpsState = false, internetState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        internetCheck = findViewById(R.id.internet_check);
        gpsCheck = findViewById(R.id.gps_check);
    }

    /* GPS Permissions */

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

    private boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SetUpActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SetUpActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
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
                if (resultCode == RESULT_OK) {
                    setGpsCheck();
                } else {
                    getLocationPermission();
                }
            }

            case PERMISSIONS_REQUEST_ENABLE_INTERNET: {
                if (resultCode == RESULT_OK) {
                    setInternetCheck();
                }
            }
        }

    }

    /* Override methods */
    @Override
    protected void onResume() {
        super.onResume();

        if (checkMapServices()) {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }
            setGpsCheck();
        }

        if (checkInternetServices()) {
            setInternetCheck();
        }


        if(gpsState && internetState){
            Intent intent = new Intent(SetUpActivity.this, EntryPoint.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    /* Helper Methods */

    public boolean checkInternetServices() {
        if (!Utilities.checkInternetConnectivity(this)) {
            buildAlertMessageNoInternet();
            return false;
        }

        return true;
    }

    private void buildAlertMessageNoGps() {
        if(gpsAlert != null)
            return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        if(internetAlert != null)
            return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void setGpsCheck() {
        gpsCheck.setText(getString(R.string.gps_connected));
        gpsState = true;
    }

    private void setInternetCheck() {
        internetCheck.setText(getString(R.string.internet_connected));
        internetState = true;
    }
}
