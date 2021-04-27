package com.iframe.bandwidthcheck;

import androidx.appcompat.app.AppCompatActivity;
import model.ConnectionInfo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.iframe.connectivitycheck.ConnectivityCheck;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView info   =   findViewById(R.id.info_txt);

        ConnectivityCheck.checkConnectionState(new ConnectivityCheck.connectionStateListener() {
            @Override
            public void connectionState(ConnectionInfo connectionInfo) {
                Log.d(TAG, "connectionState: " + connectionInfo.getSpeed() + " " + connectionInfo.getConnectionQuality());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("Speed " + connectionInfo.getSpeed() + "\n " + connectionInfo.getConnectionQuality() +"\n" + connectionInfo.getInternetConnectionType());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // handle exceptions
                Log.e(TAG, "onError: " +  e.toString() );
            }
        }, this);

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