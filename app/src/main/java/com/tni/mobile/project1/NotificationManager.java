package com.tni.mobile.project1;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.text.DecimalFormat;

public class NotificationManager extends NotificationCompat {
    private Context context;
    private String MemoryOutput = "Max Memory Usage :", NetworkOutput = "Network Traffic :", CPUOutput = "Total CPU Usage :";
    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

    private DecimalFormat mCPUFormat = new DecimalFormat("##0.0");
    private DecimalFormat mFormat = new DecimalFormat("##,###,##0");

    public NotificationManager(String appName,int maxMemUsed, Context context) {
        this.context = context;
        MemoryOutput = "Max Memory Usage" + appName + " : " + mFormat.format(maxMemUsed) + "kB";
    }

    public NotificationManager(String header, long currentRX, long currentTX, Context context) {
        this.context = context;
        NetworkOutput = header + " :TX = " + mFormat.format(currentTX) + " bytes, Rx = " + mFormat.format(currentRX) + " bytes";
    }

    public NotificationManager(float totalCPUUsage, Context context) {
        this.context = context;
        CPUOutput = "Total CPU Usage : " + mCPUFormat.format(totalCPUUsage) + " %";
    }

    public void showNotification() {
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
//        CharSequence message = MemoryOutput + "\n" + NetworkOutput + "\n" + CPUOutput;
        Intent targetIntent = new Intent(context, NotificationManager.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, targetIntent, 0);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle("Notification Message")
                .setContentText("Result")
                .setSmallIcon(R.drawable.icon_monitoring)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_monitoring, null))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        inboxStyle.setBigContentTitle("Monitoring Result");
        inboxStyle.addLine(MemoryOutput);
        inboxStyle.addLine(NetworkOutput);
        inboxStyle.addLine(CPUOutput);

        n.setStyle(inboxStyle);

        notificationManager.notify(3, n.build());
//        clear();
    }

    private void clear(){
        MemoryOutput = "";
        NetworkOutput = "";
        CPUOutput = "";
    }
}
