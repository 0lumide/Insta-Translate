package co.mide.instatranslate;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import co.mide.clipbroadcast.ClipMonitor;
import co.mide.translator.Language;
import co.mide.translator.Translator;

public class BroadcastListener extends BroadcastReceiver {
    private long startTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        startTime = System.currentTimeMillis();
        if(intent.getAction().equalsIgnoreCase(ClipMonitor.NEW_CLIP)) {

            Log.e("CLIP", "new clip");
            String sourceText = intent.getStringExtra(ClipMonitor.COPIED_STRING);
            //TODO
            /*
                Filter websites/url
                phone numbers
             */
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
                for(RecyclerAdapter.LanguagePair langPair: RecyclerAdapter.getLanguagePairs(context)) {
                    if (langPair.getSourceLanguage().language.equals(detectedIso639)) {
                        System.out.println(detectedIso639);
                        //translate
                        Intent localIntent = new Intent(context, DefinitionActivity.class);
                        localIntent.putExtra(ClipMonitor.COPIED_STRING, sourceText);
                        localIntent.putExtra("DEST_LANG", langPair.getDestLanguage().language);
                        localIntent.putExtra("DEST_LANG_NAME", langPair.getDestLanguage().name);
                        localIntent.putExtra("SOURCE_LANG", langPair.getSourceLanguage().language);
                        localIntent.putExtra("SOURCE_LANG_NAME", langPair.getSourceLanguage().name);
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        long endTime = System.currentTimeMillis();
                        if((endTime - startTime) > 4000){
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, localIntent,
                                    PendingIntent.FLAG_ONE_SHOT);

                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Instant Translate")
                                    .setContentText(context.getString(R.string.translation_found))
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent);

                            NotificationManager notificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
                        }else {
                            context.startActivity(localIntent);
                        }
                        break;
                    }
                }
            }

            public void error(String message){
                //Do nothing
                System.out.printf("Error: %s\n", message);
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
