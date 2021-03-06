package co.mide.instatranslate;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.mide.clipbroadcast.ClipMonitor;
import co.mide.instatranslate.data.DataStore;
import co.mide.instatranslate.data.LanguagePair;
import co.mide.instatranslate.views.DefinitionActivity;
import co.mide.instatranslate.views.MainActivity;
import co.mide.translator.Translator;

public class BroadcastListener extends BroadcastReceiver {
    private final long TIME_LIMIT = 4000;
    private final String TAG = "co.mide.instatranslate";

    @Override
    public void onReceive(Context context, Intent intent) {
        long startTime = System.currentTimeMillis();
        if(intent.getAction().equalsIgnoreCase(ClipMonitor.NEW_CLIP)) {
            String sourceText = intent.getStringExtra(ClipMonitor.COPIED_STRING);

            //if any word in the string is a valid url, ignore it
            String[] words = sourceText.split(" ");
            for(String word: words){
                if(Patterns.WEB_URL.matcher(word).matches())
                    return;
            }

            //if is phone number ignore it
            String clean = sourceText.replace(",", "")
                    .replace(".", "")
                    .replace("(","")
                    .replace(")","").trim();
            if(TextUtils.isDigitsOnly(clean))
                return;

            handleCopiedText(context, sourceText, startTime);

        }else if(intent.getAction().equalsIgnoreCase(ClipMonitor.NOT_RUNNING)) {
            sendNotification(context, context.getString(R.string.crash_restart), null);
        }
    }

    private void handleCopiedText(final Context context, final String sourceText, final long startTime){
        Date today = new Date();
        Date oct31;
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        try {
            oct31 = df.parse("10/31/2017");
            if(today.after(oct31)) {
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Translator t = new Translator(context.getString(R.string.google_translate_api_key));
        t.detectLanguage(sourceText, new Translator.onLanguageDetected(){
            @Override
            public void languageDetected(String detectedIso639) {
                for(LanguagePair langPair: DataStore.getLanguagePairs(context)) {
                    if (langPair.getSourceLanguage().getLanguage().equals(detectedIso639)) {
                        translateAndShow(context, langPair, sourceText, startTime);
                        break;
                    }
                }
            }

            public void error(String message){
                String errorMessage = String.format("Error: %s\n", message);
                Log.e(TAG, errorMessage);
            }
        });
    }

    private void translateAndShow(final Context context, final LanguagePair langPair,
                                  final String sourceText, final long startTime){
        if(!MainActivity.getDiscontinuedBeenViewed(context)) {
            String discontinued = context.getString(R.string.discontinued_short);
            sendNotification(context, discontinued, MainActivity.buildIntent(context));
        }
        Translator t = new Translator(context.getString(R.string.google_translate_api_key));
        final String destIso = langPair.getDestLanguage().getLanguage();
        t.translate(sourceText, destIso, new Translator.onTranslateComplete() {
            @Override
            public void translateComplete(String translated, String detectedSourceLang) {
                long endTime = System.currentTimeMillis();

                String sourceIso = langPair.getSourceLanguage().getLanguage();
                String destIso = langPair.getDestLanguage().getLanguage();
                Intent intent = DefinitionActivity.builtIntent(context, sourceText, sourceIso,
                        translated, destIso);

                if ((endTime - startTime) > TIME_LIMIT) {
                    String notificationText = context.getString(R.string.translation_found);
                    sendNotification(context, notificationText, intent);
                } else {
                    context.startActivity(intent);
                }
            }

            public void error(String message){
                Toast.makeText(context, R.string.translate_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendNotification(Context context, String message, Intent intent) {
        sendNotification(context, message, intent, 0);
    }
    private void sendNotification(Context context, String message, Intent intent, int notificationID) {
        if (intent == null){
            intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_translate_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }
}
