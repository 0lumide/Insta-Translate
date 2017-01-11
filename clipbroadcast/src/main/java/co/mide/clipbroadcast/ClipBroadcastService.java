package co.mide.clipbroadcast;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ClipBroadcastService extends Service {
    protected MyBinder binder;
    ClipMonitorThread clipMonitorThread;

    @Override
    public void onCreate(){
        super.onCreate();
        binder = new MyBinder();
        final ClipboardManager clipboard = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(ClipBroadcastService.this).toString();
                if(!clip.isEmpty() && !clipboard.getPrimaryClipDescription().hasMimeType(ClipMonitor.MIME_IGNORE)) {
                    sendNewClipBroadcast(clip);
                }
            }
        });
//        clipMonitorThread = new ClipMonitorThread(getApplicationContext(),
//                (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE));
//        clipMonitorThread.start();
    }

    @Override
    public void onDestroy(){
        clipMonitorThread.interrupt();
        Intent intent = new Intent(ClipMonitor.NOT_RUNNING);
        sendBroadcast(intent);
        Log.e("service", "destroy");
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
