package com.applilandia.letmeknow.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.applilandia.letmeknow.services.AlarmService;

/**
 * Created by JuanCarlos on 20/02/2015.
 * Called when an alarm is triggered
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Create component for the service
        ComponentName componentName = new ComponentName(context.getPackageName(),
                AlarmService.class.getName());
        startWakefulService(context, intent.setComponent(componentName));
    }
}
