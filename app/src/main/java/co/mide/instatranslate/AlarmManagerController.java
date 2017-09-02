package co.mide.instatranslate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import co.mide.clipbroadcast.ClipBroadcastService;

public class AlarmManagerController {

    public static void registerServiceLauncherAlarm (Context context) {
        final long THIRTY_MINUTES = 30 * 60 * 1000L;
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ClipBroadcastService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 879, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + THIRTY_MINUTES,
                THIRTY_MINUTES, pendingIntent);
    }
}
