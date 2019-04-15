package com.example.smartdispatch_auth.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private REQReceiveListener reqListener;

    public void setREQListener(REQReceiveListener reqListener) {
        this.reqListener = reqListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);

                    if(reqListener!=null)
                    {
                        reqListener.onREQReceived(message);
                    }
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server.
                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    // Handle the error ...
                    if(reqListener!=null)
                    {
                        reqListener.onREQTimeOut();
                    }
                    break;
                case CommonStatusCodes.API_NOT_CONNECTED:

                    if (reqListener != null) {
                        reqListener.onREQReceivedError("API NOT CONNECTED");
                    }

                    break;

                case CommonStatusCodes.NETWORK_ERROR:

                    if (reqListener != null) {
                        reqListener.onREQReceivedError("NETWORK ERROR");
                    }

                    break;

                case CommonStatusCodes.ERROR:

                    if (reqListener != null) {
                        reqListener.onREQReceivedError("SOME THING WENT WRONG");
                    }

                    break;
            }
        }

    }

    public interface REQReceiveListener {

        void onREQReceived(String otp);

        void onREQTimeOut();

        void onREQReceivedError(String error);
    }



}