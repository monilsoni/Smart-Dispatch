package com.example.smartdispatch_auth.UI.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.example.smartdispatch_auth.R;

import java.util.ArrayList;
import java.util.List;


public class HistoryReqRV extends AppCompatActivity implements View.OnClickListener {

    private android.support.v7.widget.RecyclerView recyclerView;
    private CurrentRequestAdapter requestAdapter;
    private android.support.v7.widget.RecyclerView.LayoutManager layoutManager;
    String requestType;

    List<CurrentRequest> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_history_request);

        Intent intent = getIntent();
        requestType = intent.getStringExtra("requestType");

        requestList = new ArrayList<>();

        recyclerView = findViewById(R.id.hrrecyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        requestList.add(
                new CurrentRequest(
                        "fhcgvhbjnk",
                        "fcgvhbjnkm",
                        "Pratik",
                        "1546523542",
                        "GJ5 RG 4565"
                )
        );
        requestList.add(
                new CurrentRequest(
                        "fhcgvhbjnk",
                        "fcgvhbjnkm",
                        "Pratik",
                        "1546523542",
                        "GJ5 RG 4565"
                )
        );
        requestList.add(
                new CurrentRequest(
                        "fhcgvhbjnk",
                        "fcgvhbjnkm",
                        "Pratik",
                        "1546523542",
                        "GJ5 RG 4565"
                )
        );
         requestList.add(
                new CurrentRequest(
                        "Padmavat",
                        "Rajasthan",
                        "Sanjay Lila Bhansali",
                        "1234551324",
                        "RJ4 GH 1245"
                )
        );

        requestAdapter = new CurrentRequestAdapter(this, requestList);
        recyclerView.setAdapter(requestAdapter);

    }

    @Override
    public void onClick(View view) {

    }
}
