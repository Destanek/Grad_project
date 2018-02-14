package com.tni.mobile.project1.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tni.mobile.project1.Network.NetworkService;

public class NetworkReceiverCall extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Service Stops", "Let's start again");
        context.startService(new Intent(context, NetworkService.class));
    }
}
