package com.applilandia.letmeknow.data;

import android.content.ContentValues;
import android.content.Context;

import com.applilandia.letmeknow.exceptions.AlarmException;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;

import java.util.List;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class TaskSet extends DbSet<Task> {

    private final static String LOG_TAG = TaskSet.class.getSimpleName();

    public TaskSet(Context context) {
        super(context);
    }

    public TaskSet(Context context, UnitOfWork unitOfWork) {
        super(context, unitOfWork);
    }

    @Override
    public long create(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, task.name);
        if (task.targetDateTime != null) {
            values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, task.targetDateTime.toString());
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME);
        }
        if (task.hasNotifications()) initWork();
        long rowId = mUnitOfWork.add(TaskContract.TaskEntry.TABLE_NAME, null, values);
        if (rowId > 0) {
            if (task.hasNotifications()) {
                long id = 0;
                NotificationSet notificationSet = new NotificationSet(mContext, mUnitOfWork);
                for (int index = 0; index < Notification.TypeNotification.values().length; index++) {
                    Notification notification = task.getNotifications().get(index);
                    if (notification != null) {
                        notification.taskId = (int) rowId;
                        try {
                            id = notificationSet.create(notification);
                        } catch (AlarmException e) {
                            id = -1;
                            e.printStackTrace();
                            break;
                        }
                    }
                }
                if (id < 0) {
                    endWork(false);
                    rowId = 0;
                } else {
                    endWork(true);
                }
            }
        }
        if (rowId > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
        }
        return rowId;
    }

    /**
     * Update a task with its notifications
     *
     * @param task
     * @return number of task updated, i.e., 1 if it was Ok.
     * if it wasn't updated it returns 0
     */
    @Override
    public int update(Task task) {
        //Fill values from Task entity
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, task.name);
        if (task.targetDateTime != null) {
            values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, task.targetDateTime.toString());
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME);
        }
        String where = TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(task._id)};

        initWork(); //Init the Transaction
        long id = 0; //id for notification
        int rowsAffected = mUnitOfWork.update(TaskContract.TaskEntry.TABLE_NAME, values, where, args);
        if (rowsAffected > 0) {
            //Remove all notifications
            NotificationSet notificationSet = new NotificationSet(mContext, mUnitOfWork);
            List<Notification> notifications = notificationSet.getSet(task._id);
            if (notifications != null) {
                for (Notification notification : notifications) {
                    if (!notificationSet.delete(notification)) {
                        throw new RuntimeException("Error deleting notifications");
                    }
                }
            }
            //Create new notifications if there are
            if (task.hasNotifications()) {
                for (int index = 0; index < Notification.TypeNotification.values().length; index++) {
                    Notification notification = task.getNotifications().get(index);
                    if (notification != null) {
                        notification.taskId = task._id;
                        try {
                            id = notificationSet.create(notification);
                        } catch (AlarmException e) {
                            id = -1;
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }
        }
        //End transaction. It is only right if id >= 0
        endWork(id >= 0);
        if (id >= 0)
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
        return rowsAffected;
    }

    @Override
    public boolean delete(Task task) {
        String where = TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(task._id)};
        int rowsAffected = mUnitOfWork.delete(TaskContract.TaskEntry.TABLE_NAME, where, args);
        if (rowsAffected > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean find(int id) {
        return false;
    }
}
