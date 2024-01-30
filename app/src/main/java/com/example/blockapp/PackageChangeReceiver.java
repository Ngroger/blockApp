package com.example.blockapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                    intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ||
                    intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                // Обработка изменений в установленных приложениях
                // Вы можете отправить уведомление или выполнить другие действия
                Log.d("PackageChangeReceiver", "Package change: " + packageName);
            }
        }
    }
}
