package com.augmentos.augmentos_core;

import static com.augmentos.augmentos_core.Constants.SEND_NOTIFICATIONS_ENDPOINT;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.augmentos.augmentoslib.PhoneNotification;
import com.augmentos.augmentoslib.events.NotificationEvent;
import com.augmentos.augmentos_core.augmentos_backend.BackendServerComms;
import com.augmentos.augmentos_core.augmentos_backend.VolleyJsonCallback;
import com.augmentos.augmentos_core.events.GoogleAuthFailedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class NotificationSystem {
    private static final String TAG = "NotificationSystem";

    private final ArrayList<PhoneNotification> notificationQueue;
    private BackendServerComms backendServerComms;
    private long lastDataSentTime = 0;
    Context context;

    public NotificationSystem(Context context) {
        notificationQueue = new ArrayList<>();
        backendServerComms = BackendServerComms.getInstance(context);
        EventBus.getDefault().register(this);
        this.context = context;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationEvent(NotificationEvent event) {
        PhoneNotification notif = new PhoneNotification(event.title, event.text, event.appName, event.timestamp, event.uuid);
        Log.d(TAG, "Received event: " + notif.toString());
        addNotification(notif);
    }

    public synchronized void addNotification(PhoneNotification notif) {
        // Remove existing notification with same title and appName
        notificationQueue.removeIf(existing ->
                existing.getTitle().equals(notif.getTitle()) &&
                        existing.getAppName().equals(notif.getAppName())
        );

        if (notificationQueue.size() >= 5) {
            notificationQueue.remove(0);
        }

        notificationQueue.add(notif);
        sendNotificationsRequest();
        Log.d(TAG, "Notification added to queue: " + notif);
    }

    public ArrayList<PhoneNotification> getNotificationQueue() {
        return notificationQueue;
    }

    public void sendNotificationsRequest() {
        try {
            JSONObject requestWrapper = new JSONObject();
            JSONArray notificationsArray = new JSONArray();

            for (PhoneNotification notif : notificationQueue) {
                JSONObject notifJson = new JSONObject();
                notifJson.put("title", notif.getTitle());
                notifJson.put("message", notif.getText());
                notifJson.put("appName", notif.getAppName());
                notifJson.put("timestamp", notif.getTimestamp());
                notifJson.put("uuid", notif.getUuid());
                notificationsArray.put(notifJson);
            }
            requestWrapper.put("notifications", notificationsArray);
            requestWrapper.put("userId", getUserId());

            backendServerComms.restRequest(SEND_NOTIFICATIONS_ENDPOINT, requestWrapper, new VolleyJsonCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    Log.d(TAG, "Request sent Successfully: " + result.toString());
                }

                @Override
                public void onFailure(int code) {
                    Log.d(TAG, "SOME FAILURE HAPPENED (sendNotificationsRequest)");
                    if (code == 401) {
                        EventBus.getDefault().post(new GoogleAuthFailedEvent("401 AUTH ERROR (requestUiPoll)"));
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error sending notifications: " + e.getMessage());
        }
    }

    private String getUserId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = prefs.getString("user_id", "");

        Log.d(TAG, "User ID: " + userId);

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

    public void parseSendNotificationsResult(JSONObject response) {
        Log.d(TAG, "Got result from server: " + response.toString());
    }

    private void updateLastDataSentTime() {
        lastDataSentTime = System.currentTimeMillis();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}