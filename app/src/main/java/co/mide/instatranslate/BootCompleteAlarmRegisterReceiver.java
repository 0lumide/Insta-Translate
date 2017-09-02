package co.mide.instatranslate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteAlarmRegisterReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            // Register Alarm
            AlarmManagerController.registerServiceLauncherAlarm(context);
        }
    }
}
