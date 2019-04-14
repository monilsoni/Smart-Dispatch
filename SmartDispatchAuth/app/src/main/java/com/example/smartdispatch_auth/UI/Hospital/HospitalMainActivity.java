package com.example.smartdispatch_auth.UI.Hospital;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.example.smartdispatch_auth.R;

import static com.example.smartdispatch_auth.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

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


    /*  GPS Service  */

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, HospitalLocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                HospitalMainActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("ccom.example.smartdispatch_auth.Services.HospitalLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void getHospitalDetails() {

        if (mHospital == null) {
            mHospital = new Hospital();
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(getString(R.string.collection_hospital))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: successfully set the requester client.");
                        mHospital = task.getResult().toObject(Hospital.class);
                        Log.d(TAG, "Hospital inside getHospitalDetails: " + mHospital.toString());
                        ((UserClient) (getApplicationContext())).setHospital(mHospital);

