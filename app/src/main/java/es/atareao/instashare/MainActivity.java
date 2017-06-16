package es.atareao.instashare;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private TextView mTv;
    private BroadcastReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViewById(R.id.)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTv = (TextView) findViewById(R.id.instructionsView);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        if(isMyServiceRunning(ClipboardMonitorService.class)){
            mFab.setImageResource(android.R.drawable.ic_media_pause);
        }else{
            mFab.setImageResource(android.R.drawable.ic_media_play);
        }
        mMessageReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
                    case ClipboardMonitorService.BROADCAST_MESSAGE_INSTASHARE:
                        if (intent.getBooleanExtra(ClipboardMonitorService.INSTASHARE_MONITORING, false)){
                            mFab.setImageResource(android.R.drawable.ic_media_pause);
                            mTv.setText(R.string.instructions_pause);
                        }else{
                            mFab.setImageResource(android.R.drawable.ic_media_play);
                            mTv.setText(R.string.instructions_play);
                        }
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ClipboardMonitorService.BROADCAST_MESSAGE_INSTASHARE);
        registerReceiver(mMessageReceiver, intentFilter);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int msgId = -1;
                if (isMyServiceRunning(ClipboardMonitorService.class)){
                    stopCMS();
                    msgId = R.string.stop_monitor;
                }else {
                    startCMS();
                    msgId = R.string.start_monitor;
                }
                Snackbar.make(view, getResources().getString(msgId), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        /*
        if(_selectedItem==2){
            savedInstanceState.putString("MoonMonthFragment.Calendar", Utils.fromCalendarToString(_moonMonthFragment.get_calendar()));
        }
        */
    }
    private void startCMS(){
        Intent intent = new Intent(this, ClipboardMonitorService.class);
        intent.putExtra(ClipboardMonitorService.INSTASHARE_MESSAGE, ClipboardMonitorService.START);
        startService(intent);
    }
    private void stopCMS(){
        Intent intent = new Intent(this, ClipboardMonitorService.class);
        stopService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id ==R.id.action_about){
            startActivity(new Intent(this, About.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mMessageReceiver);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
