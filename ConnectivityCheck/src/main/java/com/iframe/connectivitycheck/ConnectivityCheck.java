package com.iframe.connectivitycheck;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import enums.InternetConnectionType;
import model.ConnectionInfo;
import model.ConnectionQuality;

public class ConnectivityCheck {

    private static final String TAG = "Connectivity";

    static connectionStateListener connectionStateListener;


    static AsyncTask mMyTask;
    static URL url;

    private static volatile boolean started = false;


    public static void checkConnectionState(connectionStateListener listener) {
        if(connectionStateListener == null) {
            connectionStateListener = listener;
            execute();
        }

    }


    public interface connectionStateListener {
        void connectionState(ConnectionInfo connectionInfo);
    }

    private static void execute(){
        mMyTask = new DownloadTask().execute(stringToURL());
    }

    public static void startCheck() {
        started = true;
        execute();
    }
    public static void stopCheck() {
        started = false;
    }

    private static class DownloadTask extends AsyncTask<URL,Void, Bitmap> {
        // bandwidth in kbps
        private final int POOR_BANDWIDTH = 150;
        private final int AVERAGE_BANDWIDTH = 550;
        private final int GOOD_BANDWIDTH = 2000;

        private final int FREQUENCY = 10000;

        private long downloadStarted = 0;
        private long endTime = 0;

        private Handler handler;

        public DownloadTask() {
            this.handler = new Handler();
        }

        protected void onPreExecute(){
        }
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;
            try{
                downloadStarted = System.currentTimeMillis();
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                return BitmapFactory.decodeStream(bufferedInputStream);
            }catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
        // When all async task done
        protected void onPostExecute(Bitmap result){
            if(result!=null){
                endTime = System.currentTimeMillis();

                double timeTakenMills = Math.floor(endTime - downloadStarted);  // time taken in milliseconds
                double timeTakenSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                final int kilobytePerSec = (int) Math.round(1024 / timeTakenSecs);

                ConnectionInfo connectionInfo = null;

                if(kilobytePerSec <= POOR_BANDWIDTH){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.POOR,kilobytePerSec+" kbps",true);
                }

                if((kilobytePerSec > POOR_BANDWIDTH) && (kilobytePerSec <= AVERAGE_BANDWIDTH)){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.AVERAGE,kilobytePerSec+" kbps",true);
                }


                if(kilobytePerSec > AVERAGE_BANDWIDTH){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.GOOD,kilobytePerSec+" kbps",true);
                }

                double speed = result.getByteCount() / timeTakenMills;
                Log.d(TAG, "onPostExecute: " + kilobytePerSec);
                connectionStateListener.connectionState(connectionInfo);

                if (!started) {
                    return;
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConnectivityCheck.execute();
                    }
                },FREQUENCY);
            } else {

                double timeTakenMills = Math.floor(endTime - downloadStarted);  // time taken in milliseconds
                double timeTakenSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                final int kilobytePerSec = (int) Math.round(1024 / timeTakenSecs);

                ConnectionInfo connectionInfo = null;

                if(kilobytePerSec <= POOR_BANDWIDTH){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.POOR,kilobytePerSec+" kbps",true);
                }

                if((kilobytePerSec > POOR_BANDWIDTH) && (kilobytePerSec <= AVERAGE_BANDWIDTH)){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.AVERAGE,kilobytePerSec+" kbps",true);
                }


                if(kilobytePerSec > AVERAGE_BANDWIDTH){
                    connectionInfo = new ConnectionInfo(InternetConnectionType.WIFI, ConnectionQuality.GOOD,kilobytePerSec+" kbps",true);
                }

                connectionStateListener.connectionState(connectionInfo);

                if (!started) {
                    return;
                }


                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConnectivityCheck.execute();
                    }
                },FREQUENCY);
            }
        }
    }

    protected static URL stringToURL() {
        try {
            url = new URL("https://www.google.com/logos/doodles/2021/vera-gedroits-151st-birthday-6753651837108356.3-l.png");
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
