package co.mide.instatranslate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;

import java.util.UUID;

import co.mide.clipbroadcast.ClipMonitor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        ((EditText)findViewById(R.id.edit_text)).setText(deviceId);
    }

    public void startService(View v){
        new ClipMonitor(this).start();
    }

    public void launch(View v){
        Intent intent = new Intent(this, DefinitionActivity.class);
        intent.putExtra(ClipMonitor.COPIED_STRING, "Hola, ¿entiendes Inglés?");
        startActivity(intent);
    }
}
