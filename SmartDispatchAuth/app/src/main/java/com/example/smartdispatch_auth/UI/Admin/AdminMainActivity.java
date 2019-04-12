package com.example.smartdispatch_auth.UI.Admin;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Cluster;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.Models.ClusterFetch;
import com.example.smartdispatch_auth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "AdminMainActivity";

    private static ArrayList<Vehicle> vehicles;
    private static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        findViewById(R.id.allocateVehicle).setOnClickListener(this);
        findViewById(R.id.register_hospital).setOnClickListener(this);
        findViewById(R.id.register_vehicle).setOnClickListener(this);

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Preparing the interface");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.allocateVehicle: {
                progress.show();
                new GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/retrieve");
            }

            case R.id.register_hospital: {

            }

            case R.id.register_vehicle: {

            }
        }

    }

    private static class GetUrlContentTask extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... url2) {
            String content = null;

            try {
                URL url = new URL(url2[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                content = "";
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }

                vehicles = new ArrayList<>();

                FirebaseFirestore.getInstance().collection("Cluster Main").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                                FirebaseFirestore.getInstance().collection("Vehicles").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        for (QueryDocumentSnapshot document : task.getResult())
                                            vehicles.add(document.toObject(Vehicle.class));

                                        Log.d(TAG, vehicles.toString());

                                        FirebaseFirestore.getInstance().collection("clusters").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                int count = 0, id = 0;

                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    ClusterFetch c = document.toObject(ClusterFetch.class);
                                                    GeoPoint p = new GeoPoint(Double.parseDouble(c.getX()), Double.parseDouble(c.getY()));
                                                    ArrayList<Vehicle> v = new ArrayList<>();

                                                    for (int i = 0; i < c.getNoOfVehicles(); i++, count++) {
                                                        v.add(vehicles.get(count));
                                                    }
                                                    Cluster cluster = new Cluster(Integer.toString(id), p, v);
                                                    FirebaseFirestore.getInstance().collection("Cluster Main").add(cluster);
                                                    id++;
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });


                progress.dismiss();
            } catch (IOException e) {

                Log.d(TAG, "doInBackground: " + e.toString());
            }

            return content;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

}