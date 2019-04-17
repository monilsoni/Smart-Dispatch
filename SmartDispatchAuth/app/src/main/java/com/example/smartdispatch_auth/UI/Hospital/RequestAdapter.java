package com.example.smartdispatch_auth.UI.Hospital;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartdispatch_auth.Models.Request;
import com.example.smartdispatch_auth.Models.RequestDisp;
import com.example.smartdispatch_auth.R;
import com.example.smartdispatch_auth.UI.Hospital.HospitalMapActivity;
import com.example.smartdispatch_auth.Utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private static final String TAG = "RequestAdapter";
    private CardView cardview;
    private Context context;
    private List<RequestDisp> requestList;
    private List<Request> requests;
    public RequestAdapter(Context context, List<RequestDisp> requestList, List<Request> requests) {
        this.context = context;
        this.requestList = requestList;
        this.requests = requests;
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.request_card, null);
        //cardview = viewGroup.findViewById(R.id.card_view);
        final RequestViewHolder requestViewHolder = new RequestViewHolder(view);
        /*view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HospitalMapActivity.class);
                intent.putExtra("request", requests.get(i));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d(TAG, "onClick: " + requests.get(i).toString());
                context.startActivity(intent);
            }
        });*/
        return requestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i) {

        final RequestDisp request = requestList.get(i);

        requestViewHolder.usrname.setText(request.getUsrname());
        requestViewHolder.usrage.setText(request.getUsrage());
        requestViewHolder.usrsex.setText(request.getUsrsex());
        requestViewHolder.drivername.setText(request.getDrivername());
        requestViewHolder.contactno.setText(request.getContactno());
        requestViewHolder.vehicleno.setText(request.getVehicleno());

        final int k = i;

        requestViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, HospitalMapActivity.class);
                intent.putExtra("request", requests.get(i));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d(TAG, "onClick: " + requests.get(i).toString());
                context.startActivity(intent);
            }
        });

        requestViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean internet;

                ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                internet = cm.getActiveNetworkInfo() != null;

                if (internet) {

                    FirebaseFirestore.getInstance().collection("Requests")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Request request = document.toObject(Request.class);

                                            if (request.getRequester().getUser_id().equals(requests.get(k).getRequester().getUser_id())) {
                                                document.getReference().delete();
                                                FirebaseFirestore.getInstance().collection("Request History").add(request);

                                                String requester_id = request.getRequester().getUser_id();
                                                String vehicle_id = request.getVehicle().getUser_id();

                                                Log.d(TAG, "onComplete: " + requester_id.toString() + "\n" + vehicle_id.toString());

                                                new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifVehicleRequestEnd?id=" +
                                                        vehicle_id);
                                                new Utilities.GetUrlContentTask().execute("https://us-central1-smartdispatch-auth.cloudfunctions.net/sendNotifRequesterRequestEnd?id=" +
                                                        requester_id);


                                                Intent i = new Intent("remove");
                                                i.putExtra("index", k);
                                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                                            }

                                        }
                                    } else {
                                        Log.d("data", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        public TextView usrname, usrage, usrsex, drivername, contactno, vehicleno;
        public Button button;
        public CardView cardView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            this.usrname = itemView.findViewById(R.id.usrname);
            this.usrage = itemView.findViewById(R.id.usrage);
            this.usrsex = itemView.findViewById(R.id.usrsex);
            this.drivername = itemView.findViewById(R.id.drivername);
            this.contactno = itemView.findViewById(R.id.contactno);
            this.vehicleno = itemView.findViewById(R.id.vehicleno);
            this.cardView = itemView.findViewById(R.id.card_view);
            this.button = (Button) itemView.findViewById(R.id.endrequestButton);
        }
    }
}
