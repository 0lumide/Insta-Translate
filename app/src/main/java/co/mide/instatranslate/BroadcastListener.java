package co.mide.instatranslate;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import co.mide.clipbroadcast.ClipMonitor;

public class BroadcastListener extends BroadcastReceiver {
    public BroadcastListener() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(ClipMonitor.NEW_CLIP)) {
            sendNotification(context, "New Clip");
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.apps.translate");

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .authority("translate.google.com")
                    .path("/m/translate")
                    .appendQueryParameter("q", "c'est l'meunier Mathurin qui caresse les filles au tic-tac du moulin")
                    .appendQueryParameter("tl", "pl") // target language
                    .appendQueryParameter("sl", "fr") // source language
                    .build();
            //intent.setType("text/plain"); //not needed, but possible
            intent.setData(uri);

        }else if(intent.getAction().equalsIgnoreCase(ClipMonitor.NOT_RUNNING)) {
            sendNotification(context, "Oops I crashed, touch to restart");
        }
    }

    private void sendNotification(Context context, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Insta Translate")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
