package com.applilandia.letmeknow.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.receiver.AlarmReceiver;

import java.util.Date;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class NotificationSet extends DbSet<Notification> {

    public NotificationSet(Context context) {
        super(context);
    }

    public NotificationSet(Context context, UnitOfWork unitOfWork) {
        super(context, unitOfWork);
    }

    @Override
    public long create(Notification notification) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.NotificationEntry.COLUMN_TASK_ID, notification.taskId);
        values.put(TaskContract.NotificationEntry.COLUMN_DATE_TIME , Dates.castToDatabaseFormat(notification.dateTime));
        values.put(TaskContract.NotificationEntry.COLUMN_STATUS , notification.status.getValue());
        values.put(TaskContract.NotificationEntry.COLUMN_TYPE, notification.type.getValue());
        long rowId = mUnitOfWork.add(TaskContract.NotificationEntry.TABLE_NAME, null, values);
        if (rowId > 0) {
            Alarm alarm = new Alarm();
            if (alarm.create((int)rowId, notification.dateTime)) {
                mContext.getContentResolver().notifyChange(TaskContract.NotificationEntry.CONTENT_URI, null);
            } else {
                //TODO: raise exception
            }
        }
        return rowId;
    }

    @Override
    public int update(Notification notification) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.NotificationEntry.COLUMN_TASK_ID, notification.taskId);
        values.put(TaskContract.NotificationEntry.COLUMN_DATE_TIME , Dates.castToDatabaseFormat(notification.dateTime));
        values.put(TaskContract.NotificationEntry.COLUMN_TYPE, notification.type.getValue());
        values.put(TaskContract.NotificationEntry.COLUMN_STATUS , notification.status.getValue());

        String where = TaskContract.NotificationEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(notification._id)};

        int rowsAffected = mUnitOfWork.update(TaskContract.NotificationEntry.TABLE_NAME,
                values, where, args);
        if (rowsAffected > 0) {
            Alarm alarm = new Alarm();
            alarm.remove(notification._id);
            if (alarm.create(notification._id, notification.dateTime)) {
                mContext.getContentResolver().notifyChange(TaskContract.NotificationEntry.CONTENT_URI, null);
            } else {
                //TODO: raise exception
            }
        }
        return rowsAffected;
    }

    @Override
    public boolean delete(Notification notification) {
        String where = TaskContract.NotificationEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(notification._id)};
        int rowsAffected = mUnitOfWork.delete(TaskContract.NotificationEntry.TABLE_NAME, where, args);
        if (rowsAffected > 0) {
            Alarm alarm = new Alarm();
            alarm.remove(notification._id);
            mContext.getContentResolver().notifyChange(TaskContract.NotificationEntry.CONTENT_URI, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean find(int id) {
        return false;
    }

    //TODO: Build the notification correctly
    // Refer to: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //           http://developer.android.com/design/patterns/notifications.html
    public void send(int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("LetMeKnow")
                .setContentText("Task A")
                .setAutoCancel(true);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String[] events = new String[6];
        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Event tracker details:");
        // Moves events into the expanded layout
        for (int i = 0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }
        // Moves the expanded layout object into the notification object.
        builder.setStyle(inboxStyle);
        // Issue the notification here.

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(id, builder.build());

        //TODO: update the notification to sent status
    }


    /**
     * Class to set and cancel the alarms
     */
    public final class Alarm {

        public final static String NOTIFICATION_ID = "notification_id";

        /**
         * Schedule an alarm to be broadcasted with PendingIntent to AlarmReceiver
         *
         * @param notificationId = to identify the intent
         * @param dateTime       = datetime to set the alarm.  It must be in database format
         * @return true if the alarm could be set
         */
        public boolean create(int notificationId, Date dateTime) {
            boolean result = false;
            if (dateTime != null) {
                AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(mContext, AlarmReceiver.class);
                intent.putExtra(NOTIFICATION_ID, notificationId);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                        notificationId,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, dateTime.getTime(), pendingIntent);
                result = true;
            }
            return result;
        }

        /**
         * Cancel an alarm for a notification
         *
         * @param notificationId = notification identifier
         */
        public void remove(int notificationId) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mContext, AlarmReceiver.class);
            intent.putExtra(NOTIFICATION_ID, notificationId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                    notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }

    }

}
