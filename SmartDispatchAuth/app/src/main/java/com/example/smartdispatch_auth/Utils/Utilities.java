package com.example.smartdispatch_auth.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.support.constraint.Constraints.TAG;

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

    public static class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... url2) {
            String content = "";
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
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }

            } catch (IOException e) {
                content = "";
                Log.d(TAG, "doInBackground: "+ e.toString());
            }

            return content;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

    }
}
