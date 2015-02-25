package com.applilandia.letmeknow;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskDbHelper;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class testTaskUseCases extends AndroidTestCase {

    private void resetDatabase() {
        SQLiteDatabase db = new TaskDbHelper(mContext).getWritableDatabase();
        db.delete(TaskContract.NotificationEntry.TABLE_NAME, null, null);
        db.delete(TaskContract.HistoryEntry.TABLE_NAME, null, null);
        db.delete(TaskContract.TaskEntry.TABLE_NAME, null, null);
        db.close();
    }

    private List<Task> createExpiredTasks() {
        List<Task> result = new ArrayList<Task>();
        LocalDate date = new LocalDate();
        date.addDays(-1);
        date.removeTime();
        Task task = new Task();
        task.name = "expired without time";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Expired;
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        result.add(task);

        date = new LocalDate();
        date.addHours(-5);
        task = new Task();
        task.name = "expired with time 5 hours less";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Expired;
        task._id = useCaseTask.createTask(task);
        task.removeNotifications();
        result.add(task);

        date = new LocalDate();
        date.addMinutes(-5);
        task = new Task();
        task.name = "expired with time 5 minutes less";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Expired;
        task._id = useCaseTask.createTask(task);
        task.removeNotifications();
        result.add(task);

        return result;
    }

    private List<Task> createTodayTasks() {
        List<Task> result = new ArrayList<Task>();
        LocalDate date = new LocalDate();
        date.removeTime();
        Task task = new Task();
        task.name = "today without time";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Today;
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        result.add(task);

        date = new LocalDate();
        date.addHours(3);
        task = new Task();
        task.name = "today with time 3 hours later";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Today;
        task._id = useCaseTask.createTask(task);
        task.removeNotifications();
        result.add(task);

        date = new LocalDate();
        date.addMinutes(50);
        task = new Task();
        task.name = "today with time 50 minutes later";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Today;
        task._id = useCaseTask.createTask(task);
        task.removeNotifications();
        result.add(task);

        return result;
    }

    private List<Task> createAnyTimeTasks() {
        List<Task> result = new ArrayList<Task>();
        LocalDate date = new LocalDate();
        date.removeTime();
        Task task = new Task();
        task.name = "any time task";
        task.typeTask = Task.TypeTask.AnyTime;
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        result.add(task);

        date = new LocalDate();
        date.removeTime();
        task = new Task();
        task.name = "last any time task";
        task.typeTask = Task.TypeTask.AnyTime;
        useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        result.add(task);

        return result;
    }

    private List<Task> createFutureTasks() {
        List<Task> result = new ArrayList<Task>();
        LocalDate date = new LocalDate().addDays(1);
        date.removeTime();
        Task task = new Task();
        task.name = "Future task without time";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Future;
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        result.add(task);

        date = new LocalDate().addDays(2);
        task = new Task();
        task.name = "Future task with time";
        task.targetDateTime = date;
        task.typeTask = Task.TypeTask.Future;
        useCaseTask = new UseCaseTask(mContext);
        task._id = useCaseTask.createTask(task);
        task.removeNotifications();
        result.add(task);

        return result;
    }

    public void testShouldReturnAllTasks() {
        resetDatabase();
        List<Task> expectedExpiredTask = createExpiredTasks();
        List<Task> expectedTodayTask = createTodayTasks();
        List<Task> expectedAnyTimeTask = createAnyTimeTasks();
        List<Task> expectedFutureTask = createFutureTasks();
        List<Task> expectedAllTask = new ArrayList<Task>();
        expectedAllTask.addAll(expectedExpiredTask);
        expectedAllTask.addAll(expectedTodayTask);
        expectedAllTask.addAll(expectedAnyTimeTask);
        expectedAllTask.addAll(expectedFutureTask);

        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasks();
        assertEquals(expectedAllTask.size(), taskList.size());

        assertTrue(expectedAllTask.containsAll(taskList));
    }

    public void testShouldReturnExpiredTasks() {
        resetDatabase();
        List<Task> expectedExpiredTask = createExpiredTasks();
        createTodayTasks();
        createAnyTimeTasks();
        createFutureTasks();
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasksByType(Task.TypeTask.Expired);

        assertEquals(expectedExpiredTask.size(), taskList.size());

        assertTrue(expectedExpiredTask.containsAll(taskList));
    }

    public void testShouldReturnTodayTasks() {
        resetDatabase();
        List<Task> expectedTodayTask = createTodayTasks();
        createExpiredTasks();
        createAnyTimeTasks();
        createFutureTasks();
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasksByType(Task.TypeTask.Today);

        assertEquals(expectedTodayTask.size(), taskList.size());

        assertTrue(expectedTodayTask.containsAll(taskList));
    }

    public void testShouldReturnFutureTasks() {
        resetDatabase();
        createTodayTasks();
        createExpiredTasks();
        createAnyTimeTasks();
        List<Task> expectedFutureTask = createFutureTasks();
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasksByType(Task.TypeTask.Future);

        assertEquals(expectedFutureTask.size(), taskList.size());

        assertTrue(expectedFutureTask.containsAll(taskList));
    }

    public void testShouldReturnAnyTimeTasks() {
        resetDatabase();
        createTodayTasks();
        createExpiredTasks();
        List<Task> expectedAnyTimeTask = createAnyTimeTasks();
        createFutureTasks();
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTasksByType(Task.TypeTask.AnyTime);

        assertEquals(expectedAnyTimeTask.size(), taskList.size());

        assertTrue(expectedAnyTimeTask.containsAll(taskList));
    }

    public void testShouldReturnFullSummary() {
        resetDatabase();
        List<Task> expectedTodayTasks = createTodayTasks();
        List<Task> expectedExpiredTasks = createExpiredTasks();
        List<Task> expectedAnyTimeTasks = createAnyTimeTasks();
        List<Task> expectedFutureTasks = createFutureTasks();

        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> taskList = useCaseTask.getTaskSummary();

        assertEquals(4, taskList.size());

        for(Task task : taskList) {
            if (task.typeTask == Task.TypeTask.Expired) {
                assertEquals(expectedExpiredTasks.get(expectedExpiredTasks.size()-1),task);
            }
            if (task.typeTask == Task.TypeTask.Today) {
                assertEquals(expectedTodayTasks.get(0), task);
            }
            if (task.typeTask == Task.TypeTask.Future) {
                assertEquals(expectedFutureTasks.get(0),task);
            }
            if (task.typeTask == Task.TypeTask.AnyTime) {
                assertEquals(expectedAnyTimeTasks.get(expectedAnyTimeTasks.size()-1),task);
            }
        }
    }

}
