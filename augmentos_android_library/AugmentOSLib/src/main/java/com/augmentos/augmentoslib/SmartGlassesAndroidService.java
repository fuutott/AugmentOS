package com.augmentos.augmentoslib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.augmentos.augmentoslib.events.KillTpaEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;
import java.util.UUID;

//a service provided for third party apps to extend, that make it easier to create a service in Android that will continually run in the background
public abstract class SmartGlassesAndroidService extends LifecycleService {
    // Service Binder given to clients
    private final IBinder binder = new LocalBinder();
    public static final String TAG = "SmartGlassesAndroidService_AugmentOS";
    public static final String INTENT_ACTION = "AUGMENTOS_INTENT";
    public static final String TPA_ACTION = "tpaAction";
    public static final String ACTION_START_FOREGROUND_SERVICE = "AugmentOSLIB_ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "AugmentOSLIB_ACTION_STOP_FOREGROUND_SERVICE";
    private String NOTIFICATION_DESCRIPTION = "Running in foreground";
    private final int NOTIFICATION_ID = Math.abs(UUID.randomUUID().hashCode());//Math.abs(getPackageName().hashCode());
    private static final String CHANNEL_ID = "augmentos_default_channel";

    private static final String CHANNEL_NAME = "AugmentOS Background Service";
    public FocusStates focusState;

    public SmartGlassesAndroidService(){
        this.focusState = FocusStates.OUT_FOCUS;
    }

    //service stuff
    private Notification updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(NOTIFICATION_DESCRIPTION);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(CHANNEL_NAME)
                .setContentText(NOTIFICATION_DESCRIPTION)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setTicker("...")
                .setOngoing(true).build();
    }

    public class LocalBinder extends Binder {
        public SmartGlassesAndroidService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SmartGlassesAndroidService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
           
            //True when service is started from AugmentOS
            if(Objects.equals(action, INTENT_ACTION) && extras != null){
                action = (String) extras.get(TPA_ACTION);
            }

            switch (action) {
                case ACTION_START_FOREGROUND_SERVICE:
                    // start the service in the foreground
                    Log.d("TEST", "starting foreground");
                    startForeground(NOTIFICATION_ID, updateNotification());
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return Service.START_STICKY;
    }

    @Subscribe
    public void onKillTpaEvent(KillTpaEvent receivedEvent){
        //if(receivedEvent.uuid == this.appUUID) //TODO: Figure out implementation here...
        Log.d(TAG, "TPA KILLING SELF");
        if(true)
        {
            Log.d(TAG, "TPA KILLING SELF received");
            stopForeground(true);
            Log.d(TAG, "Foreground stopped");
            stopSelf();
            Log.d(TAG, "Self stopped, service should end.");
        }
    }

    protected String getUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = prefs.getString("user_id", "");

        if (userId.isEmpty()) {
            // Generate a random UUID string if no userId exists
            userId = UUID.randomUUID().toString();

            // Save the new userId to SharedPreferences
            prefs.edit()
                    .putString("user_id", userId)
                    .apply();
        }

        return userId;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onDestroy(){
        Log.d(TAG, "running onDestroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        Log.d(TAG, "ran onDestroy");
    }
}
