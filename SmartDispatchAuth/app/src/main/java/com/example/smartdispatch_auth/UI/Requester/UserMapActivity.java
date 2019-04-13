package com.example.smartdispatch_auth.UI.Requester;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.PolylineData;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestClusterMarker;
import com.example.smartdispatch_auth.Models.Requester;

import com.example.smartdispatch_auth.Models.User;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Utils.RequestClusterManagerRenderer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import static com.example.smartdispatch_auth.Constants.MAPVIEW_BUNDLE_KEY;

public class UserMapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnPolylineClickListener {

    private static final String TAG = "UserMapActivity";
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    // widgets
    private MapView mMapView;

    // vars
    private ArrayList<User> mUserLocations = new ArrayList<>();
    private GoogleMap mGoogleMap;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private GeoApiContext mGeoApiContext = null;
    private boolean toggle = true;

    // Cluster Manager and Cluster Manager Renderer are actually responsible for putting the markers on the map
    private ClusterManager mClusterManager;
    private RequestClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<RequestClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();


    private Request mRequest;
    private Requester mRequester;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        findViewById(R.id.btn_reset_map).setOnClickListener(this);
        findViewById(R.id.btn_go).setOnClickListener(this);


        Intent intent = getIntent();
        if (intent != null) {
            mRequest = intent.getParcelableExtra("request");

            mRequester = mRequest.getRequester();

            mUserLocations.add(mRequest.getVehicle());
            mUserLocations.add(mRequest.getHospital());
            mUserLocations.add(mRequester);

            Log.d(TAG, "Request: " + mRequest.toString());

        }

        initGoogleMap(savedInstanceState);
    }

    /* Helper methods */

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.cluster_map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_map_api_key))
                    .build();
        }
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (mPolylinesData.size() > 0) {
                    for (PolylineData polylineData : mPolylinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }

                double duration = Double.MAX_VALUE;

                int i = 0;
                for (DirectionsRoute route : result.routes) {
                    i++;
                    Log.d(TAG, "run: i=" + i);
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline); // this simulates the click. Important!
                        zoomRoute(polyline.getPoints());
                    }

                }
            }
        });
    }

    private void calculateDirections(GeoPoint start, GeoPoint end) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                end.getLatitude(),
                end.getLongitude()
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        start.getLatitude(),
                        start.getLongitude()
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());

        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    private void startUserLocationsRunnable() {
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations() {
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users");

        try {
            for (final RequestClusterMarker clusterMarker : mClusterMarkers) {

                switch (clusterMarker.getUser().getType()){
                    case "requester": {
                        DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                                .collection(getString(R.string.collection_users))
                                .document(clusterMarker.getUser().getUser_id());
                        userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        Requester updatedUserLocation = task.getResult().toObject(Requester.class);
                                        clusterMarker.setPosition(new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()
                                        ));
                                    }else{
                                        Log.d(TAG, "onComplete: Did not find any documents like this");
                                    }
                                }
                            }
                        });
                        break;
                    }

                    case "vehicle": {
                        DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                                .collection(getString(R.string.collection_vehicles))
                                .document(clusterMarker.getUser().getUser_id());
                        userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        Vehicle updatedUserLocation = task.getResult().toObject(Vehicle.class);
                                        clusterMarker.setPosition(new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()
                                        ));
                                    }else{
                                        Log.d(TAG, "onComplete: Did not find any documents like this");
                                    }

                                }
                            }
                        });
                        break;
                    }

                    case "hospital": {
                        DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                                .collection(getString(R.string.collection_hospital))
                                .document(clusterMarker.getUser().getUser_id());

                        userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        Hospital updatedUserLocation = task.getResult().toObject(Hospital.class);
                                        clusterMarker.setPosition(new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()
                                        ));
                                    }else{
                                        Log.d(TAG, "onComplete: Did not find any documents like this");
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage());
        }

    }

    private void resetMap() {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            if (mClusterManager != null) {
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }

            if (mPolylinesData.size() > 0) {
                mPolylinesData.clear();
                mPolylinesData = new ArrayList<>();
            }
        }
    }

    private void addMapMarkers() {
        if (mGoogleMap != null) {

            resetMap();

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<RequestClusterMarker>(this, mGoogleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new RequestClusterManagerRenderer(
                        this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            //mGoogleMap.setOnInfoWindowClickListener(this);

            for (User user : mUserLocations) {

                Log.d(TAG, "addMapMarkers: location: " + user.getGeoPoint().toString());
                try {
                    String snippet = "";
                    if (user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        snippet = "This is you";
                    } else {
                        snippet = "Determine route to " + user.getEmail() + "?";
                    }

                    int avatar = R.drawable.ic_launcher_background; // set the default avatar

                    RequestClusterMarker newClusterMarker = new RequestClusterMarker(
                            new LatLng(user.getGeoPoint().getLatitude(), user.getGeoPoint().getLongitude()),
                            user.getEmail(),
                            snippet,
                            avatar,
                            user
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();

            setCameraView();
        }
    }

    private void setCameraView() {

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen


        // Set a boundary to start
        double bottomBoundary = mRequester.getGeoPoint().getLatitude() - .1;
        double leftBoundary = mRequester.getGeoPoint().getLongitude() - .1;
        double topBoundary = mRequester.getGeoPoint().getLatitude() + .1;
        double rightBoundary = mRequester.getGeoPoint().getLongitude() + .1;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, width, height, padding));

    }

    /* Override methods */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mGoogleMap = map;
        addMapMarkers();
        mGoogleMap.setOnPolylineClickListener(this);

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
        stopLocationUpdates();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_map: {
                addMapMarkers();
                break;
            }

            case R.id.btn_go:{
                if(toggle){
                    calculateDirections(mRequest.getVehicle().getGeoPoint(),
                            mRequest.getRequester().getGeoPoint());
                    toggle = false;
                }else{
                    calculateDirections(mRequest.getRequester().getGeoPoint(),
                            mRequest.getHospital().getGeoPoint());
                    toggle = true;
                }
            }
        }
    }


    @Override
    public void onPolylineClick(Polyline polyline) {

        for (PolylineData polylineData : mPolylinesData) {
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                Toast.makeText(UserMapActivity.this, "Trip Duration: " + polylineData.getLeg().duration, Toast.LENGTH_LONG).show();
            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }


}
