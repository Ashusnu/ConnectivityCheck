package com.iframe.bandwidthcheck;

import androidx.appcompat.app.AppCompatActivity;
import model.ConnectionInfo;

import android.os.Bundle;
import android.util.Log;

import com.iframe.connectivitycheck.ConnectivityCheck;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityCheck.checkConnectionState(connectionInfo ->
                Log.d(TAG, "connectionState: " + connectionInfo.getSpeed() + " " + connectionInfo.getConnectionQuality()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityCheck.startCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectivityCheck.stopCheck();
    }

}