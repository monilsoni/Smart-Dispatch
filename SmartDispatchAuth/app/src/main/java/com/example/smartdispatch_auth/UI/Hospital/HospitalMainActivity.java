package com.example.smartdispatch_auth.UI.Hospital;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartdispatch_auth.R;

public class HospitalMainActivity extends AppCompatActivity {

    private Button currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_main);

        currentRequest = findViewById(R.id.current_request);

        currentRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HospitalMainActivity.this, HospitalCurrentRequest.class);
                startActivity(intent);
            }
        });

    }

}



