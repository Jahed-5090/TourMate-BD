package com.tourmate.bd.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Broadcast Receiver for TourMate BD (Topic 10: Broadcast Receivers).
 * Dynamically detects network connectivity changes (online/offline).
 * Notifies registered Activities to show offline alert banners or update data.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface NetworkChangeListener {
        void onNetworkChanged(boolean isConnected);
    }

    private NetworkChangeListener listener;

    public NetworkChangeReceiver() {
    }

    public NetworkChangeReceiver(NetworkChangeListener listener) {
        this.listener = listener;
    }

    public void setListener(NetworkChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = isNetworkAvailable(context);
        if (listener != null) {
            listener.onNetworkChanged(isConnected);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
}
