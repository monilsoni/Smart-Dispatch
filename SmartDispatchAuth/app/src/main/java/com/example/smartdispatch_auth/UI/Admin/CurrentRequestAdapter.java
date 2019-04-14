package com.example.smartdispatch_auth.UI.Admin;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartdispatch_auth.R;

import java.util.List;

public class CurrentRequestAdapter extends RecyclerView.Adapter<CurrentRequestAdapter.CurrentRequestViewHolder> {

    private Context context;
    private List<CurrentRequest> crrequestList;

    public CurrentRequestAdapter(Context context, List<CurrentRequest> crrequestList) {
        this.context = context;
        this.crrequestList = crrequestList;
    }

    @NonNull
    @Override
    public CurrentRequestAdapter.CurrentRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.admin_request_card, null);
        CurrentRequestViewHolder requestViewHolder = new CurrentRequestViewHolder(view);
        return requestViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentRequestViewHolder requestViewHolder, int i) {

        CurrentRequest request = crrequestList.get(i);

        requestViewHolder.hospitalname.setText(request.getHospitalname());
        requestViewHolder.hospitaladdr.setText(request.getHospitalddr());
        requestViewHolder.drivername.setText(request.getDrivername());
        requestViewHolder.contactno.setText(request.getContactno());
        requestViewHolder.vehicleno.setText(request.getVehicleno());
    }

    @Override
    public int getItemCount() {
        return crrequestList.size();
    }

    class CurrentRequestViewHolder extends RecyclerView.ViewHolder {

        public TextView hospitalname, hospitaladdr, drivername, contactno, vehicleno;

        public CurrentRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            hospitalname = itemView.findViewById(R.id.hospitalname);
            hospitaladdr = itemView.findViewById(R.id.hospitaladdr);
            drivername = itemView.findViewById(R.id.drivername);
            contactno = itemView.findViewById(R.id.contactno);
            vehicleno = itemView.findViewById(R.id.vehicleno);
        }
    }
}
