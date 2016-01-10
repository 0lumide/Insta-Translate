package co.mide.instatranslate;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import co.mide.clipbroadcast.ClipMonitor;
import co.mide.translator.Translator;

public class BroadcastListener extends BroadcastReceiver {
    public BroadcastListener() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(ClipMonitor.NEW_CLIP)) {
            sendNotification(context, "New Clip");

            Log.e("CLIP", "new clip");
            String sourceText = intent.getStringExtra(ClipMonitor.COPIED_STRING);
            isWantedLanguage(context, sourceText);

        }else if(intent.getAction().equalsIgnoreCase(ClipMonitor.NOT_RUNNING)) {
            sendNotification(context, "Oops I crashed, touch to restart");
        }
    }

    private void isWantedLanguage(final Context context, final String sourceText){
        Translator t = new Translator(context.getString(R.string.google_translate_api_key));
        t.detectLanguage(sourceText, new Translator.onLanguageDetected(){
            @Override
            public void languageDetected(String detectedIso639) {
                if ("en".equals(detectedIso639)){
                    System.out.println(detectedIso639);
                    //translate
                    Intent localIntent = new Intent(context, DefinitionActivity.class);
                    localIntent.putExtra(ClipMonitor.COPIED_STRING, sourceText);
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(localIntent);
                }
            }

            public void error(){
                //Do nothing
                System.out.println("\n\nError\n");
            }
        });
    }

    private void sendNotification(Context context, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Instant Translate")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
