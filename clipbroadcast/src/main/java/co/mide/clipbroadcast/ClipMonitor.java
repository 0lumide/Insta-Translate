package co.mide.clipbroadcast;

import android.content.Context;
import android.content.Intent;

public class ClipMonitor {
    public final static String NEW_CLIP = "co.mide.clipbroadcast.NEW_CLIP";
    public final static String NOT_RUNNING = "co.mide.clipbroadcast.NOT_RUNNING";
    public static final String COPIED_STRING = "copied_string";
    public static final String MIME_IGNORE = "text/ignore";
    private Context context;

    public ClipMonitor(Context context){
        this.context = context;
    }

    public void start(){
        Intent intent = new Intent(context, ClipBroadcast.class);
        context.startService(intent);
    }
}
