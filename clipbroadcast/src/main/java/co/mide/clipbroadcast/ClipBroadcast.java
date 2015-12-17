package co.mide.clipbroadcast;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ClipBroadcast extends Service {
    protected MyBinder binder;

    @Override
    public void onCreate(){
        super.onCreate();
        binder = new MyBinder();
        new ClipMonitorThread(getApplicationContext(),
                (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(ClipMonitor.NOT_RUNNING);
        sendBroadcast(intent);
    }

    public void sendNewClipBroadcast(){
        Intent intent = new Intent(ClipMonitor.NEW_CLIP);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder{
        public ClipBroadcast getService(){
            return ClipBroadcast.this;
        }
    }
}
