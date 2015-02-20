package com.applilandia.letmeknow;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskSet;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;

import java.util.Date;
import java.util.List;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class testTaskUseCases extends AndroidTestCase {


    private int createTaskWithoutNotification() {
        Task task = new Task();
        task.name = "task without notification";
        task.targetDatetime = new Date();

        TaskSet taskSet = new TaskSet(mContext);
        return (int) taskSet.create(task);
    }

    public void testShouldCreateTaskWithDefaultNotifications() {
        Task task = new Task();
        task.name = "task shouldcreatetask use case";
        task.targetDatetime = Dates.addDays(new Date(), 1);

        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        int taskId = useCaseTask.createTask(task);
        assertTrue(taskId != -1);

    }

    public void testShouldGetTaskWithoutNotifications() {
        int id = createTaskWithoutNotification();
        assertTrue(id > 0);
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        Task task = useCaseTask.getTask(id);
        assertTrue(task != null);
        assertEquals(false, task.hasNotifications());
    }

    public void testShouldGetTaskWithNotifications() {
        Task task = new Task();
        task.name = "task with notifications";
        //Take the current date without seconds
        task.targetDatetime = Dates.getDate(Dates.getCurrentDateTime());
        //add a notification
        Notification notification = new Notification();
        notification.dateTime = Dates.addHour(task.targetDatetime, -1);
        notification.type = Notification.TypeNotification.OneHourBefore;
        notification.status = Notification.TypeStatus.Pending;
        task.addNotification(notification);
        notification = new Notification();
        notification.dateTime = Dates.addDays(task.targetDatetime, -7);
        notification.type = Notification.TypeNotification.OneWeekBefore;
        notification.status = Notification.TypeStatus.Pending;
        task.addNotification(notification);

        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        int id = useCaseTask.createTask(task);
        assertTrue(id > 0);

        //We check now if the data are correct
        Task result = useCaseTask.getTask(id);
        assertEquals(task, result);
    }

    public void testShouldReturnAllTasks() {
        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, null, null, null);
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasks();
        assertEquals(cursor.getCount(), taskList.size());
    }

}
