package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.LocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.Vehicle.VehicleMainActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import static com.example.smartdispatch_auth.Constants.ERROR_DIALOG_REQUEST;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class RequesterMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RequesterMainActivity";
    Source source = Source.DEFAULT;
    ProgressDialog progress;
    // Android widgets
    private TextView mWelcomeText, mAadharText, mPhoneText;
    // Variables
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Requester mRequester;
    private Request mRequest = null;
    private BroadcastReceiver mEndedRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            findViewById(R.id.vehicle_layout).setVisibility(View.GONE);
            findViewById(R.id.look_at_map).setVisibility(View.GONE);
            ((UserClient) getApplicationContext()).setRequest(null);

            findViewById(R.id.submit_request).setVisibility(View.VISIBLE);
        }
    };
    private BroadcastReceiver vehicleReached = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mRequest = ((UserClient)getApplicationContext()).getRequest();
            mRequest.setVehiclereached(1);
            ((UserClient)getApplicationContext()).setRequest(mRequest);
        }

    };

    private String offlineReqVehicleContactNo;

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

        progress = new ProgressDialog(this);
        progress.setMessage("Loading your data");
        progress.setCancelable(false);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                vehicleReached, new IntentFilter("vehicle_reached")
        );

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mEndedRequest, new IntentFilter("r_request_ended")
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utilities.checkInternetConnectivity(this)){
            source = Source.CACHE;
            setRequestData();
        }else{
            source = Source.DEFAULT;
        }

        checkForRequests();

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getUserDetails();
            } else {
                getLocationPermission();
            }
        }
    }

    private void setRequestData() {
        SharedPreferences preferences = getSharedPreferences("smsrequest", MODE_PRIVATE);
        String req = preferences.getString("requestString", "");
        findViewById(R.id.vehicle_card).setVisibility(View.VISIBLE);
        findViewById(R.id.submit_request).setVisibility(View.GONE);

        String reqdetails[] = req.split("\\n");
        offlineReqVehicleContactNo = reqdetails[3];

        ((TextView)findViewById(R.id.driver_name)).setText(reqdetails[1]);
        ((TextView)findViewById(R.id.vehicle_number)).setText(reqdetails[2]);

    }

    private void checkForRequests() {
        Log.d(TAG, "checkForRequests: called");

        mRequest = ((UserClient)getApplicationContext()).getRequest();
        if(mRequest != null)
            displayVehicle();

        final Request[] request = new Request[1];
        FirebaseFirestore.getInstance().collection(getString(R.string.collection_request)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: task successful");
                            for (QueryDocumentSnapshot document : task.getResult())
                                if (document.exists()) {
                                    request[0] = document.toObject(Request.class);
                                    Log.d(TAG, "onComplete: "+request[0].toString());

                                    if (request[0].getRequester().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        mRequest = request[0];
                                        ((UserClient) getApplicationContext()).setRequest(mRequest);

                                        displayVehicle();
                                        return;
                                    }
                                }
                        }

                    }
                });

    }

    private boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(RequesterMainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(RequesterMainActivity.this, available, ERROR_DIALOG_REQUEST);
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
            getUserDetails();
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

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    getUserDetails();
                } else {
                    getLocationPermission();
                }
            }
        }

    }

    /* Helper methods */

    private void getUserDetails() {

        progress.show();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mRequester = new Requester();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {

                            mRequester = task.getResult().toObject(Requester.class);
                            mRequester.setTimeStamp(null);

                            Log.d(TAG, "Requester inside getUserDetails: " + mRequester.toString());
                            ((UserClient) (getApplicationContext())).setRequester(mRequester);

                            getLastKnownLocation();
                        }

                    }
                }
            });
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
                    GeoPoint geoPoint = (mLocation != null) ?
                            new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude()) : new GeoPoint(0, 0);

                    mRequester.setGeoPoint(geoPoint);

                    startLocationService();
                    displayRequester();
                }
            }
        });
    }

    public void displayRequester() {

        // todo: modify the card to have two parts. then update the number part from here

        mWelcomeText.setText("Welcome to SmartDispatch " + mRequester.getEmail().substring(0, mRequester.getEmail().indexOf("@")));
        mAadharText.setText("Aadhar Number: " + mRequester.getAadhar_number());
        mPhoneText.setText("Phone Number: " + mRequester.getPhone_number());
        progress.dismiss();

    }

    public void displayVehicle() {
        Log.d(TAG, "displayVehicle: ");
        findViewById(R.id.vehicle_card).setVisibility(View.VISIBLE);
        if(Utilities.checkInternetConnectivity(this))
            findViewById(R.id.look_at_map).setVisibility(View.VISIBLE);

        findViewById(R.id.submit_request).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.driver_name)).setText(mRequest.getVehicle().getDriver_name());
        ((TextView)findViewById(R.id.vehicle_number)).setText(mRequest.getVehicle().getVehicle_number());

    }

    /*  GPS Service  */

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                RequesterMainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.smartdispatch_auth.Services.LocationService".equals(service.service.getClassName())) {
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

                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.remove("type");
                editor.apply();

                if (isLocationServiceRunning()) {
                    Intent serviceIntent = new Intent(this, LocationService.class);
                    stopService(serviceIntent);
                }

                ((UserClient) getApplicationContext()).setRequester(null);
                ((UserClient) getApplicationContext()).setRequest(null);

                Intent intent = new Intent(RequesterMainActivity.this, EntryPoint.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            }

            case R.id.look_at_map: {

                Intent intent = new Intent(RequesterMainActivity.this, RequesterMapActivity.class);
                intent.putExtra("request", mRequest);
                startActivity(intent);
                break;
            }

            case R.id.submit_request: {
                Intent intent = new Intent(RequesterMainActivity.this, RequestForm.class);
                startActivity(intent);
                break;
            }

            case R.id.driver_phone: {
                Intent intent;
                if(Utilities.checkInternetConnectivity(this))
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mRequest.getVehicle().getPhone_number()));
                else
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + offlineReqVehicleContactNo));
                startActivity(intent);
            }
        }
    }


}

