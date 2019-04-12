package com.example.smartdispatch_auth.UI.Admin;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class AdminMainActivity extends AppCompatActivity {

    private static ArrayList<Vehicle> vehicles;
    private static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        Button button = findViewById(R.id.button);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
                new GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/retrieve");

            }
        });
    }
    private static class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... url2) {

            try {
                URL url = new URL(url2[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String content = "", line;
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }

                vehicles = new ArrayList<>();

                FirebaseFirestore.getInstance().collection("Cluster Main").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    document.getReference().delete();
                                }
                                FirebaseFirestore.getInstance().collection("Vehicles")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Vehicle vehicle = document.toObject(Vehicle.class);
                                                    vehicles.add(vehicle);
                                                }
                                                Log.i("vehicles", vehicles.toString());
                                                FirebaseFirestore.getInstance().collection("clusters")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                int count=0;
                                                                int id=0;
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    ClusterFetch c = document.toObject(ClusterFetch.class);
                                                                    GeoPoint p = new GeoPoint(Double.parseDouble(c.getX()), Double.parseDouble(c.getY()));
                                                                    ArrayList<Vehicle> v = new ArrayList<>();
                                                                    for (int i=0;i<c.getNoOfVehicles();i++){
                                                                        v.add(vehicles.get(count));
                                                                        count++;
                                                                    }
                                                                    Cluster clus = new Cluster(Integer.toString(id), p, v);

                                                                    FirebaseFirestore.getInstance().collection("Cluster Main").add(clus);
                                                                    id++;
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });


                progress.dismiss();
                return content;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

    }

}