package com.applilandia.letmeknow;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.data.HistorySet;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskSet;
import com.applilandia.letmeknow.models.History;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class testEntitiesSet extends AndroidTestCase {

    private Task generateTaskEntity(String name, LocalDate date) {
        Task task = new Task();
        task.name = name;
        task.targetDateTime = date;
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

    private History generateHistoryEntity(String name, LocalDate targetDate, LocalDate completedDate) {
        History history = new History();
        history.name = name;
        history.targetDate = targetDate;
        history.completedDate = completedDate;
        return history;
    }

    public void testCreateTask() {
        Task task = generateTaskEntity("task 1", new LocalDate());
        TaskSet taskSet = new TaskSet(mContext);
        long taskId = taskSet.create(task);
        assertTrue(taskId > 0);

        if (taskId > 0) {
            Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.setUriTaskId(taskId),
                    null, null, null, null);
            assertFalse(cursor == null);
            if (cursor != null) {
                assertEquals(1, cursor.getCount());
                cursor.moveToFirst();
                LocalDate date = null;
                try {
                    date = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                Task retTask = generateTaskEntity(taskName, date);
                assertEquals(task, retTask);
            }
        }

    }

    public void testCreateHistory() {
        History history = generateHistoryEntity("task history 1", new LocalDate(), new LocalDate().addDays(1));
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
                LocalDate targetDate = null;
                try {
                    targetDate = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.HistoryEntry.COLUMN_TARGET_DATE_TIME)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                LocalDate completedDate = null;
                try {
                    completedDate = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.HistoryEntry.COLUMN_COMPLETED_DATE_TIME)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                History retHistory = generateHistoryEntity(taskName, targetDate, completedDate);
                assertEquals(history, retHistory);
            }
        } else {
            historySet.endWork(false);
        }

    }

}
