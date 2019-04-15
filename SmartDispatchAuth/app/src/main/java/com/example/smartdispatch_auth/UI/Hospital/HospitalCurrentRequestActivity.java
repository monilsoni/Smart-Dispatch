package com.example.smartdispatch_auth.UI.Hospital;

import android.app.ProgressDialog;
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
import android.widget.TextView;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestDisp;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HospitalCurrentRequestActivity extends AppCompatActivity {

    private static final String TAG = "HospitalCurrReqActivity";

    List<RequestDisp> requestList;
    List<Request> requests;
    RequestAdapter adapter;
    int recurrentRead = 0;
    TextView emptyListMessage;

    // widgets
    ProgressDialog progress;

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
            updateUI();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_current_request);
        recyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.recyclerView);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("send"));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                removeReceiver, new IntentFilter("remove"));

        emptyListMessage = findViewById(R.id.empty_list_message);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading Data");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    @Override
    protected void onResume() {
        super.onResume();
        emptyListMessage.setVisibility(View.INVISIBLE);
        updateUI();
    }

    private void updateList() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RequestAdapter(getApplicationContext(), requestList, requests);
        recyclerView.setAdapter(adapter);
    }

    private void updateUI() {
        progress.show();
        recurrentRead = 0;
        requestList = new ArrayList<>();
        requests = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("Requests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                recurrentRead++;
                if (recurrentRead == 1) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Request request = document.toObject(Request.class);
                            String id = request.getHospital().getUser_id();

                            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(id)) {
                                Requester requester = request.getRequester();
                                Vehicle vehicle = request.getVehicle();

                                requestList.add(new RequestDisp(
                                        requester.getName(),
                                        requester.getAge(),
                                        requester.getSex(),
                                        vehicle.getDriver_name(),
                                        vehicle.getPhone_number(),
                                        vehicle.getVehicle_number()
                                ));
                                requests.add(request);
                            }
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }

                    progress.dismiss();
                    if (requestList.size() != 0)
                        updateList();

                    else
                        emptyListMessage.setVisibility(View.VISIBLE);

                }
            }
        });
    }

}
