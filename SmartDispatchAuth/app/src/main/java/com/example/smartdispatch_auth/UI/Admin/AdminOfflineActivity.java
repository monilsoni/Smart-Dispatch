package com.example.smartdispatch_auth.UI.Admin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.example.smartdispatch_auth.Models.SMSRequest;
import com.example.smartdispatch_auth.R;

import java.util.ArrayList;
import java.util.List;

public class AdminOfflineActivity extends AppCompatActivity {

    List<SMSRequest> requestList;
    private android.support.v7.widget.RecyclerView recyclerView;
    private SMSRequestAdapter requestAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_offline);

        requestList = new ArrayList<>();
        recyclerView = (android.support.v7.widget.RecyclerView) findViewById(R.id.sosrecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "Pratik Rajani"
                )
        );

        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "Pratik Rajani"
                )
        );
        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "Pratik Rajani"
                )
        );
        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "Pratik Rajani"
                )
        );






        requestAdapter = new SMSRequestAdapter(this, requestList);
        recyclerView.setAdapter(requestAdapter);
    }
}
