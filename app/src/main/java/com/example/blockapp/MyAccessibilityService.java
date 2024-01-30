package com.example.blockapp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private List<String> blackList = new ArrayList<>();

    private static final long TIMER_DURATION = 60 * 1000; // 1 минута в миллисекундах
    private static final long CHECK_INTERVAL = 1000; // Интервал проверки фокуса приложения

    private View blockView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private void showBlockLayout() {
        // Initialize WindowManager and layout parameters
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        // Inflate block_layout.xml to a View
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        blockView = inflater.inflate(R.layout.block_layout, null);

        // Find and set click listener for closeButton in block_layout
        ImageView closeButton = blockView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBlockLayout();
            }
        });

        // Add the View to the WindowManager
        windowManager.addView(blockView, params);


    }


    private void hideBlockLayout() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (windowManager != null && blockView != null) {
            windowManager.removeView(blockView);
            blockView = null;
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private boolean isTimerRunning = false;
    private CountDownTimer countDownTimer;

    private boolean isTimerUsed = false;

    private Handler handler = new Handler();

    private String currentFocusedPackage = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo != null) {
                String packageName = getPackageName(nodeInfo);
                String className = getClassName(nodeInfo);
                Log.d(TAG, "Package: " + packageName + ", Class: " + className);

                if (blackList.contains(packageName)) {
                    if (!isTimerRunning && !isTimerUsed) {
                        startTimer();
                        sendNotification("Вы запустили приложение из черного листа");
                    } else if (!isTimerRunning && isTimerUsed) {
                        // Ваш код для блокировки приложения после того, как таймер использован
                        // Например, вызов метода для блокировки приложения
                        blockApplication();
                        showBlockLayout(); // Show the block_layout immediately after blocking the application
                    }
                } else {
                    if (isTimerRunning) {
                        stopTimer();
                        sendNotification("Таймер остановлен");
                    }
                }

                currentFocusedPackage = packageName;

                nodeInfo.recycle();
            }
        }
    }

    // Runnable для проверки фокуса приложения
    private Runnable checkFocusRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerRunning && !isAppInForeground(currentFocusedPackage)) {
                stopTimer();
            }
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    // Проверка, находится ли приложение в фокусе
    private boolean isAppInForeground(String packageName) {
        // Ваша логика проверки фокуса приложения
        // Возможно, вам потребуется использовать другие методы AccessibilityService
        // для определения текущего фокусного приложения.
        // Например, AccessibilityServiceInfo.getResolveInfo().
        // Данная логика зависит от версии Android и вашего конкретного случая использования.
        // Вероятно, потребуется некоторая доработка.

        // Пример простой проверки: (это может потребовать дополнительной настройки)
        return packageName != null && packageName.equals(currentFocusedPackage);
    }

    // Методы для управления таймером
    private void startTimer() {
        isTimerRunning = true;
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sendNotification("Время вышло");
                showBlockLayout();
                isTimerRunning = false;
                isTimerUsed = true; // Устанавливаем флаг, что таймер был использован
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
    }

    // Ваш метод для блокировки приложения
    private void blockApplication() {
        // Ваш код для блокировки приложения
        // Например, вызов метода для блокировки приложения
    }

    private String getPackageName(AccessibilityNodeInfo nodeInfo) {
        while (nodeInfo != null) {
            if (nodeInfo.getPackageName() != null) {
                return nodeInfo.getPackageName().toString();
            }
            nodeInfo = nodeInfo.getParent();
        }
        return null;
    }

    private String getClassName(AccessibilityNodeInfo nodeInfo) {
        while (nodeInfo != null) {
            if (nodeInfo.getClassName() != null) {
                return nodeInfo.getClassName().toString();
            }
            nodeInfo = nodeInfo.getParent();
        }
        return null;
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt: something went wrong");
    }

    void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Уведомление")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build();

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notification);
    }

    private class LoadBlackListTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadFromNetwork(urls[0]);
            } catch (IOException e) {
                return "Unable to load content. Check your network connection.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                parseJsonResult(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String loadFromNetwork(String urlString) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            // Timeout for reading InputStream
            connection.setReadTimeout(3000);

            // Timeout for connection.connect()
            connection.setConnectTimeout(3000);

            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");

            // Set some general request properties.
            connection.setRequestProperty("Accept", "application/json");

            // Establish connection and retrieve the response code.
            connection.connect();
            int responseCode = connection.getResponseCode();

            // If the request was successful (response code 200),
            // read the InputStream and use it for parsing the JSON result.
            if (responseCode == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                result = readStream(stream);
            } else {
                result = "HTTP error code: " + responseCode;
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    private String readStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private void parseJsonResult(String result) throws JSONException {
        JSONObject json = new JSONObject(result);
        JSONArray appsArray = json.getJSONArray("apps");

        for (int i = 0; i < appsArray.length(); i++) {
            JSONObject appObject = appsArray.getJSONObject(i);
            String systemNameApp = appObject.getString("systemNameApp");
            blackList.add(systemNameApp);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        new LoadBlackListTask().execute("http://45.12.72.22:8005/getAllBlackListApps");

        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);
        Log.e(TAG, "onServiceConnected: ");
    }
}
