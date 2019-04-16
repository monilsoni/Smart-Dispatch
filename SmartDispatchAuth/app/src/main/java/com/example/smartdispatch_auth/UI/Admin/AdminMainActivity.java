package com.example.smartdispatch_auth.UI.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Cluster;
import com.example.smartdispatch_auth.Models.SMSRequest;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.Models.ClusterFetch;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Services.SMSBroadcastReceiver;
import com.example.smartdispatch_auth.UI.LoginActivity;
import com.example.smartdispatch_auth.UI.RegisterActivity;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity implements View.OnClickListener, SMSBroadcastReceiver.REQReceiveListener{

    private final static String TAG = "AdminMainActivity";

    private static ArrayList<Vehicle> vehicles;
    private static ProgressDialog progress;
    private SMSBroadcastReceiver smsbroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        findViewById(R.id.allocateVehicle).setOnClickListener(this);
        findViewById(R.id.current_request).setOnClickListener(this);
        findViewById(R.id.request_history).setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setMessage("Creating clusters and assigning vehicles...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        startsmslistner();

    }

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


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.allocateVehicle: {
                progress.show();

                FirebaseFirestore.getInstance().collection(getString(R.string.collection_vehicles)).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){

                                    QuerySnapshot queryDocumentSnapshot = task.getResult();
                                    if(queryDocumentSnapshot != null)
                                        new GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/retrieve?vehicles=" + queryDocumentSnapshot.size());


                                }
                            }
                        });


                break;
            }

            case R.id.current_request:{
                Intent intent = new Intent(AdminMainActivity.this, AdminCurrentRequest.class);
                startActivity(intent);
                break;
            }

            case R.id.request_history: {
                Intent intent = new Intent(AdminMainActivity.this, AdminRequestHistory.class);
                startActivity(intent);
                break;
            }



        }

    }

    private static class GetUrlContentTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... url2) {
            String content = null;

            try {
                URL url = new URL(url2[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                content = "";
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }

                vehicles = new ArrayList<>();

                FirebaseFirestore.getInstance().collection("Cluster Main").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                                FirebaseFirestore.getInstance().collection("Vehicles").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        for (QueryDocumentSnapshot document : task.getResult())
                                            vehicles.add(document.toObject(Vehicle.class));

                                        Log.d(TAG, vehicles.toString());

                                        FirebaseFirestore.getInstance().collection("clusters").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                int count = 0, id = 0;

                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    ClusterFetch c = document.toObject(ClusterFetch.class);
                                                    GeoPoint p = new GeoPoint(Double.parseDouble(c.getX()), Double.parseDouble(c.getY()));
                                                    ArrayList<Vehicle> v = new ArrayList<>();

                                                    for (int i = 0; i < c.getNoOfVehicles(); i++, count++) {
                                                        v.add(vehicles.get(count));
                                                    }
                                                    Cluster cluster = new Cluster(Integer.toString(id), p, v);
                                                    FirebaseFirestore.getInstance().collection("Cluster Main").add(cluster);
                                                    id++;
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });


                progress.dismiss();
            } catch (IOException e) {

                Log.d(TAG, "doInBackground: " + e.toString());
            }

            return content;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    @Override
    public void onREQReceived(String req) {

       // showToast("recieved");

        Parserequest(req);
        //t_type.setText(req);

        if (smsbroadcast != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsbroadcast);
        }

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
            LocalBroadcastManager.getInstance(this).unregisterReceiver(smsbroadcast);
        }
    }

    private void Parserequest(String req) {

        String data[] = req.split("\\n");

        // Todo: where is this used?
        SMSRequest request = new SMSRequest(data[1],data[2],data[3],data[4],data[5]);

    }


}