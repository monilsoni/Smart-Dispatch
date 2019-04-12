package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;
import java.lang.Float;

import static com.example.smartdispatch_auth.Constants.MY_PERMISSIONS_REQUEST_SEND_SMS;


public class RequestForm extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RequestForm";

    //Android Widgets
    private CheckBox mChkbox_medical,mChkbox_fire,mChkbox_other;
    private SeekBar mSeek_severity;

    //Variables

    private int severity;

    //requestVariables
    private String typeofemergency;
    private int scaleofemergency;
    private GeoPoint location;

    //convert to vehicle and hospital objects
    private String vehicleid;
    private String hospitalid;
    private Requester requester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);


        mChkbox_medical = findViewById(R.id.checkBox_medical);
        mChkbox_fire = findViewById(R.id.checkBox_fire);
        mChkbox_other = findViewById(R.id.checkBox_other);
        mSeek_severity = findViewById(R.id.seekBar_severity);

        findViewById(R.id.sendrequestButton).setOnClickListener(this);
        findViewById(R.id.sendSMSButton).setOnClickListener(this);

        requester = ((UserClient) getApplicationContext()).getRequester();
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

        Log.d(TAG,"presses send");
        //createRequest();
        String typeofemergency="";
        if(mChkbox_medical.isChecked())
            typeofemergency+="Medical ";
        if(mChkbox_fire.isChecked())
            typeofemergency+="Fire ";
        if(mChkbox_other.isChecked())
            typeofemergency+="Other";

        int scaleofemergency = mSeek_severity.getProgress();


        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        // double latitude = 23.56, longitude = 26.56;
        Location currloc = new Location("");
        currloc.setLongitude(requester.getGeoPoint().getLongitude());
        currloc.setLatitude(requester.getGeoPoint().getLatitude());

        GeoPoint location1 = new GeoPoint(23.1116, 72.5728);
        GeoPoint location2 = new GeoPoint(23.1859, 72.6213);

        ArrayList<Vehicle> vehicles_list = new ArrayList<Vehicle>();



        FirebaseFirestore.getInstance().collection("Cluster Main").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        float temp = Float.MAX_VALUE;
                        Cluster nearestCluster = new Cluster();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Cluster c = document.toObject(Cluster.class);

                            Location l1 = new Location("");
                            l1.setLongitude(c.getGeoPoint().getLatitude());
                            l1.setLongitude(c.getGeoPoint().getLongitude());

                            if (temp >= currloc.distanceTo(l1)) {
                                temp = currloc.distanceTo(l1);
                                nearestCluster = c;
                            }
                        }

                        Vehicle nearestVehicle;
                        int vehicles_count = nearestCluster.getVehicles().size();

                        int x = (int)(Math.random()*((vehicles_count-0)+1)) + 0;
                        nearestVehicle = nearestCluster.getVehicles().get(x);




                        FirebaseFirestore.getInstance().collection("Hospital").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    float temp = Float.MAX_VALUE;
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        Hospital nearestHospital = new Hospital();

                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            Hospital h = document.toObject(Hospital.class);

                                            Location l1 = new Location("");
                                            l1.setLongitude(h.getGeoPoint().getLatitude());
                                            l1.setLongitude(h.getGeoPoint().getLongitude());

                                            if (temp >= currloc.distanceTo(l1)) {
                                                temp = currloc.distanceTo(l1);
                                                nearestHospital = h;
                                            }
                                        }


                                        Request request = new Request(
                                                requester,
                                                nearestVehicle,
                                                nearestHospital
                                        );


                                        FirebaseFirestore.getInstance().collection("Requests").add(request)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        showToast("Request stored");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                showToast("Request failed");
                                            }
                                        });

                                    }
                                });


                    }
                });

    }

    public void onclickSendSmS()
    {

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
        }
        else
            sendSMS();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
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

    public void sendSMS()
    {
        String typeofemergency="";
        if(mChkbox_medical.isChecked())
            typeofemergency+="Medical ";
        if(mChkbox_fire.isChecked())
            typeofemergency+="Fire ";
        if(mChkbox_other.isChecked())
            typeofemergency+="Other";

        int scaleofemergency = mSeek_severity.getProgress();

        double latitude = 23.56, longitude = 26.56;
        GeoPoint location = new GeoPoint(latitude, longitude);

        //Find nearby vehicle and store vehicle id in variable

        String vehicleid = "1";


        //Find nearby Hospital and store hospital id in variable
        String hospitalid = "2";

        String message = "<#>" + "\n" + typeofemergency + "\n" + scaleofemergency + "\n" + location.getLatitude()
                + "\n" + location.getLongitude() + "\n" + vehicleid + "\n" + hospitalid + "\n" + requester.getEmail() + "\n" +"MamEVHTp4dw";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("8347747701", null, message, null, null);
        showToast("SMS sent");
    }

    private void createRequest() {
        String typeofemergency=null;
        if(mChkbox_medical.isChecked())
            typeofemergency+="Medical ";
        if(mChkbox_fire.isChecked())
            typeofemergency+="Fire ";
        if(mChkbox_other.isChecked())
            typeofemergency+="Other";

        int scaleofemergency = mSeek_severity.getProgress();

        double latitude = 23.56, longitude = 26.56;
        GeoPoint location1 = new GeoPoint(23.1116, 72.5728);
        GeoPoint location2 = new GeoPoint(23.1859, 72.6213);


        //Find nearby vehicle and store vehicle id in variable

        String vehicleid = "1";

        //Find nearby Hospital and store hospital id in variable
        String hospitalid = "2";



    }

    private void showToast(String msg)
    {
        Toast.makeText(RequestForm.this,msg,Toast.LENGTH_LONG).show();
    }



}