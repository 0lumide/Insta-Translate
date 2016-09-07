package co.mide.clipbroadcast;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ClipBroadcastService extends Service {
    protected MyBinder binder;
    ClipMonitorThread clipMonitorThread;

    @Override
    public void onCreate(){
        super.onCreate();
        binder = new MyBinder();
        clipMonitorThread = new ClipMonitorThread(getApplicationContext(),
                (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE));
        clipMonitorThread.start();
    }

    @Override
    public void onDestroy(){
        clipMonitorThread.interrupt();
        Intent intent = new Intent(ClipMonitor.NOT_RUNNING);
        sendBroadcast(intent);
        super.onDestroy();
    }

    public void sendNewClipBroadcast(String text){
        Intent intent = new Intent(ClipMonitor.NEW_CLIP);
        intent.putExtra(ClipMonitor.COPIED_STRING, text);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder{
        public ClipBroadcastService getService(){
            return ClipBroadcastService.this;
        }
    }
}
