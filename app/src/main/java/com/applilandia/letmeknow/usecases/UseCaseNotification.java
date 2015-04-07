package com.applilandia.letmeknow.usecases;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.applilandia.letmeknow.LetMeKnowApp;
import com.applilandia.letmeknow.NotificationListActivity;
import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.services.NotificationService;

import java.util.List;

/**
 * Created by JuanCarlos on 24/03/2015.
 */
public class UseCaseNotification {

    private final static String LOG_TAG = UseCaseNotification.class.getSimpleName();

    public final static int LET_ME_KNOW_NOTIFICATION_ID = 1;
    public final static int LET_ME_KNOW_DAILY_NOTIFICATION = 2;

    private Context mContext;

    public UseCaseNotification(Context context) {
        mContext = context;
    }

    /**
     * Return if sent notifications exist
     *
     * @return
     */
    public boolean existInSentStatus() {
        boolean result = false;

        NotificationSet notificationSet = new NotificationSet(mContext);
        result = notificationSet.getCount(Notification.TypeStatus.Sent) > 0;

        return result;
    }

    /**
     * Remove all sent notifications
     */
    public void removeSent() {
        NotificationSet notificationSet = new NotificationSet(mContext);
        notificationSet.deleteNotifications(Notification.TypeStatus.Sent);
        //clear Sent Notification App variable to state there aren't any more
        LetMeKnowApp.clearSentNotification();
    }

    /**
     * Send a notification with the tasks for today if there are any
     */
    public void sendTodayTaskNotifications() {
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasksByType(Task.TypeTask.Today);
        if ((taskList != null) && (taskList.size() > 0)) {
            sendMultipleTodayNotification(taskList);
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
        NotificationSet notificationSet = new NotificationSet(mContext);
        notificationSet.initWork();
        notificationSet.changeStatus(id, Notification.TypeStatus.Sent);
        int count = notificationSet.getSentNotificationCount();
        Notification notification = notificationSet.get(id);
        if (notification != null) {
            if (count == 1) {
                sendSingle(notification.taskId);
            } else {
                sendMultiple(count);
            }
        }
        notificationSet.endWork(true);
    }

    /**
     * Send a single notification for a specific one
     *
     * @param taskId task identifier
     */
    private void sendSingle(int taskId) {
        Task task = null;
        NotificationSet notificationSet = new NotificationSet(mContext);
        task = notificationSet.getTask(taskId);
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
     * Send a daily notification for a single task
     *
     * @param task today task
     */
    private void sendSingleTodayNotification(Task task) {
        NotificationSet notificationSet = new NotificationSet(mContext);
        if (task != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(task.name)
                    .setContentText(task.targetDateTime.getDisplayFormat(mContext))
                    .setContentIntent(getActivityContentIntent(NotificationListActivity.ACTION_NONE, task._id))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(LET_ME_KNOW_DAILY_NOTIFICATION, builder.build());
        }
    }

    /**
     * Send a multiple view notification
     */
    private void sendMultiple(int number) {
        NotificationSet notificationSet = new NotificationSet(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mContext.getString(R.string.notification_title_tasks))
                .setContentText(mContext.getString(R.string.notification_content_tasks))
                .setNumber(number)
                .setContentIntent(getActivityContentIntent(NotificationListActivity.ACTION_NONE, 0))
                .setAutoCancel(true);

        Cursor cursor = notificationSet.getSentNotificationsTaskName();
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
     * Send a multiple notification for today task in this moment
     *
     * @param taskList list of tasks for today
     */
    private void sendMultipleTodayNotification(List<Task> taskList) {
        if ((taskList != null) && (taskList.size() > 0)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(mContext.getString(R.string.notification_title_today_tasks))
                    .setContentText(mContext.getString(R.string.notification_content_tasks))
                    .setNumber(taskList.size())
                    .setContentIntent(getActivityContentIntent(NotificationListActivity.ACTION_NONE, 0))
                    .setAutoCancel(true);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle(mContext.getResources().getString(R.string.notifications_tracker_details));
            // Moves events into the expanded layout
            for (int index = 0; index < taskList.size(); index++) {
                Task task = taskList.get(index);
                String line = task.name;
                if (task.targetDateTime != null) {
                    line += "" + task.targetDateTime.getDisplayFormat(mContext);
                    inboxStyle.addLine(line);
                }
            }
            // Moves the expanded layout object into the notification object.
            builder.setStyle(inboxStyle);
            // Now, issue the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(LET_ME_KNOW_NOTIFICATION_ID, builder.build());
        }
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


}
