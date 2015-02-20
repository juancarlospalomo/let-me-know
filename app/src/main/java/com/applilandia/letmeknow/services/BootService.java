package com.applilandia.letmeknow.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.data.TaskContract;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class BootService extends IntentService {
    //Service name for the worker thread.
    private final static String SERVICE_NAME = BootService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BootService() {
        super(SERVICE_NAME);
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * name SERVICE_NAME with the intent that started the service.  When this
     * method returns, IntentService tops the service itself, so it hasn't to
     * call to stopItSelf()
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        restoreAlarms();
    }

    /**
     * Create alarms again if notifications exist as they are lost
     * after reboot the device
     */
    protected void restoreAlarms() {
        Cursor cursor = this.getContentResolver().query(TaskContract.NotificationEntry.CONTENT_URI,
                null, null, null, null);
        //Once get the stored notifications, create the alarms
        if ((cursor!=null) && (cursor.moveToFirst())) {
            while (!cursor.isAfterLast()) {
                String dateTime = cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME));
                int notificationId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry._ID));
                NotificationSet.Alarm alarm = new NotificationSet(this).new Alarm();
                if (!alarm.create(notificationId, Dates.getDate(dateTime))) {
                    //TODO: trace log
                }
                alarm = null;
                cursor.moveToNext();
            }
        }
        //Always close the cursor
        cursor.close();
    }
}
