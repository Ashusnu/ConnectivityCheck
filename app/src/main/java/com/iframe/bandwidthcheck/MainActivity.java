package com.iframe.bandwidthcheck;

import androidx.appcompat.app.AppCompatActivity;
import model.ConnectionInfo;

import android.os.Bundle;

import com.iframe.connectivitycheck.ConnectivityCheck;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityCheck.checkConnectionState(connectionInfo -> {

        });

    }
}