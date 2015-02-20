package com.applilandia.letmeknow.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.applilandia.letmeknow.services.BootService;

/**
 * Created by JuanCarlos on 20/02/2015.
 * <p/>
 * Triggered when the device is booted.
 * Registered in AndroidManifest.xml
 */
public class BootReceiver extends WakefulBroadcastReceiver{

    /**
     * Called when action BOOT_COMPLETED is broadcasted
     * @param context = calling context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Create component for the service
            ComponentName componentName = new ComponentName(context.getPackageName(),
                    BootService.class.getName());
            startWakefulService(context, intent.setComponent(componentName));
        }
    }
}
