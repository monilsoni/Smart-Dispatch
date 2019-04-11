package com.example.smartdispatch_auth.UI.Requester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UserClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.sql.Timestamp;

import static com.example.smartdispatch_auth.Constants.MY_PERMISSIONS_REQUEST_SEND_SMS;


public class RequestForm extends AppCompatActivity implements View.OnClickListener {

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

        //initialize firestore
        // mAuth = FirebaseAuth.getInstance();
        // db = FirebaseFirestore.getInstance();

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

        double latitude = 23.56, longitude = 26.56;
        GeoPoint location = new GeoPoint(latitude, longitude);
        GeoPoint location1 = new GeoPoint(23.1116, 72.5728);
        GeoPoint location2 = new GeoPoint(23.1859, 72.6213);

        //Find nearby vehicle and store vehicle id in variable
        Vehicle vehicle = new Vehicle("abcd","25","12345","1234567890","145236","123 1452 146","v_abc@gmail.com","1", location1, null);
        //Find nearby Hospital and store hospital id in variable

        Hospital hospital = new Hospital(location2,ts,"xyz","2");

        CollectionReference dbreq = FirebaseFirestore.getInstance().collection("Requests");


        Request request = new Request(
                requester,
                vehicle,
                hospital
        );


        dbreq.add(request)
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