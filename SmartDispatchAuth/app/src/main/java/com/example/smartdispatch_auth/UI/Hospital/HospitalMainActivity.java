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

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.Utils.RequestAdapter;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HospitalMainActivity extends AppCompatActivity {

    // widgets
    TextView emptyListMessage;
    ProgressBar mProgressBar;
    // vars
    List<Request> requestList;
    RequestAdapter adapter;
    int recurrentRead = 0;
    private android.support.v7.widget.RecyclerView recyclerView;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;
    private BroadcastReceiver removeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int k = intent.getIntExtra("index", 0);
            requestList.remove(k);
            adapter.notifyDataSetChanged();
            if (requestList.size() == 0) {

                emptyListMessage.setVisibility(View.VISIBLE);
            }
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            ArrayList<String> list = intent.getStringArrayListExtra("Data");
            String usrname = "hey", usrage = "hey", usrsex = "hey", drivername = "hey", contactno = "hey", vehicleno = "hey";
            for (int i = 0; i < 12; i += 2) {
                String value = list.get(i);
                switch (value) {
                    case "usrname":
                        usrname = list.get(i + 1);
                        break;

                    case "usrage":
                        usrage = list.get(i + 1);
                        break;

                    case "usrsex":
                        usrsex = list.get(i + 1);
                        break;

                    case "drivername":
                        drivername = list.get(i + 1);
                        break;

                    case "contactno":
                        contactno = list.get(i + 1);
                        break;

                    case "vehicleno":
                        vehicleno = list.get(i + 1);
                        break;

                }

            }


            if (emptyListMessage.getVisibility() == View.VISIBLE) {

                requestList.clear();
                requestList.add(new Request(usrname, usrage, usrsex, drivername, contactno, vehicleno));
                updateList();

            } else {
                requestList.add(new Request(usrname, usrage, usrsex, drivername, contactno, vehicleno));
                adapter.notifyDataSetChanged();
            }

        }
    };

    private void updateList() {
        emptyListMessage.setVisibility(View.INVISIBLE);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RequestAdapter(getApplicationContext(), requestList);
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
        recurrentRead = 0;
        requestList = new ArrayList<>();
        requestList.clear();

        FirebaseFirestore.getInstance().collection("Hospital")
                .document("Hospital1")
                .collection("requests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        recurrentRead++;
                        if (recurrentRead == 1) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    requestList.add(
                                            new Request(
                                                    document.getData().get("usrname").toString(),
                                                    document.getData().get("usrage").toString(),
                                                    document.getData().get("usrsex").toString(),
                                                    document.getData().get("drivername").toString(),
                                                    document.getData().get("contactno").toString(),
                                                    document.getData().get("vehicleno").toString()
                                            )
                                    );
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

        Utilities.showDialog(mProgressBar);
        updateUI(null);
    }


}
