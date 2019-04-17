package com.example.smartdispatch_auth.UI.Vehicle;

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

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.LocationService;
import com.example.smartdispatch_auth.UI.EntryPoint;
import com.example.smartdispatch_auth.UI.Requester.RequesterMainActivity;
import com.example.smartdispatch_auth.UI.Requester.RequesterMapActivity;
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
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_INTERNET;

public class VehicleMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VehicleMainActivity";
    Source source = Source.DEFAULT;
    ProgressDialog progress;
    IntentFilter filter;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Vehicle mVehicle;
    private Request mRequest;
    private boolean internetState =false, gpsState = false;
    private AlertDialog internetAlert, gpsAlert;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            FirebaseFirestore.getInstance().collection(getString(R.string.collection_request)).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Request request = document.toObject(Request.class);
                                    if (request.getVehicle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        mRequest = request;
                                        ((UserClient)getApplicationContext()).setRequest(request);

                                        displayRequestDetails();
                                        Toast.makeText(VehicleMainActivity.this, "Got the request!", Toast.LENGTH_SHORT).show();

                                        new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifRequester?id="+
                                                mRequest.getRequester().getUser_id()+
                                                "&name="+mRequest.getVehicle().getDriver_name()+
                                                "&no="+mRequest.getVehicle().getVehicle_number());

                                        Intent mapIntent = new Intent(VehicleMainActivity.this, VehicleMapActivity.class);
                                        mapIntent.putExtra("request", mRequest);
                                        startActivity(mapIntent);
                                    }

                                }
                            }

                        }

                    });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("get"));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.sign_out).setOnClickListener(this);
        findViewById(R.id.look_at_map).setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading your data");
        progress.setCancelable(false);

        filter = new IntentFilter();
        filter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(new CheckConnectivity(), filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    gpsState = true;
                }
                else{
                    getLocationPermission();
                }
            }

            case PERMISSIONS_REQUEST_ENABLE_INTERNET: {
                if (resultCode == RESULT_OK) {
                    internetState = true;
                }
            }
        }

        if(internetState && gpsState)
            getVehicleDetails();
        else
            Log.d(TAG, "onActivityResult: Did not switch on the network. Send broadcast from here");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isServicesOK()){
            if(mLocationPermissionGranted && internetState && gpsState){
                getVehicleDetails();
            }
            else{
                getLocationPermission();
            }
        }

        checkForRequests();
    }

    private void checkForRequests() {
        Log.d(TAG, "checkForRequests: called");

        mRequest = ((UserClient)getApplicationContext()).getRequest();
        if(mRequest != null){
            displayRequestDetails();
            Intent mapIntent = new Intent(VehicleMainActivity.this, VehicleMapActivity.class);
            mapIntent.putExtra("request", mRequest);
            startActivity(mapIntent);
        }


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

                                    if (request[0].getVehicle().getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        mRequest = request[0];

                                        displayRequestDetails();
                                        ((UserClient) getApplicationContext()).setRequest(mRequest);

                                        Intent mapIntent = new Intent(VehicleMainActivity.this, VehicleMapActivity.class);
                                        mapIntent.putExtra("request", mRequest);
                                        startActivity(mapIntent);
                                        return;
                                    }
                                }
                        }

                    }
                });

    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(VehicleMainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(VehicleMainActivity.this, available, ERROR_DIALOG_REQUEST);
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

    /* UI stuff */
    private void getVehicleDetails() {

        progress.show();
        if (mVehicle == null) {
            mVehicle = new Vehicle();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if(task.getResult().exists()){
                            Log.d(TAG, "onComplete: successfully set the requester client.");
                            mVehicle = task.getResult().toObject(Vehicle.class);
                            Log.d(TAG, "Requester inside getUserDetails: " + mVehicle.toString());

                            ((UserClient) (getApplicationContext())).setVehicle(mVehicle);

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
                    mVehicle.setTimeStamp(null);
                    GeoPoint geoPoint = new GeoPoint(0, 0);
                    try {
                        geoPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());

                    } catch (NullPointerException e) {
                        Log.d(TAG, "getLastKnownLocation: mLocation is null.");

                    }
                    mVehicle.setGeoPoint(geoPoint);

                    progress.dismiss();
                    startLocationService();
                    display();
                }
            }
        });
    }

    public void display() {

        ((TextView)findViewById(R.id.driver_name)).setText(mVehicle.getDriver_name());
        ((TextView)findViewById(R.id.contact_no)).setText(mVehicle.getPhone_number());
        ((TextView)findViewById(R.id.vehicle_no)).setText(mVehicle.getVehicle_number());
        ((TextView)findViewById(R.id.license_number)).setText(mVehicle.getLicense_number());

    }

    public void displayRequestDetails(){
        findViewById(R.id.user_card).setVisibility(View.VISIBLE);
        findViewById(R.id.hospital_card).setVisibility(View.VISIBLE);
        findViewById(R.id.look_at_map).setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.user_name)).setText(mRequest.getRequester().getName());
        ((TextView)findViewById(R.id.severity)).setText(Integer.toString(mRequest.getScaleofemergency()));

        ((TextView)findViewById(R.id.hospital_name)).setText(mRequest.getHospital().getHospital_name());
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                VehicleMainActivity.this.startForegroundService(serviceIntent);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_out:{
                FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles)).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("token", null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                    }
                });


                SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                editor.remove("type");
                editor.apply();

                if (isLocationServiceRunning()) {
                    Intent serviceIntent = new Intent(this, LocationService.class);
                    stopService(serviceIntent);
                }

                ((UserClient)getApplicationContext()).setVehicle(null);
                ((UserClient)getApplicationContext()).setRequest(null);

                Intent intent = new Intent(VehicleMainActivity.this, EntryPoint.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            }

            case R.id.look_at_map:{
                Intent intent = new Intent(VehicleMainActivity.this, VehicleMapActivity.class);
                intent.putExtra("request", mRequest);
                startActivity(intent);
                break;
            }
        }
    }

    public class CheckConnectivity extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {

            boolean isNotConnected = arg1.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if(isNotConnected){
                internetAlert = null;
                buildAlertMessageNoInternet();
            }else{
                internetState = true;
            }


            final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gpsState = true;
            }else{
                gpsAlert = null;
                buildAlertMessageNoGps();
            }

            if(gpsState && internetState)
                getVehicleDetails();
        }
    }


}
