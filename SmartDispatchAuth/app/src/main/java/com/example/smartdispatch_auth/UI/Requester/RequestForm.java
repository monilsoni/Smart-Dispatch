package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Cluster;
import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UserClient;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.lang.Float;
import java.util.Objects;

import static com.example.smartdispatch_auth.Constants.MY_PERMISSIONS_REQUEST_SEND_SMS;


public class RequestForm extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RequestForm";
    private static Location currloc;
    //Android Widgets
    private CheckBox mChkbox_medical, mChkbox_fire, mChkbox_other;

    //Variables
    private SeekBar mSeek_severity;
    private ProgressDialog progress;
    private Requester requester;

    public static Comparator<Cluster> sortClusters() {
        Comparator comp = new Comparator<Cluster>() {
            @Override
            public int compare(Cluster c1, Cluster c2) {
                Location l1 = new Location("");
                Location l2 = new Location("");

                l1.setLatitude(c1.getGeoPoint().getLatitude());
                l1.setLongitude(c1.getGeoPoint().getLongitude());

                l2.setLatitude(c2.getGeoPoint().getLatitude());
                l2.setLongitude(c2.getGeoPoint().getLongitude());

                float dist1 = currloc.distanceTo(l1);
                float dist2 = currloc.distanceTo(l2);

                return (dist1 < dist2) ? 1 : 0;
            }
        };
        return comp;
    }

    public static Comparator<Vehicle> sortVehicles() {
        Comparator comp = new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle c1, Vehicle c2) {
                Location l1 = new Location("");
                Location l2 = new Location("");

                l1.setLatitude(c1.getGeoPoint().getLatitude());
                l1.setLongitude(c1.getGeoPoint().getLongitude());

                l2.setLatitude(c2.getGeoPoint().getLatitude());
                l2.setLongitude(c2.getGeoPoint().getLongitude());

                float dist1 = currloc.distanceTo(l1);
                float dist2 = currloc.distanceTo(l2);

                return (dist1 < dist2) ? 1 : 0;
            }
        };
        return comp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);

        progress = new ProgressDialog(this);
        progress.setMessage("Sending Request");
        progress.setCancelable(false);

        mChkbox_medical = findViewById(R.id.checkBox_medical);
        mChkbox_fire = findViewById(R.id.checkBox_fire);
        mChkbox_other = findViewById(R.id.checkBox_other);
        mSeek_severity = findViewById(R.id.seekBar_severity);

        findViewById(R.id.sendrequestButton).setOnClickListener(this);
        findViewById(R.id.sendSMSButton).setOnClickListener(this);

        requester = ((UserClient) getApplicationContext()).getRequester();

        currloc = new Location("");
        currloc.setLongitude(requester.getGeoPoint().getLongitude());
        currloc.setLatitude(requester.getGeoPoint().getLatitude());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendrequestButton: {
                onclickSend();
                break;
            }

            case R.id.sendSMSButton: {
                onclickSendSmS();
                break;
            }

        }
    }

    public void onclickSend() {

        progress.show();
        Log.d(TAG, "presses send");


        FirebaseFirestore.getInstance().collection(getString(R.string.collection_cluster_main)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cluster c = document.toObject(Cluster.class);
                            clusters.add(c);
                        }

                        Collections.sort(clusters, sortClusters());

                        ArrayList<Vehicle> Available_Vehicles = new ArrayList<>();

                        for (Cluster cluster : clusters) {
                            Available_Vehicles.clear();

                            for (Vehicle vehicle : cluster.getVehicles()) {
                                if (vehicle.getEngage() == 0)
                                    Available_Vehicles.add(vehicle);
                            }
                            if (Available_Vehicles.size() > 0)
                                break;

                        }


                        Vehicle nearestVehicle;
                        int vehicles_count = Available_Vehicles.size();
                        if (vehicles_count == 0){
                            Toast.makeText(getApplicationContext(), "All the Vehicles are busy. Please try again later.", Toast.LENGTH_LONG).show();
                            progress.dismiss();

                        } else {

                            Collections.sort(Available_Vehicles, sortVehicles());

                            nearestVehicle = Available_Vehicles.get(0);

                            final String[] token = new String[1];
                            FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles))
                                    .document(nearestVehicle.getUser_id()).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful() && task.getResult().exists()) {
                                                token[0] = task.getResult().toObject(Vehicle.class).getToken();
                                                nearestVehicle.setToken(token[0]);
                                            }
                                        }
                                    });

                            FirebaseFirestore.getInstance().collection(getString(R.string.collection_hospitals)).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           float temp = Float.MAX_VALUE;

                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Hospital nearestHospital = new Hospital();

                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                Hospital h = document.toObject(Hospital.class);
                                                Log.d(TAG, h.toString());
                                                Location l1 = new Location("");
                                                l1.setLongitude(h.getGeoPoint().getLatitude());
                                                l1.setLongitude(h.getGeoPoint().getLongitude());

                                                if (h.getUser_id() != null && temp >= currloc.distanceTo(l1) && h.getToken()!=null) {
                                                    temp = currloc.distanceTo(l1);
                                                    nearestHospital = h;
                                                }
                                            }

                                            String typeofemergency = "";
                                            if (mChkbox_medical.isChecked()) {
                                                if (!Objects.equals(typeofemergency, ""))
                                                    typeofemergency += ",";
                                                typeofemergency += "Medical ";
                                            }

                                            if (mChkbox_fire.isChecked()) {
                                                if (!Objects.equals(typeofemergency, ""))
                                                    typeofemergency += ",";
                                                typeofemergency += "Fire ";
                                            }

                                            if (mChkbox_other.isChecked()) {
                                                if (!Objects.equals(typeofemergency, ""))
                                                    typeofemergency += ",";
                                                typeofemergency += "Other";
                                            }

                                            int scaleofemergency = mSeek_severity.getProgress();

                                            Request request = new Request(
                                                    requester,
                                                    nearestVehicle,
                                                    nearestHospital,
                                                    typeofemergency,
                                                    scaleofemergency,
                                                    0,
                                                    requester.getUser_id()
                                            );


                                            Log.d(TAG, "onComplete: Request: " + request.toString());

                                            FirebaseFirestore.getInstance().collection(getString(R.string.collection_request)).document(requester.getUser_id()).set(request)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "onSuccess: Request Stored.");

                                                            FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles))
                                                                    .document(nearestVehicle.getUser_id())
                                                                    .update("engage", 1)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.w(TAG, "Error updating document", e);
                                                                        }
                                                                    });


                                                            progress.dismiss();

                                                            Intent intent = new Intent(RequestForm.this, RequesterMainActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            ((UserClient) getApplicationContext()).setRequest(request);

                                                            new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifVehicle?id=" +
                                                                    request.getVehicle().getUser_id() +
                                                                    "&name=" + request.getRequester().getName());

                                                            new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifHospital?id=" +
                                                                    request.getHospital().getUser_id() +
                                                                    "&name=" + request.getRequester().getName());

                                                            startActivity(intent);
                                                            finish();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progress.dismiss();
                                                    Log.d(TAG, "onFailure: Request failed to store.");
                                                }
                                            });

                                        }
                                    });
                        }

                    }
                });

    }

    public void onclickSendSmS() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }
        } else
            sendSMS();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                } else {

                    return;
                }
            }
        }

    }

    public void sendSMS() {
        String typeofemergency = "";
        if (mChkbox_medical.isChecked())
            typeofemergency += "Medical ";
        if (mChkbox_fire.isChecked())
            typeofemergency += "Fire ";
        if (mChkbox_other.isChecked())
            typeofemergency += "Other";

        int scaleofemergency = mSeek_severity.getProgress();

        double latitude = 23.56, longitude = 26.56;
        GeoPoint location = new GeoPoint(latitude, longitude);

        String vehicleid = "1";

        String hospitalid = "2";

        String message = "<#>" + "\n" + typeofemergency + "\n" + scaleofemergency + "\n" + location.getLatitude()
                + "\n" + location.getLongitude() + "\n" + vehicleid + "\n" + hospitalid + "\n" + requester.getEmail() + "\n" + "MamEVHTp4dw";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("8347747701", null, message, null, null);
        showToast("SMS sent");
    }

    private void showToast(String msg) {
        Toast.makeText(RequestForm.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utilities.checkInternetConnectivity(this)) {
            findViewById(R.id.sendSMSButton).setVisibility(View.GONE);
        } else {
            findViewById(R.id.sendrequestButton).setVisibility(View.GONE);
        }
    }
}