package com.akigen.ecotrekk.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public interface Listener {
        void onConnectivityChanged(boolean online);
    }

    private final Listener listener;

    public NetworkChangeReceiver(@NonNull Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onConnectivityChanged(NetworkUtils.isOnline(context));
    }
}
