package com.applilandia.letmeknow;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.data.HistorySet;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskSet;
import com.applilandia.letmeknow.exceptions.AlarmException;
import com.applilandia.letmeknow.models.History;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;

import java.util.Date;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class testEntitiesSet extends AndroidTestCase {

    private Task generateTaskEntity(String name, Date date) {
        Task task = new Task();
        task.name = name;
        task.targetDatetime = date;
        return task;
    }

    private Notification generateNotificationEntity(long taskId, Date date,
                                                    Notification.TypeNotification type,
                                                    Notification.TypeStatus status) {
        Notification notification = new Notification();
        notification.taskId = (int) taskId;
        notification.dateTime = date;
        notification.status = status;
        notification.type = type;
        return notification;
    }

    private History generateHistoryEntity(String name, Date targetDate, Date completedDate) {
        History history = new History();
        history.name = name;
        history.targetDate = targetDate;
        history.completedDate = completedDate;
        return history;
    }

    public void testCreateTask() {
        Task task = generateTaskEntity("task 1", new Date());
        TaskSet taskSet = new TaskSet(mContext);
        long taskId = taskSet.create(task);
        assertTrue(taskId > 0);

        if (taskId > 0) {
            //To remove the seconds
            task.targetDatetime = Dates.getDate(Dates.castToDatabaseFormat(task.targetDatetime));
            Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.setUriTaskId(taskId),
                    null, null, null, null);
            assertFalse(cursor == null);
            if (cursor != null) {
                assertEquals(1, cursor.getCount());
                cursor.moveToFirst();
                Date date = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
                String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                Task retTask = generateTaskEntity(taskName, date);
                assertEquals(task, retTask);
            }
        }

    }

    public void testCreateNotification() {
        Task task = generateTaskEntity("task 1", new Date());
        TaskSet taskSet = new TaskSet(mContext);
        taskSet.initWork();
        long taskId = taskSet.create(task);
        if (taskId > 0) {
            Notification notification = generateNotificationEntity(taskId, new Date(),
                    Notification.TypeNotification.FiveMinutesBefore,
                    Notification.TypeStatus.Pending);
            NotificationSet notificationSet = new NotificationSet(mContext, taskSet.getUnitOfWork());
            long id = 0;
            try {
                id = notificationSet.create(notification);
            } catch (AlarmException e) {
                e.printStackTrace();
            }
            if (id > 0) {
                taskSet.endWork(true);
                Cursor cursor = mContext.getContentResolver().query(TaskContract.NotificationEntry.setUriNotificationId(id),
                        null, null, null, null);
                if (cursor != null) {
                    assertEquals(1, cursor.getCount());
                    cursor.moveToFirst();
                    taskId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TASK_ID));
                    Date date = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME)));
                    Notification.TypeNotification type = Notification.TypeNotification.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TYPE)));
                    Notification.TypeStatus status = Notification.TypeStatus.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_STATUS)));
                    Notification retNotification = generateNotificationEntity(taskId, date, type, status);
                    //To remove the seconds
                    notification.dateTime = Dates.getDate(Dates.castToDatabaseFormat(notification.dateTime));
                    assertEquals(notification, retNotification);
                }
            } else {
                taskSet.endWork(false);
            }
        } else {
            taskSet.endWork(false);
        }
    }

    public void testCreateHistory() {
        History history = generateHistoryEntity("task history 1", new Date(), Dates.addDays(new Date(), 1));
        HistorySet historySet = new HistorySet(mContext);
        long id = historySet.create(history);
        if (id > 0) {
            Cursor cursor = mContext.getContentResolver().query(TaskContract.HistoryEntry.CONTENT_URI,
                    null, TaskContract.HistoryEntry._ID + "=?",
                    new String[]{String.valueOf(id)}, null);
            if (cursor != null) {
                assertEquals(1, cursor.getCount());
                cursor.moveToFirst();
                String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.HistoryEntry.COLUMN_TASK_NAME));
                Date targetDate = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.HistoryEntry.COLUMN_TARGET_DATE_TIME)));
                Date completedDate = Dates.getDate(cursor.getString(cursor.getColumnIndex(TaskContract.HistoryEntry.COLUMN_COMPLETED_DATE_TIME)));
                History retHistory = generateHistoryEntity(taskName, targetDate, completedDate);
                //To remove the seconds from the original object
                history.targetDate = Dates.getDate(Dates.castToDatabaseFormat(history.targetDate));
                history.completedDate = Dates.getDate(Dates.castToDatabaseFormat(history.completedDate));
                assertEquals(history, retHistory);
            }
        } else {
            historySet.endWork(false);
        }

    }

}
