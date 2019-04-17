package com.example.smartdispatch_auth.UI.Admin;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.smartdispatch_auth.Models.Cluster;
import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.SMSRequest;
import com.example.smartdispatch_auth.Models.Vehicle;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.Requester.RequestForm;
import com.example.smartdispatch_auth.UI.Requester.RequesterMainActivity;
import com.example.smartdispatch_auth.UserClient;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class SMSRequestAdapter extends RecyclerView.Adapter<SMSRequestAdapter.RequestViewHolder> {

    private final static String TAG = "SMSRequestAdapter";

    private Context context;
    private List<SMSRequest> requestList;
    private static Location currloc = new Location("");
    private ProgressDialog progress;

    public SMSRequestAdapter(Context context, List<SMSRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public SMSRequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.sms_card, null);
        RequestViewHolder requestViewHolder = new RequestViewHolder(view);
        progress = new ProgressDialog(context);
        progress.setMessage("Sending Request");
        progress.setCancelable(false);
        return requestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i) {

        SMSRequest request = requestList.get(i);

        requestViewHolder.emergencyType.setText(request.getEmergencyType());
        requestViewHolder.severity.setText(request.getSeverity());
        requestViewHolder.latitude.setText(request.getLatitude());
        requestViewHolder.longitude.setText(request.getLongitude());

        requestViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress.show();
                FirebaseFirestore.getInstance().collection("Users").document(request.getRequesterID()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){

                                    Requester currRequester;
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        currRequester = document.toObject(Requester.class);

                                        currloc.setLatitude(currRequester.getGeoPoint().getLatitude());
                                        currloc.setLongitude(currRequester.getGeoPoint().getLongitude());


                                        FirebaseFirestore.getInstance().collection("Cluster Main").get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Cluster c = document.toObject(Cluster.class);
                                                            clusters.add(c);
                                                        }

                                                        Collections.sort(clusters, sortClusters());

                                                        ArrayList<Vehicle> Available_Vehicles = new ArrayList<>();

                                                        for (Cluster cluster : clusters) {
                                                            Available_Vehicles.clear();

                                                            for (Vehicle vehicle : cluster.getVehicles()) {
                                                                if (vehicle.getEngage() == 0)
                                                                    Available_Vehicles.add(vehicle);
                                                            }
                                                            if (Available_Vehicles.size() > 0)
                                                                break;

                                                        }


                                                        Vehicle nearestVehicle;
                                                        int vehicles_count = Available_Vehicles.size();
                                                        if (vehicles_count == 0)
                                                            Log.d("Vehicle-count", "no free vehicles");
                                                        else {

                                                            Collections.sort(Available_Vehicles, sortVehicles());

                                                            nearestVehicle = Available_Vehicles.get(0);

                                                            final String[] token = new String[1];
                                                            FirebaseFirestore.getInstance().collection("Vehicles")
                                                                    .document(nearestVehicle.getUser_id()).get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful() && task.getResult().exists()) {
                                                                                token[0] = task.getResult().toObject(Vehicle.class).getToken();
                                                                                nearestVehicle.setToken(token[0]);
                                                                            }
                                                                        }
                                                                    });

                                                            FirebaseFirestore.getInstance().collection("Hospitals").get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        float temp = Float.MAX_VALUE;

                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            Hospital nearestHospital = new Hospital();

                                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                Hospital h = document.toObject(Hospital.class);
                                                                                Log.d(TAG, h.toString());
                                                                                Location l1 = new Location("");
                                                                                l1.setLongitude(h.getGeoPoint().getLatitude());
                                                                                l1.setLongitude(h.getGeoPoint().getLongitude());

                                                                                if (temp >= currloc.distanceTo(l1)) {
                                                                                    temp = currloc.distanceTo(l1);
                                                                                    nearestHospital = h;
                                                                                }
                                                                            }

                                                                            Request smsRequest = new Request(
                                                                                    currRequester,
                                                                                    nearestVehicle,
                                                                                    nearestHospital,
                                                                                    request.getEmergencyType(),
                                                                                    Integer.parseInt(request.getSeverity()),
                                                                                    0,
                                                                                    currRequester.getUser_id()
                                                                            );

                                                                            FirebaseFirestore.getInstance().collection("OfflineRequests")
                                                                                    .get()
                                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                                            if (task.isSuccessful()) {
                                                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                                    SMSRequest smsrequest = document.toObject(SMSRequest.class);

                                                                                                    if (smsrequest.getRequesterID().equals(requestList.get(i).getRequesterID())) {
                                                                                                        document.getReference().delete();
                                                                                                    }
                                                                                                }

                                                                                                FirebaseFirestore.getInstance().collection("Requests").document(currRequester.getUser_id()).set(smsRequest)
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                Log.d(TAG, "onSuccess: Request Stored.");
                                                                                                                progress.dismiss();
                                                                                                            }
                                                                                                        });

                                                                                                new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifVehicle?id=" +
                                                                                                        smsRequest.getVehicle().getUser_id() +
                                                                                                        "&name=" + smsRequest.getRequester().getName());

                                                                                                new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifHospital?id=" +
                                                                                                        smsRequest.getHospital().getUser_id() +
                                                                                                        "&name=" + smsRequest.getRequester().getName());

                                                                                                Intent intent = new Intent("remove");
                                                                                                intent.putExtra("index", i);
                                                                                                intent.putExtra("request",smsRequest);
                                                                                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                                                                            }
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progress.dismiss();
                                                                                    Log.d(TAG, "onFailure: Request failed to store.");
                                                                                }

                                                                            });

                                                                        }
                                                                    });
                                                        }

                                                    }
                                                });




                                    }
                                    else
                                        Log.d(TAG,"No requester found with requester id " + request.getRequesterID() );
                                }
                            }
                        });

            }
        });
    }

    public static Comparator<Cluster> sortClusters() {
        Comparator comp = new Comparator<Cluster>() {
            @Override
            public int compare(Cluster c1, Cluster c2) {
                Location l1 = new Location("");
                Location l2 = new Location("");

                l1.setLatitude(c1.getGeoPoint().getLatitude());
                l1.setLongitude(c1.getGeoPoint().getLongitude());

                l2.setLatitude(c2.getGeoPoint().getLatitude());
                l2.setLongitude(c2.getGeoPoint().getLongitude());

                float dist1 = currloc.distanceTo(l1);
                float dist2 = currloc.distanceTo(l2);

                if (dist1 < dist2)
                    return 1;
                else
                    return 0;
            }
        };
        return comp;
    }

    public static Comparator<Vehicle> sortVehicles() {
        Comparator comp = new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle c1, Vehicle c2) {
                Location l1 = new Location("");
                Location l2 = new Location("");

                l1.setLatitude(c1.getGeoPoint().getLatitude());
                l1.setLongitude(c1.getGeoPoint().getLongitude());

                l2.setLatitude(c2.getGeoPoint().getLatitude());
                l2.setLongitude(c2.getGeoPoint().getLongitude());

                float dist1 = currloc.distanceTo(l1);
                float dist2 = currloc.distanceTo(l2);

                if (dist1 < dist2)
                    return 1;
                else
                    return 0;
            }
        };
        return comp;
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        public TextView emergencyType, severity, latitude, longitude;
        public Button button;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            emergencyType = itemView.findViewById(R.id.emergencyType);
            severity = itemView.findViewById(R.id.severity);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);

            this.button = (Button) itemView.findViewById(R.id.assign_vehicle);
        }
    }
}