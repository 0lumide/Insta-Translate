package co.mide.instatranslate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import co.mide.clipbroadcast.ClipMonitor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startService(View v){
        new ClipMonitor(this).start();
    }
}
