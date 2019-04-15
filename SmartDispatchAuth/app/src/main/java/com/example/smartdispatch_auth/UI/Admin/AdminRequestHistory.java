package com.example.smartdispatch_auth.UI.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.smartdispatch_auth.Models.CurrentRequest;
import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestDisp;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.Hospital.RequestAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminRequestHistory extends AppCompatActivity {

    private static final String TAG = "AdminRequestHistory";
    private android.support.v7.widget.RecyclerView recyclerView;
    private CurrentRequestAdapter requestAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;
    String requestType;
    int recurrentRead;
    ProgressDialog progress;
    TextView emptyListMessage;

    List<CurrentRequest> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request_history);

        emptyListMessage = findViewById(R.id.empty_message);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading Data");
        progress.setCancelable(false);
        recyclerView = findViewById(R.id.hrrecyclerView);
    }

    private void updateList() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        requestAdapter = new CurrentRequestAdapter(getApplicationContext(), requestList);
        recyclerView.setAdapter(requestAdapter);
    }

    public void updateUI(){
        emptyListMessage.setVisibility(View.INVISIBLE);
        progress.show();
        recurrentRead = 0;
        requestList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("Request History").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                recurrentRead++;
                if (recurrentRead == 1) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            Request request = document.toObject(Request.class);

                            //Requester requester = request.getRequester();
                            Vehicle vehicle = request.getVehicle();
                            Hospital hospital = request.getHospital();

                            requestList.add(new CurrentRequest(
                                    hospital.getHospital_name(),
                                    hospital.getContactno(),
                                    vehicle.getDriver_name(),
                                    vehicle.getPhone_number(),
                                    vehicle.getVehicle_number()
                            ));

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

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
