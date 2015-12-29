package co.mide.clipbroadcast;

import android.app.Service;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ClipBroadcast extends Service {

    @Override
    public void onCreate(){
        super.onCreate();
        final ClipboardManager clipboard = ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(ClipBroadcast.this).toString();
                if(!clip.isEmpty() && !clipboard.getPrimaryClipDescription().hasMimeType(ClipMonitor.MIME_IGNORE))
                    sendNewClipBroadcast(clip);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(ClipMonitor.NOT_RUNNING);
        sendBroadcast(intent);
    }

    public void sendNewClipBroadcast(String text){
        Intent intent = new Intent(ClipMonitor.NEW_CLIP);
        intent.putExtra(ClipMonitor.COPIED_STRING, text);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
