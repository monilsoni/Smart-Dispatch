package com.example.smartdispatch_auth.UI.Admin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.example.smartdispatch_auth.R;

import java.util.ArrayList;
import java.util.List;


public class SMSRecyclerView extends AppCompatActivity implements View.OnClickListener {

    private android.support.v7.widget.RecyclerView recyclerView;
    private SMSRequestAdapter requestAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;

    List<SMSRequest> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sms);

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
                        "45",
                        "56",
                        "Pratik Rajani"
                )
        );

        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "45",
                        "56",
                        "Pratik Rajani"
                )
        );
        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "45",
                        "56",
                        "Pratik Rajani"
                )
        );
        requestList.add(
                new SMSRequest(
                        "Road Accident",
                        "2",
                        "25.23",
                        "58.52",
                        "45",
                        "56",
                        "Pratik Rajani"
                )
        );






        requestAdapter = new SMSRequestAdapter(this, requestList);
        recyclerView.setAdapter(requestAdapter);
    }

    @Override
    public void onClick(View view) {

    }
}
