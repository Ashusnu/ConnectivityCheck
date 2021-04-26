package model;


import enums.InternetConnectionType;

public class ConnectionInfo {

    InternetConnectionType internetConnectionType;
    ConnectionQuality connectionQuality;
    String speed;
    boolean connected;

    public ConnectionInfo(InternetConnectionType internetConnectionType, ConnectionQuality connectionQuality, String speed, boolean connected) {
        this.internetConnectionType = internetConnectionType;
        this.connectionQuality = connectionQuality;
        this.speed = speed;
        this.connected = connected;
    }

    public InternetConnectionType getInternetConnectionType() {
        return internetConnectionType;
    }

    public ConnectionQuality getConnectionQuality() {
        return connectionQuality;
    }

    public String getSpeed() {
        return speed;
    }

    public boolean isConnected() {
        return connected;
    }
}
