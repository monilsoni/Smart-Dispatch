package com.example.smartdispatch_auth.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.smartdispatch_auth.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EntryPoint extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_point);

        findViewById(R.id.user_login_button).setOnClickListener(this);
        findViewById(R.id.vehicle_login_button).setOnClickListener(this);
        findViewById(R.id.hospital_login_button).setOnClickListener(this);
        findViewById(R.id.admin_login_button).setOnClickListener(this);

        // Check if the user is already authenticated in the system & redirect accordingly
        /*
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Intent intent = new Intent(EntryPoint.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            String email = user.getEmail();
            if(email.contains("@smartdispatch.gov.in")){
               if(email.contains("v_"))
                   intent.putExtra("authenticator", "vehicle");
               else
                   intent.putExtra("authenticator", "hospital");
            }else{
                intent.putExtra("authenticator", "requester");
            }
            startActivity(intent);
            finish();
        }*/
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(EntryPoint.this, LoginActivity.class);

        switch (v.getId()) {
            case R.id.user_login_button: {
                intent.putExtra("authenticator", "requester");
                startActivity(intent);
                break;
            }
            case R.id.vehicle_login_button: {
                intent.putExtra("authenticator", "vehicle");
                startActivity(intent);
                break;
            }
            case R.id.hospital_login_button: {
                intent.putExtra("authenticator", "hospital");
                startActivity(intent);
                break;
            }
            case R.id.admin_login_button: {
                intent.putExtra("authenticator", "admin");
                startActivity(intent);
                break;
            }
        }

    }
}
