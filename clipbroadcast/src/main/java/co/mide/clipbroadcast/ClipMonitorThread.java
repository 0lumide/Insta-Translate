package co.mide.clipbroadcast;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import co.mide.clipbroadcast.ClipBroadcast.MyBinder;

public class ClipMonitorThread extends Thread{
    protected boolean stopped;
    protected Context context;
    protected ClipBroadcast clipBroadcast;
    protected boolean mBound;
    protected ClipboardManager clipboard;
    private String lastClip = "";

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyBinder binder = (MyBinder) service;
            clipBroadcast = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public ClipMonitorThread(Context c, ClipboardManager clipboard){
        initialize(c, clipboard);
    }

    public void initialize(Context context, ClipboardManager clipboard){
        this.context = context.getApplicationContext();
        this.clipboard = clipboard;
        Intent intent = new Intent(context, ClipBroadcast.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void run(){
        if(context == null)
            throw new NullPointerException("Context not set");
        Intent intent = new Intent(context, ClipBroadcast.class);
        context.startService(intent);

        while(!stopped){
            checkClip();
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                //Do nothing
            }
        }
    }

    private void checkClip(){
        String clip = "";
        ClipDescription clipDescription = clipboard.getPrimaryClipDescription();

        if(clipDescription == null) {
            lastClip = null;
            return;
        }

        if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            clip = clipboard.getPrimaryClip().getItemAt(0).toString();
        }else if (clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
            clip = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        }

        if((lastClip != null) && lastClip.equals(""))
            lastClip = clip;

        if((lastClip == null) || !clip.equals(lastClip)){
            lastClip = clip;
            if(mBound)
                clipBroadcast.sendNewClipBroadcast();
        }
    }
}
