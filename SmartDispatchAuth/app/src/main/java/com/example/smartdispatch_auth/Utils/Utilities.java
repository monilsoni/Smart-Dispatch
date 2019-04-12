package com.example.smartdispatch_auth.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ProgressBar;

public class Utilities {

    public static void showDialog(ProgressBar mProgressBar){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    public static void hideDialog(ProgressBar mProgressBar){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public static boolean checkInternetConnectivity(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());

    }
}
