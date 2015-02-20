package com.applilandia.letmeknow.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.applilandia.letmeknow.data.NotificationSet;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class AlarmService extends IntentService {
    //Service name for the worker thread.
    private final static String SERVICE_NAME = AlarmService.class.getSimpleName();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AlarmService() {
        super(SERVICE_NAME);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * name SERVICE_NAME with the intent that started the service.  When this
     * method returns, IntentService stops the service itself, so it hasn't to
     * call to stopItSelf()
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent!=null) {
            Bundle bundle = intent.getExtras();
            if (bundle!=null) {
                int id = bundle.getInt(NotificationSet.Alarm.NOTIFICATION_ID);
                if (id!=0) {
                    NotificationSet notificationSet = new NotificationSet(this);
                    notificationSet.send(id);
                }
            }
        }
    }
}
