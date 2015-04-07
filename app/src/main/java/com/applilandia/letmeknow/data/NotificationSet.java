package com.applilandia.letmeknow.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.cross.Settings;
import com.applilandia.letmeknow.exceptions.AlarmException;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.receiver.AlarmReceiver;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class NotificationSet extends DbSet<Notification> {

    private final static String LOG_TAG = NotificationSet.class.getSimpleName();

    public NotificationSet(Context context) {
        super(context);
    }

    public NotificationSet(Context context, UnitOfWork unitOfWork) {
        super(context, unitOfWork);
    }

    @Override
    public long create(Notification notification) throws AlarmException {
        ContentValues values = new ContentValues();
        values.put(TaskContract.NotificationEntry.COLUMN_TASK_ID, notification.taskId);
        values.put(TaskContract.NotificationEntry.COLUMN_DATE_TIME, new LocalDate(notification.dateTime).toString());
        values.put(TaskContract.NotificationEntry.COLUMN_STATUS, notification.status.getValue());
        values.put(TaskContract.NotificationEntry.COLUMN_TYPE, notification.type.getValue());
        long rowId = mUnitOfWork.add(TaskContract.NotificationEntry.TABLE_NAME, null, values);
        if (rowId > 0) {
            Alarm alarm = new Alarm();
            if (alarm.create((int) rowId, notification.dateTime)) {
                mContext.getContentResolver().notifyChange(TaskContract.NotificationEntry.CONTENT_URI, null);
            } else {
                throw new AlarmException("alarm couldn't be created");
            }
        }
        return rowId;
    }

    @Override
    public int update(Notification notification) throws AlarmException {
        ContentValues values = new ContentValues();
        values.put(TaskContract.NotificationEntry.COLUMN_TASK_ID, notification.taskId);
        values.put(TaskContract.NotificationEntry.COLUMN_DATE_TIME, new LocalDate(notification.dateTime).toString());
        values.put(TaskContract.NotificationEntry.COLUMN_TYPE, notification.type.getValue());
        values.put(TaskContract.NotificationEntry.COLUMN_STATUS, notification.status.getValue());

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
                throw new AlarmException("Alarm couldn't be created");
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

    /**
     * Change the status of a notification
     *
     * @param id        notification identifier
     * @param newStatus new type status
     * @return
     */
    public int changeStatus(int id, Notification.TypeStatus newStatus) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.NotificationEntry.COLUMN_STATUS, newStatus.getValue());

        String where = TaskContract.NotificationEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(id)};

        int rowsAffected = mUnitOfWork.update(TaskContract.NotificationEntry.TABLE_NAME,
                values, where, args);
        return rowsAffected;
    }

    /**
     * Remove the notifications with "Sent" status that belong to a Task
     *
     * @param taskId
     */
    public void deleteSentNotification(int taskId) {
        String sql = "DELETE FROM " + TaskContract.NotificationEntry.TABLE_NAME +
                " WHERE " + TaskContract.NotificationEntry.COLUMN_TASK_ID + "=" + taskId +
                " AND " + TaskContract.NotificationEntry.COLUMN_STATUS + "=" + Notification.TypeStatus.Sent.getValue();
        mUnitOfWork.mDatabase.execSQL(sql);
        mContext.getContentResolver().notifyChange(TaskContract.NotificationEntry.CONTENT_URI, null);
    }

    /**
     * Delete all notifications sent
     */
    public void deleteNotifications(Notification.TypeStatus status) {
        String sql = "DELETE FROM " + TaskContract.NotificationEntry.TABLE_NAME +
                " WHERE " + TaskContract.NotificationEntry.COLUMN_STATUS + "=" + status.getValue();
        mUnitOfWork.mDatabase.execSQL(sql);
    }

    /**
     * Return a notification entity
     *
     * @param notificationId
     * @return
     */
    public Notification get(int notificationId) {
        Notification notification = null;
        Cursor cursor = mUnitOfWork.get(TaskContract.NotificationEntry.TABLE_NAME,
                TaskContract.NotificationEntry._ID + "=?",
                new String[]{String.valueOf(notificationId)},
                null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            notification = new Notification();
            notification._id = notificationId;
            notification.taskId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TASK_ID));
            notification.status = Notification.TypeStatus.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_STATUS)));
            notification.type = Notification.TypeNotification.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TYPE)));
            try {
                notification.dateTime = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME))).getDateTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return notification;
    }

    /**
     * Get all the notifications belonging to a specific task
     *
     * @param taskId task identifier
     * @return List of Notification
     */
    public List<Notification> getSet(int taskId) {
        List<Notification> result = null;
        Cursor cursor = mUnitOfWork.get(TaskContract.NotificationEntry.TABLE_NAME,
                TaskContract.NotificationEntry.COLUMN_TASK_ID + "=?",
                new String[]{String.valueOf(taskId)},
                null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            result = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                Notification notification = new Notification();
                notification._id = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry._ID));
                try {
                    notification.dateTime = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME))).getDateTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                notification.type = Notification.TypeNotification.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TYPE)));
                notification.status = Notification.TypeStatus.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_STATUS)));
                notification.taskId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TASK_ID));
                result.add(notification);
                cursor.moveToNext();
            }
        }

        cursor.close();
        return result;
    }

    /**
     * Return a task entity
     *
     * @param taskId
     * @return
     */
    public Task getTask(int taskId) {
        Task task = null;
        Cursor cursor = mUnitOfWork.get(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry._ID + "=?", new String[]{String.valueOf(taskId)},
                null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            task = new Task();
            task._id = taskId;
            task.name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
            try {
                task.targetDateTime = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return task;
    }

    /**
     * Get the number of notifications. The number can be filtered by status too
     *
     * @param status status of the notifications that want be counted
     * @return number of notifications
     */
    public int getCount(Notification.TypeStatus status) {
        int result = 0;
        String where = "";
        if (status != null) {
            where = TaskContract.NotificationEntry.COLUMN_STATUS + "=" + status.getValue();
        }
        try {
            Cursor cursor = mUnitOfWork.get(TaskContract.NotificationEntry.TABLE_NAME, where
                    , null, null);
            if (cursor != null) {
                result = cursor.getCount();
                cursor.close();
            }
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.getMessage());
        }
        return result;
    }

    /**
     * Find out the sent notifications number
     *
     * @return
     */
    public int getSentNotificationCount() {
        String sql = "SELECT " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME +
                " FROM " + TaskContract.TaskEntry.TABLE_NAME + " INNER JOIN " + TaskContract.NotificationEntry.TABLE_NAME +
                " ON " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=" +
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_TASK_ID +
                " WHERE " + TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_STATUS + "=" +
                Notification.TypeStatus.Sent.getValue() +
                " GROUP BY " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME;
        Cursor cursor = mUnitOfWork.mDatabase.rawQuery(sql, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            return cursor.getCount();
        }
        return 0;
    }


    /**
     * Build a query to get the task name of the notifications sent
     *
     * @return Cursor with the Task names
     */
    public Cursor getSentNotificationsTaskName() {
        String sql = "SELECT " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME +
                " FROM " + TaskContract.TaskEntry.TABLE_NAME + " INNER JOIN " + TaskContract.NotificationEntry.TABLE_NAME +
                " ON " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=" +
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_TASK_ID +
                " WHERE " + TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_STATUS + "=" +
                Notification.TypeStatus.Sent.getValue() +
                " GROUP BY " + TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME;
        return mUnitOfWork.mDatabase.rawQuery(sql, null);
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
         * Create a daily repetitive alarm
         *
         * @param timeInMillis time in milliseconds when the Alarm has to be triggered
         */
        public void create(long timeInMillis) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mContext, AlarmReceiver.class);
            intent.putExtra(NOTIFICATION_ID, Settings.DAILY_NOTIFICATION_ID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, Settings.DAILY_NOTIFICATION_ID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }

        /**
         * Create a repetitive alarm based on the passed time
         * @param time if the passed time is null, it will take the value from the setting
         */
        public void create(String time) {
            if (TextUtils.isEmpty(time)) {
                time = Settings.getDailyTriggerNotificationValue(mContext);
            }
            LocalDate date = new LocalDate();
            date.setTime(time);
            int hour = date.getHour();
            int minute = date.getMinute();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            create(calendar.getTimeInMillis());
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
