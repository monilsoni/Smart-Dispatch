package com.example.smartdispatch_auth.UI.Admin;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartdispatch_auth.Models.SMSRequest;
import com.example.smartdispatch_auth.R;

import java.util.List;

public class SMSRequestAdapter extends RecyclerView.Adapter<SMSRequestAdapter.RequestViewHolder> {

    private Context context;
    private List<SMSRequest> requestList;

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
        return requestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i) {

        SMSRequest request = requestList.get(i);

        requestViewHolder.emergencyType.setText(request.getEmergencyType());
        requestViewHolder.severity.setText(request.getSeverity());
        requestViewHolder.latitude.setText(request.getLatitude());
        requestViewHolder.longitude.setText(request.getLongitude());
        requestViewHolder.requesterID.setText(request.getRequesterID());
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        public TextView emergencyType, severity, latitude, longitude, requesterID;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            emergencyType = itemView.findViewById(R.id.emergencyType);
            severity = itemView.findViewById(R.id.severity);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            requesterID = itemView.findViewById(R.id.requesterID);
        }
    }
}