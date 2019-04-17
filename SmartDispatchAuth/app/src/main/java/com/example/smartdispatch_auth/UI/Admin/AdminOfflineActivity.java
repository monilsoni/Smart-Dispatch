package com.example.smartdispatch_auth.UI.Admin;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.SMSRequest;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.SMSBroadcastReceiver;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.smartdispatch_auth.Constants.MY_APP_HASH;
import static com.example.smartdispatch_auth.Constants.MY_PERMISSIONS_PHONE_STATE;
import static com.example.smartdispatch_auth.Constants.MY_PERMISSIONS_REQUEST_SEND_SMS;
import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class AdminOfflineActivity extends AppCompatActivity implements SMSBroadcastReceiver.REQReceiveListener {

    private final static String TAG = "AdminOfflineAvtivity";

    List<SMSRequest> requestList;
    private android.support.v7.widget.RecyclerView recyclerView;
    private SMSRequestAdapter requestAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;
    ProgressDialog progress;
    private String mphone_no;
    private boolean mSMSpermissiongranted = false, mPhoneStatePermissionGranted = false;

    private SMSBroadcastReceiver smsbroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_offline);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                removeReceiver, new IntentFilter("remove"));


        progress = new ProgressDialog(this);
        progress.setMessage("Loading Data");
        progress.setCancelable(false);

        startsmslistner();
    }

    private BroadcastReceiver removeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int k = intent.getIntExtra("index", -1);
            Request request = intent.getParcelableExtra("request");

            Log.i(TAG, Integer.toString(k));
            if(requestList.size() != 0){
                requestList.remove(k);
                requestAdapter.notifyDataSetChanged();
                sendSMS(request);

            }

            if (requestList.size() == 0) {
                findViewById(R.id.empty_list_message).setVisibility(View.VISIBLE);
            }
        }
    };



    private void startsmslistner() {

        try {
            smsbroadcast = new SMSBroadcastReceiver();
            smsbroadcast.setREQListener(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            this.registerReceiver(smsbroadcast, intentFilter);

            SmsRetrieverClient client = SmsRetriever.getClient(this);

            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // API successfully started
                    //showToast("Listening");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Fail to start API
                    //showToast("Not Listening");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateList(){
        recyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.sosrecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        requestAdapter = new SMSRequestAdapter(this, requestList);
        recyclerView.setAdapter(requestAdapter);
    }


    private void updateUI() {
        findViewById(R.id.empty_list_message).setVisibility(View.INVISIBLE);
        progress.show();
        requestList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(getString(R.string.collection_offline_request)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SMSRequest request = document.toObject(SMSRequest.class);
                                requestList.add(request);
                            }
                        }
                        progress.dismiss();
                        if (requestList.size() != 0)
                            updateList();

                        else
                            findViewById(R.id.empty_list_message).setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = getSharedPreferences("activity", MODE_PRIVATE).edit();
        editor.putString("name", "Admin_offline");
        editor.apply();
        updateUI();

        if(!mSMSpermissiongranted)
            getSMSPermission();
        if(!mPhoneStatePermissionGranted)
            getPhoneStatePermission();
    }

    public void sendSMS(Request request) {

        String message = "<#>" + "\n" + request.getVehicle().getDriver_name() + "\n" + request.getVehicle().getVehicle_number()
        + "\n" + request.getVehicle().getPhone_number() + "\n" + MY_APP_HASH;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(request.getRequester().getPhone_number(), null, message, null, null);
        showToast("SMS sent");
    }

    private void showToast(String msg) {
        Toast.makeText(AdminOfflineActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onREQReceived(String req) {
        Parserequest(req);
    }

    @Override
    public void onREQTimeOut() {
        //showToast("REQ Time out");
    }

    @Override
    public void onREQReceivedError(String error) {
        //showToast(error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsbroadcast != null) {
            this.unregisterReceiver(smsbroadcast);
        }
    }

    private void Parserequest(String req) {

        SharedPreferences prefs = getSharedPreferences("activity", MODE_PRIVATE);
        if(prefs.getString("name", null).equals("Admin_offline")) {
            String data[] = req.split("\\n");
            SMSRequest request = new SMSRequest(data[1], data[2], data[3], data[4], data[5]);

            Log.d(TAG, request.toString());
            FirebaseFirestore.getInstance().collection(getString(R.string.collection_offline_request)).add(request);
            updateUI();
        }
    }

    private void getSMSPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            mSMSpermissiongranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
    }

    private void getPhoneStatePermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            mPhoneStatePermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_PHONE_STATE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                mSMSpermissiongranted = false;

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSMSpermissiongranted = true;
                }
            }

            case MY_PERMISSIONS_PHONE_STATE: {
                mPhoneStatePermissionGranted = false;

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSMSpermissiongranted = true;
                }
            }
        }
    }
}

