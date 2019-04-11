package com.example.smartdispatch_auth.UI.Hospital;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestDisp;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HospitalMainActivity extends AppCompatActivity {

    private static final String TAG = "HospitalMainActivity";

    // widgets
    TextView emptyListMessage;
    ProgressBar mProgressBar;
    // vars
    List<RequestDisp> requestList;
    List<Request> requests;
    RequestAdapter adapter;
    int recurrentRead = 0;
    private android.support.v7.widget.RecyclerView recyclerView;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;
    private BroadcastReceiver removeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int k = intent.getIntExtra("index", 0);
            requestList.remove(k);
            requests.remove(k);
            adapter.notifyDataSetChanged();
            if (requestList.size() == 0) {

                emptyListMessage.setVisibility(View.VISIBLE);
            }
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            updateUI(null);
        }

    };

    private void updateList() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RequestAdapter(getApplicationContext(), requestList, requests);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_main);

        emptyListMessage = findViewById(R.id.empty_list_message);
        mProgressBar = findViewById(R.id.progressBar);
        recyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.recyclerView);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("send"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                removeReceiver, new IntentFilter("remove"));

    }

    private void updateUI(FirebaseUser user) {
        Utilities.showDialog(mProgressBar);
        recurrentRead = 0;
        requestList = new ArrayList<>();
        requests = new ArrayList<>();
        requests.clear();
        requestList.clear();

        FirebaseFirestore.getInstance().collection("Requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        recurrentRead++;
                        if (recurrentRead == 1) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Request request = document.toObject(Request.class);

                                    if (true){
                                        Requester usr = request.getRequester();
                                        Vehicle v = request.getVehicle();
                                        String usrname = usr.getName(), usrage = usr.getAge(), usrsex = usr.getSex();
                                        String drivername = v.getDriver_name(), contactno = v.getPhone_number(), vehicleno = v.getVehicle_number();
                                        requestList.add(new RequestDisp(usrname, usrage, usrsex, drivername, contactno, vehicleno));
                                        requests.add(request);

                                        DocumentReference newUserRef = FirebaseFirestore.getInstance()
                                                .collection(getString(R.string.collection_vehicles))
                                                .document(request.getVehicle().getUser_id());

                                        newUserRef.set(request.getVehicle()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {
                                                     Toast.makeText(HospitalMainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                                }else if(task.getException() != null){
                                                    Toast.makeText(HospitalMainActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(HospitalMainActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                        newUserRef = FirebaseFirestore.getInstance()
                                                .collection(getString(R.string.collection_hospital))
                                                .document(request.getHospital().getUser_id());

                                        newUserRef.set(request.getHospital()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {
                                                    Toast.makeText(HospitalMainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                                }else if(task.getException() != null){
                                                    Toast.makeText(HospitalMainActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(HospitalMainActivity.this, "Something went wrong: FireStore", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                                    }
                                    Log.d("data", document.getId() + " => " + document.getData());

                                }
                            } else {
                                Log.d("data", "Error getting documents: ", task.getException());
                            }

                            Utilities.hideDialog(mProgressBar);
                            if (requestList.size() != 0)
                                updateList();

                            else
                                emptyListMessage.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        emptyListMessage.setVisibility(View.INVISIBLE);
        updateUI(null);
    }


}