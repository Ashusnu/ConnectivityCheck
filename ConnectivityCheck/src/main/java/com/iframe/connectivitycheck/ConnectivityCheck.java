package com.iframe.connectivitycheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.ref.WeakReference;

import enums.InternetConnectionType;
import model.ConnectionInfo;
import model.ConnectionQuality;

public class ConnectivityCheck {

    private static final String TAG = "Connectivity";
    private static final int FREQUENCY = 10000;
    private static final int POOR_BANDWIDTH = 150;
    private static final int AVERAGE_BANDWIDTH = 550;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    static WeakReference<connectionStateListener> connectionStateListenerRef;


    static AsyncTask mMyTask;
    static URL url;

    private static Context context;

    private static volatile boolean started = false;
    private static final Runnable RECHECK_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            if (started) {
                ConnectivityCheck.execute();
            }
        }
    };


    public static void checkConnectionState(connectionStateListener listener, Context con) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        connectionStateListenerRef = new WeakReference<>(listener);
        if (con != null) {
            // Use app context so this static class does not leak Activities.
            context = con.getApplicationContext();
        } else {
            notifyError(new CustomException("Context cannot be null.", null));
        }
    }


    public interface connectionStateListener {
        void connectionState(ConnectionInfo connectionInfo);
        void onError(Exception e);
    }

    private static void execute(){
        URL checkUrl = stringToURL();
        if (checkUrl == null) {
            notifyError(new CustomException("Could not create connectivity test URL.", null));
            scheduleNextIfStarted();
            return;
        }
        if (context == null) {
            notifyError(new CustomException("Context is not initialized. Call checkConnectionState first.", null));
            scheduleNextIfStarted();
            return;
        }
        mMyTask = new DownloadTask(context).execute(checkUrl);
    }

    public static void startCheck() {
        if (getListener() == null) {
            throw new IllegalStateException("Call checkConnectionState(listener, context) before startCheck().");
        }
        if (context == null) {
            throw new IllegalStateException("Context is not initialized. Call checkConnectionState(listener, context) first.");
        }
        started = true;
        MAIN_HANDLER.removeCallbacks(RECHECK_RUNNABLE);
        execute();
    }
    public static void stopCheck() {
        started = false;
        MAIN_HANDLER.removeCallbacks(RECHECK_RUNNABLE);
        if (mMyTask != null) {
            mMyTask.cancel(true);
            mMyTask = null;
        }
    }

    private static class DownloadTask extends AsyncTask<URL,Void, DownloadResult> {
        boolean isMetered = false;

        public DownloadTask(Context context) {
            if (context!= null) {
                try {
                    ConnectivityManager cm =
                            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        isMetered = cm.isActiveNetworkMetered();
                    }
                } catch (Exception e) {
                    notifyError(e);
                }
            } else {
                notifyError(new CustomException("Context cannot be null.",null));
            }
        }

        protected void onPreExecute(){
        }
        protected DownloadResult doInBackground(URL...urls){
            if (urls == null || urls.length == 0 || urls[0] == null) {
                return DownloadResult.error(new CustomException("URL cannot be null.", null));
            }
            URL url = urls[0];
            HttpURLConnection connection = null;
            long bytesRead = 0;
            long downloadStarted = System.currentTimeMillis();
            try{
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream();
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                    byte[] buffer = new byte[8 * 1024];
                    int read;
                    while ((read = bufferedInputStream.read(buffer)) != -1) {
                        bytesRead += read;
                    }
                }
                long endTime = System.currentTimeMillis();
                return DownloadResult.success(bytesRead, endTime - downloadStarted);
            }catch(IOException e){
                long endTime = System.currentTimeMillis();
                return DownloadResult.error(e, bytesRead, endTime - downloadStarted);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        // When all async task done
        protected void onPostExecute(DownloadResult result){

            InternetConnectionType internetConnectionType = InternetConnectionType.UNKNOWN;
            if (isMetered) {
                internetConnectionType = InternetConnectionType.SIM;
            }
            if (!isMetered) {
                internetConnectionType = InternetConnectionType.WIFI;
            }


            if (result == null) {
                notifyError(new CustomException("Download result was null.", null));
                connectionState(internetConnectionType, ConnectionQuality.POOR, 0, false);
                scheduleNextIfStarted();
                return;
            }

            long safeElapsedMs = Math.max(1L, result.timeTakenMillis);
            double bitsPerSec = (result.bytesDownloaded * 8.0d) / (safeElapsedMs / 1000.0d);
            final int kbps = (int) Math.round(bitsPerSec / 1000.0d);

            ConnectionQuality connectionQuality = toConnectionQuality(kbps);
            boolean connected = result.error == null;
            connectionState(internetConnectionType, connectionQuality, kbps, connected);

            if (result.error != null) {
                notifyError(result.error);
            }

            scheduleNextIfStarted();
        }
    }

    protected static URL stringToURL() {
        try {
            url = new URL("https://www.google.com/logos/doodles/2021/vera-gedroits-151st-birthday-6753651837108356.3-l.png");
            return url;
        } catch (Exception e) {
            notifyError(e);
            return null;
        }
    }

    private static void scheduleNextIfStarted() {
        if (!started) {
            return;
        }
        MAIN_HANDLER.removeCallbacks(RECHECK_RUNNABLE);
        MAIN_HANDLER.postDelayed(RECHECK_RUNNABLE, FREQUENCY);
    }

    private static ConnectionQuality toConnectionQuality(int kbps) {
        if (kbps <= POOR_BANDWIDTH) {
            return ConnectionQuality.POOR;
        }
        if (kbps <= AVERAGE_BANDWIDTH) {
            return ConnectionQuality.AVERAGE;
        }
        return ConnectionQuality.GOOD;
    }

    private static void connectionState(InternetConnectionType type, ConnectionQuality quality, int kbps, boolean connected) {
        connectionStateListener listener = getListener();
        if (listener == null) {
            return;
        }
        ConnectionInfo connectionInfo = new ConnectionInfo(type, quality, kbps + " kbps", connected);
        listener.connectionState(connectionInfo);
    }

    private static void notifyError(final Exception e) {
        final connectionStateListener listener = getListener();
        if (listener == null || e == null) {
            return;
        }
        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(e);
            }
        });
    }

    private static connectionStateListener getListener() {
        if (connectionStateListenerRef == null) {
            return null;
        }
        return connectionStateListenerRef.get();
    }

    private static class DownloadResult {
        final long bytesDownloaded;
        final long timeTakenMillis;
        final Exception error;

        private DownloadResult(long bytesDownloaded, long timeTakenMillis, Exception error) {
            this.bytesDownloaded = bytesDownloaded;
            this.timeTakenMillis = timeTakenMillis;
            this.error = error;
        }

        static DownloadResult success(long bytesDownloaded, long timeTakenMillis) {
            return new DownloadResult(bytesDownloaded, timeTakenMillis, null);
        }

        static DownloadResult error(Exception error) {
            return new DownloadResult(0, 0, error);
        }

        static DownloadResult error(Exception error, long bytesDownloaded, long timeTakenMillis) {
            return new DownloadResult(bytesDownloaded, timeTakenMillis, error);
        }
    }


}
