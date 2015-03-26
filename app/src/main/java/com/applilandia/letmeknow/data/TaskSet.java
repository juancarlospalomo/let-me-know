package com.applilandia.letmeknow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

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
        long rowId = 0;
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, task.name);
        if (task.targetDateTime != null) {
            values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, task.targetDateTime.toString());
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME);
        }
        try {
            if (task.hasNotifications()) initWork();
            rowId = mUnitOfWork.add(TaskContract.TaskEntry.TABLE_NAME, null, values);
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
        } catch (SQLiteException exception) {
            Log.e(LOG_TAG, exception.getMessage());
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
            removeNotifications(task._id);
            //Create new notifications if there are
            if (task.hasNotifications()) {
                NotificationSet notificationSet = new NotificationSet(mContext, mUnitOfWork);
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

    /**
     * Delete a task and their notifications
     * @param task
     * @return true if it was successfully deleted
     */
    @Override
    public boolean delete(Task task) {
        boolean result = false;
        boolean startNewWork = false;
        String where = TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(task._id)};

        if (!mUnitOfWork.isWorkStarted()) {
            //If it isn't inside a work, we need to create a new one.
            //It is a new work, because we need this execution is in a work
            startNewWork = true;
        }
        if (startNewWork) initWork();
        int rowsAffected = mUnitOfWork.delete(TaskContract.TaskEntry.TABLE_NAME, where, args);
        if (rowsAffected > 0) {
            try {
                removeNotifications(task._id);
                mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
                result = true;
            } catch(RuntimeException e) {
                result = false;
            }
        }
        if (startNewWork) {
            //Execute when initWork() has been executed. endWork(result);
            endWork(result);
        }
        return result;
    }

    @Override
    public boolean find(int id) {
        return false;
    }

    /**
     * Remove the notifications that belong to one task
     * @param taskId
     */
    private void removeNotifications(int taskId) {
        NotificationSet notificationSet = new NotificationSet(mContext, mUnitOfWork);
        //get all current notifications
        List<Notification> notifications = notificationSet.getSet(taskId);
        if (notifications != null) {
            for (Notification notification : notifications) {
                if (!notificationSet.delete(notification)) {
                    throw new RuntimeException("Error deleting notifications");
                }
            }
        }
    }
}
