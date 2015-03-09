package com.applilandia.letmeknow.usecases;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.cross.Settings;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskSet;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;

import java.text.ParseException;
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
     * @return taskId if it was created successfully. In other way, will be 0
     */
    public int createTask(Task task) {
        boolean hasNotifications = task.hasNotifications();

        //Check if the Date has time.  If it hasn't, notification cannot be created
        if ((task.targetDateTime != null) && (!task.targetDateTime.isTimeNull())) {
            //We check if we have to include the creation of notification
            if (!task.hasNotifications()) {
                //The task hasn't notifications included, so we must check the setting
                //to know if we must create a default notification
                if (Settings.getCreateDefaultNotificationValue(mContext)) {
                    //Create the default notification.  It is the 5 minutes before
                    Notification notification = new Notification();
                    notification.type = Notification.TypeNotification.FiveMinutesBefore;
                    notification.status = Notification.TypeStatus.Pending;
                    notification.dateTime = new LocalDate(task.targetDateTime).addMinutes(-5).getDateTime();
                    task.addNotification(notification);
                }
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
                null, null, null,
                "CASE WHEN " + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " IS NULL THEN 1 ELSE 0 END, " +
                        TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " ASC, " +
                        TaskContract.TaskEntry.COLUMN_TASK_NAME + " ASC");
        if ((cursor != null) && (cursor.moveToFirst())) {
            task = new Task();
            task._id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.ALIAS_ID));
            task.name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
            try {
                task.targetDateTime = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME)));
                while (!cursor.isAfterLast()) {
                    int notificationId = cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry._ID));
                    if (notificationId > 0) {
                        Notification notification = new Notification();
                        notification._id = notificationId;
                        notification.dateTime = new LocalDate(cursor.getString(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_DATE_TIME))).getDateTime();
                        notification.taskId = id;
                        notification.status = Notification.TypeStatus.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_STATUS)));
                        notification.type = Notification.TypeNotification.map(cursor.getInt(cursor.getColumnIndex(TaskContract.NotificationEntry.COLUMN_TYPE)));
                        task.addNotification(notification);
                    }
                    cursor.moveToNext();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return task;
    }

    /**
     * Conver cursor with tasks to List
     *
     * @param cursor task cursor
     * @return List of tasks
     */
    private List<Task> toList(Cursor cursor) {
        List<Task> taskList = null;
        if ((cursor != null) && (cursor.moveToFirst())) {
            taskList = new ArrayList<Task>();
            while (!cursor.isAfterLast()) {
                Task task = new Task();
                task._id = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry._ID));
                task.name = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK_NAME));
                try {
                    String date = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME));
                    if (!TextUtils.isEmpty(date)) {
                        task.targetDateTime = new LocalDate(date);
                    }
                    task.typeTask = getTypeTask(task.targetDateTime);
                    task.setCurrentNotificationsCount(cursor.getInt(cursor.getColumnIndex(TaskContract.TaskEntry.ALIAS_NOTIFICATION_COUNT)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                taskList.add(task);
                cursor.moveToNext();
            }
        }
        return taskList;
    }

    /**
     * Recover all tasks stored in the repository
     *
     * @return List of tasks
     */
    public List<Task> getTasks() {
        List<Task> result = null;
        String orderBy = "CASE WHEN " + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " IS NULL " +
                "THEN 1 ELSE 0 END DESC, " + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " DESC," +
                TaskContract.TaskEntry.COLUMN_TASK_NAME;

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, null, null, orderBy);
        result = toList(cursor);
        cursor.close();
        return result;
    }

    /**
     * Get the the task that expired the last
     *
     * @return Task
     */
    private Task getLastExpiredTask() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<? AND " +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<>?";
        LocalDate currentDate = new LocalDate();
        String[] args = new String[]{currentDate.toString(), currentDate.getDate()};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " DESC LIMIT 1";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, selection, args, orderBy);
        List<Task> result = toList(cursor);
        cursor.close();
        if ((result != null) && (result.size() == 1))
            return result.get(0);
        else
            return null;
    }

    /**
     * Get the the next task for today
     *
     * @return Task
     */
    private Task getNextTodayTask() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " =? OR (" +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + ">? AND " +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<?)";
        LocalDate currentDate = new LocalDate();
        String beginningDateTime = currentDate.toString();
        currentDate.setTime(23, 59);
        String endingDateTime = currentDate.toString();
        String[] args = new String[]{currentDate.getDate(), beginningDateTime, endingDateTime};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " ASC LIMIT 1";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, selection, args, orderBy);
        List<Task> result = toList(cursor);
        cursor.close();
        if ((result != null) && (result.size() == 1))
            return result.get(0);
        else
            return null;
    }

    /**
     * Get the the next task for the future (from tomorrow onwards)
     *
     * @return Task
     */
    private Task getNextFutureTask() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " >=?";
        LocalDate currentDate = new LocalDate().addDays(1);
        String[] args = new String[]{currentDate.getDate()};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " ASC LIMIT 1";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, selection, args, orderBy);
        List<Task> result = toList(cursor);
        cursor.close();
        if ((result != null) && (result.size() == 1))
            return result.get(0);
        else
            return null;
    }

    /**
     * Return the last task entered without date
     *
     * @return Task
     */
    private Task getLastEnteredAnyTimeTask() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " IS NULL";
        String orderBy = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + " DESC LIMIT 1";
        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                null, selection, null, orderBy);
        List<Task> result = toList(cursor);
        cursor.close();
        if ((result != null) && (result.size() == 1))
            return result.get(0);
        else
            return null;
    }

    /**
     * Get the summary for the existing Tasks
     *
     * @return List of tasks
     */
    public List<Task> getTaskSummary() {
        List<Task> result = new ArrayList<Task>();
        Task task = getLastExpiredTask();
        result.add(task);
        task = getNextTodayTask();
        result.add(task);
        task = getNextFutureTask();
        result.add(task);
        task = getLastEnteredAnyTimeTask();
        result.add(task);

        return result;
    }

    /**
     * Get the current expired tasks
     *
     * @return List with expired tasks
     */
    private List<Task> getExpiredTasks() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<? AND " +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<>?";
        LocalDate currentDate = new LocalDate();
        String[] args = new String[]{currentDate.toString(), currentDate.getDate()};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " DESC, " +
                TaskContract.TaskEntry.COLUMN_TASK_NAME + " ASC";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI, null, selection,
                args, orderBy);
        List<Task> taskList = toList(cursor);
        cursor.close();
        return taskList;
    }

    /**
     * Get the current tasks for today
     *
     * @return List of tasks for today
     */
    private List<Task> getTodayTasks() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " =? OR (" +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + ">? AND " +
                TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + "<?)";
        LocalDate currentDate = new LocalDate();
        String beginningDateTime = currentDate.toString();
        currentDate.setTime(23, 59);
        String endingDateTime = currentDate.toString();
        String[] args = new String[]{currentDate.getDate(), beginningDateTime, endingDateTime};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " ASC, " +
                TaskContract.TaskEntry.COLUMN_TASK_NAME + " ASC";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI, null, selection,
                args, orderBy);
        List<Task> taskList = toList(cursor);
        cursor.close();
        return taskList;
    }

    /**
     * Get the task from tomorrow onwards
     *
     * @return List of tasks
     */
    private List<Task> getFutureTasks() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " >=?";
        LocalDate currentDate = new LocalDate().addDays(1);
        String[] args = new String[]{currentDate.getDate()};
        String orderBy = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " ASC, " +
                TaskContract.TaskEntry.COLUMN_TASK_NAME + " ASC";

        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI, null, selection,
                args, orderBy);
        List<Task> taskList = toList(cursor);
        cursor.close();
        return taskList;
    }

    /**
     * Get the tasks without date
     *
     * @return List of tasks
     */
    private List<Task> getAnyTimeTasks() {
        String selection = TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME + " IS NULL";
        String orderBy = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + " DESC";
        Cursor cursor = mContext.getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI, null, selection,
                null, orderBy);
        List<Task> taskList = toList(cursor);
        cursor.close();
        return taskList;
    }

    /**
     * Return a list with the tasks filtered by type
     *
     * @param typeTask type of task to filter
     * @return List with tasks
     */
    public List<Task> getTasksByType(Task.TypeTask typeTask) {
        if (typeTask == Task.TypeTask.Expired) {
            return getExpiredTasks();
        }
        if (typeTask == Task.TypeTask.Today) {
            return getTodayTasks();
        }
        if (typeTask == Task.TypeTask.Future) {
            return getFutureTasks();
        }
        if (typeTask == Task.TypeTask.AnyTime) {
            return getAnyTimeTasks();
        }
        return null;
    }

    /**
     * Figure out the task type based on the date
     *
     * @param date task date
     * @return TypeTask
     */
    private Task.TypeTask getTypeTask(LocalDate date) {
        Task.TypeTask typeTask = Task.TypeTask.AnyTime;
        if ((date != null) && (date.toString() != null)) {
            LocalDate currentDate = new LocalDate();
            if (date.isTimeNull()) {
                //To make the comparison homogeneous,
                //we must to leave both dates, current and date passed as parameter,
                //with the same parts
                currentDate.removeTime();
            }
            //now, we can make the comparison surely
            int result = date.compareTo(currentDate);
            if (result < 0) {
                //date passed as parameter is less than the current one
                typeTask = Task.TypeTask.Expired;
            } else {
                if (result == 0) {
                    //Both are equals
                    typeTask = Task.TypeTask.Today;
                } else {
                    if (result > 0) {
                        //date passed as parameter is greater than the current one
                        if (date.isToday()) {
                            //if it is greater but the date is for today
                            typeTask = Task.TypeTask.Today;
                        } else {
                            //date greater than today
                            typeTask = Task.TypeTask.Future;
                        }
                    }
                }
            }
        }
        return typeTask;
    }

}
