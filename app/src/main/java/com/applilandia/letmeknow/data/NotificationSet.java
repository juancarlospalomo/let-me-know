package com.applilandia.letmeknow.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.applilandia.letmeknow.NotificationListActivity;
import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.exceptions.AlarmException;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.receiver.AlarmReceiver;
import com.applilandia.letmeknow.services.NotificationService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class NotificationSet extends DbSet<Notification> {

    private final static String LOG_TAG = NotificationSet.class.getSimpleName();

    public final static int LET_ME_KNOW_NOTIFICATION_ID = 1;

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
    private Notification get(int notificationId) {
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
    private Task getTask(int taskId) {
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
     * @param status status of the notifications that want be counted
     * @return number of notifications
     */
    public int getCount(Notification.TypeStatus status) {
        int result = 0;
        String where = "";
        if (status != null) {
            where = TaskContract.NotificationEntry.COLUMN_STATUS + "=" + status.getValue();
        }
        Cursor cursor = mUnitOfWork.get(TaskContract.NotificationEntry.TABLE_NAME, where
                , null, null);
        if (cursor != null) {
            result = cursor.getCount();
        }
        return result;
    }

    /**
     * Find out the sent notifications number
     *
     * @return
     */
    private int getSentNotificationCount() {
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
     * Build an pending intent for an activity
     *
     * @param actionId
     * @param taskId
     * @return
     */
    private PendingIntent getActivityContentIntent(int actionId, int taskId) {
        Intent intent = new Intent(mContext, NotificationListActivity.class);
        intent.putExtra(NotificationListActivity.INTENT_ACTION, actionId);
        intent.putExtra(NotificationListActivity.EXTRA_TASK_ID, taskId);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                actionId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * Build a Pending Intent for a service
     *
     * @param actionId
     * @param taskId
     * @return
     */
    private PendingIntent getServiceContentIntent(int actionId, int taskId) {
        Intent intent = new Intent(mContext, NotificationService.class);
        intent.putExtra(NotificationService.INTENT_ACTION, actionId);
        intent.putExtra(NotificationService.EXTRA_TASK_ID, taskId);
        PendingIntent pendingIntent = PendingIntent.getService(mContext,
                actionId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    /**
     * Build a query to get the task name of the notifications sent
     *
     * @return Cursor with the Task names
     */
    private Cursor getSentNotificationsTaskName() {
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
     * Send a multiple view notification
     */
    private void sendMultiple(int number) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mContext.getString(R.string.notification_title_tasks))
                .setContentText(mContext.getString(R.string.notification_content_tasks))
                .setNumber(number)
                .setContentIntent(getActivityContentIntent(NotificationListActivity.ACTION_NONE, 0))
                .setAutoCancel(true);

        Cursor cursor = getSentNotificationsTaskName();
        if ((cursor != null) && (cursor.moveToFirst())) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle(mContext.getResources().getString(R.string.notifications_tracker_details));
            // Moves events into the expanded layout
            while (!cursor.isAfterLast()) {
                String line = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                inboxStyle.addLine(line);
                cursor.moveToNext();
            }
            // Moves the expanded layout object into the notification object.
            builder.setStyle(inboxStyle);
        }
        cursor.close();
        // Now, issue the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(LET_ME_KNOW_NOTIFICATION_ID, builder.build());
    }

    /**
     * Send a single notification for a specific one
     *
     * @param id notification identifier
     */
    private void sendSingle(int id, int taskId) {
        Task task = null;
        task = getTask(taskId);
        if (task != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(task.name)
                    .setContentText(task.targetDateTime.getDisplayFormat(mContext))
                    .setContentIntent(getActivityContentIntent(NotificationListActivity.ACTION_NONE, task._id))
                    .setAutoCancel(true);

            builder.addAction(R.drawable.ic_alarm_on, "",
                    getActivityContentIntent(NotificationListActivity.ACTION_VIEW,
                            task._id));

            builder.addAction(R.drawable.ic_check_off, "",
                    getServiceContentIntent(NotificationService.ACTION_END_TASK,
                            task._id));

            builder.addAction(R.drawable.ic_clear, "",
                    getServiceContentIntent(NotificationService.ACTION_DISMISS,
                            task._id));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(LET_ME_KNOW_NOTIFICATION_ID, builder.build());
        }
    }


    /**
     * Send a notification to the notification bar
     * // Refer to: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
     * // http://developer.android.com/design/patterns/notifications.html
     *
     * @param id notification identifier
     */
    public void send(int id) {
        initWork();
        changeStatus(id, Notification.TypeStatus.Sent);
        int count = getSentNotificationCount();
        Notification notification = get(id);
        if (notification != null) {
            if (count == 1) {
                sendSingle(id, notification.taskId);
            } else {
                sendMultiple(count);
            }
        }
        endWork(true);
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
            intent.putExtra(NOTIFICATION_ID, 0);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent);
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
