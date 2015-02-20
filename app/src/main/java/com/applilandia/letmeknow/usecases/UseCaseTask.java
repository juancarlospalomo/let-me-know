package com.applilandia.letmeknow.usecases;

import android.content.Context;
import android.database.Cursor;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.cross.Settings;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskSet;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 20/02/2015.
 * This class implements the Use Cases regarding to Tasks
 */
public class UseCaseTask {

    private Context mContext;

    public UseCaseTask(Context context) {
        mContext = context;
    }

    /**
     * Create a task.
     * Preconditions will be task is valid, then it will not validate the data
     *
     * @param task task to be created
     * @return true if the task was successfully created
     */
    public int createTask(Task task) {
        boolean hasNotifications = task.hasNotifications();

        //We check if we have to include the creation of notification
        if (!task.hasNotifications()) {
            //The task hasn't notifications included, so we must check the setting
            //to know if we must create a default notification
            if (Settings.getCreateDefaultNotificationValue(mContext)) {
                //Create the default notification.  It is the 5 minutes before
                Notification notification = new Notification();
                notification.type = Notification.TypeNotification.FiveMinutesBefore;
                notification.status = Notification.TypeStatus.Pending;
                notification.dateTime = Dates.addMinutes(task.targetDatetime, -5);
                task.addNotification(notification);
            }
        }

        TaskSet taskSet = new TaskSet(mContext);
        long taskId = taskSet.create(task);
        return (int) taskId;
    }

    /**
     * Recover a task with its notifications by its id
     *
     * @param id task identifier
     * @return task entity
     */
    public Task getTask(int id) {
        Task task = null;
        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.setUriTaskId(id),
                null, null, null, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            task = new Task();
            task._id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.ALIAS_ID));
            task.name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
            task.targetDatetime = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
            while (!cursor.isAfterLast()) {
                int notificationId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry._ID));
                if (notificationId > 0) {
                    Notification notification = new Notification();
                    notification._id = notificationId;
                    notification.dateTime = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME)));
                    notification.taskId = id;
                    notification.status = Notification.TypeStatus.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_STATUS)));
                    notification.type = Notification.TypeNotification.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TYPE)));
                    task.addNotification(notification);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return task;
    }

    /**
     * Recover all tasks stored in the repository
     *
     * @return List of tasks
     */
    public List<Task> getTasks() {
        List<Task> result = null;

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, null, null, TaskContract.TaskEntry.COLUMN_TASK_NAME + "," +
                        TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME);
        if ((cursor != null) && (cursor.moveToFirst())) {
            result = new ArrayList<Task>();
            while(!cursor.isAfterLast()) {
                Task task = new Task();
                task._id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                task.name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                task.targetDatetime = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
                result.add(task);
                cursor.moveToNext();
            }
        }
        return result;
    }

}
