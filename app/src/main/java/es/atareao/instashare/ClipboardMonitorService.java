package es.atareao.instashare;

/*
 * Copyright 2013 Tristan Waddington
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

import static es.atareao.instashare.JsonReader.readJsonFromUrl;

/**
 * Monitors the {@link ClipboardManager} for changes and logs the text to a file.
 */
public class ClipboardMonitorService extends Service {
    public static final int INSTASHARE_NOTIFICATION = 0;
    public static final String INSTASHARE_MESSAGE = "INSTASHARE_MESSAGE";
    public static final String INSTASHARE_MONITORING = "INSTASHARE_MONITORING";
    public static final String BROADCAST_MESSAGE_INSTASHARE = "BROADCAST_MESSAGE_INSTASHARE";
    public static final int DO_NONE = 0;
    public static final int START = 1;
    public static final int STOP = 2;
    public static final int START_STOP = 3;
    public static final int GET_INFO = 4;
    //
    private static final String TAG = "ClipboardManager";
    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);
        if((intent!=null)&&(intent.hasExtra(INSTASHARE_MESSAGE))){
            switch (intent.getIntExtra(INSTASHARE_MESSAGE,DO_NONE)) {
                case START:
                    if (mOnPrimaryClipChangedListener == null){
                        mOnPrimaryClipChangedListener =
                            new ClipboardManager.OnPrimaryClipChangedListener() {
                                @Override
                                public void onPrimaryClipChanged() {
                                    Log.d(TAG, "onPrimaryClipChanged");
                                    ClipData clip = mClipboardManager.getPrimaryClip();
                                    CharSequence mTextToWrite = clip.getItemAt(0).getText();
                                    new RetrieveUrlTask().execute(mTextToWrite.toString());
                                }
                            };
                        mClipboardManager.addPrimaryClipChangedListener(
                                mOnPrimaryClipChangedListener);
                    }
                    showNotification();
                    Intent startintent = new Intent(BROADCAST_MESSAGE_INSTASHARE);
                    startintent.putExtra(INSTASHARE_MONITORING, true);
                    sendBroadcast(startintent);
                    break;
                case GET_INFO:
                    boolean bmonitoring = (mOnPrimaryClipChangedListener != null);
                    Intent getinfo = new Intent(BROADCAST_MESSAGE_INSTASHARE);
                    getinfo.putExtra(INSTASHARE_MONITORING, bmonitoring);
                    sendBroadcast(getinfo);
                    break;
            }
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent getinfo = new Intent(BROADCAST_MESSAGE_INSTASHARE);
        getinfo.putExtra(INSTASHARE_MONITORING, false);
        sendBroadcast(getinfo);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(
                    mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            /** Remove existing files **/
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
            /** Saving new file **/
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
            Log.i(TAG, bmpUri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    private class RetrieveUrlTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            try {
                String theurl = urls[0];
                String imageurl = readJsonFromUrl("https://api.instagram.com/oembed?url="+theurl);
                Log.d(TAG, "Share image url: "+ imageurl);
                URL url = new URL(imageurl);
                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                startActivity(Intent.createChooser(i, "Compartiendo desde Instagram con InstaShare..."));
                Log.d(TAG, "shariiiiiiing");
                return imageurl;
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(String imgurl) {
            Log.d(TAG, "Share: "+imgurl);
        }
    }
    private void showNotification() {
        Context context = getApplicationContext();
        Intent aintent = new Intent(context, MainActivity.class);
        aintent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setTicker("InstaShare")
                .setOngoing(true)
                //.setContent(contentView)
                .setContentIntent(PendingIntent.getActivity(context, 0, aintent, PendingIntent.FLAG_UPDATE_CURRENT));
        if (mOnPrimaryClipChangedListener != null){
            mBuilder.setSmallIcon(R.drawable.instaplay);
        }else{
            mBuilder.setSmallIcon(R.drawable.instapause);
        }
        Notification notification = mBuilder.build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(INSTASHARE_NOTIFICATION, notification);
    }
}