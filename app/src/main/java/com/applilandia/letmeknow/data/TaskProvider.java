package com.applilandia.letmeknow.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by JuanCarlos on 18/02/2015.
 */
public class TaskProvider extends ContentProvider {

    private final static String LOG_TAG = TaskProvider.class.getSimpleName();

    //Uri codes when Uri match
    private static final int TASK = 100;
    private static final int TASK_WITH_NOTIFICATIONS = 101;
    private static final int TASK_NOTIFICATION_STATUS = 102;
    private static final int NOTIFICATION = 200;
    private static final int NOTIFICATIONS = 201;
    private static final int HISTORY = 300;

    private TaskDbHelper mDbHelper;

    //Init UriMatcher in static block when class is initialized
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASK, TASK);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASK + "/#", TASK_WITH_NOTIFICATIONS);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASK + "/" + TaskContract.PATH_NOTIFICATION_STATUS + "/#", TASK_NOTIFICATION_STATUS);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_NOTIFICATION + "/#", NOTIFICATION);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_NOTIFICATION, NOTIFICATIONS);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_HISTORY, HISTORY);
    }

    /**
     * Return a task with its notifications
     *
     * @param id local task identifier
     * @return Cursor
     */
    private Cursor getTasksWithNotifications(int id) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //Build join
        sqLiteQueryBuilder.setTables(TaskContract.TaskEntry.TABLE_NAME + " LEFT JOIN " +
                TaskContract.NotificationEntry.TABLE_NAME + " ON " + TaskContract.TaskEntry.TABLE_NAME + "." +
                TaskContract.TaskEntry._ID + "=" + TaskContract.NotificationEntry.TABLE_NAME + "." +
                TaskContract.NotificationEntry.COLUMN_TASK_ID);
        //Columns
        String[] projection = new String[]{TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + " AS " + TaskContract.TaskEntry.ALIAS_ID,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry._ID,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_DATE_TIME,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_STATUS,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_TYPE};
        //Task identifier value condition if id != 0
        //Filter
        String selection = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(id)};

        return sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), projection, selection, args, null, null, null);
    }

    /**
     * Return a list of task with a specific notification status
     *
     * @param orderBy
     * @return
     */
    private Cursor getTasksNotificationStatus(int status, String orderBy) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TaskContract.TaskEntry.TABLE_NAME + " INNER JOIN " +
                TaskContract.NotificationEntry.TABLE_NAME + " ON " +
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=" +
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_TASK_ID);

        String[] fields = new String[]{TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME,
                "Count(" + TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry._ID + ") AS " + TaskContract.TaskEntry.ALIAS_NOTIFICATION_COUNT};

        String selection = TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_STATUS + "=?";
        String[] args = new String[]{String.valueOf(status)};

        return sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(),
                fields, selection, args, null, null, orderBy);
    }

    /**
     * Return the whole task list and their notifications count number
     *
     * @param selection
     * @param args
     * @param orderBy
     * @return
     */
    private Cursor getTasks(String selection, String[] args, String orderBy) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TaskContract.TaskEntry.TABLE_NAME + " LEFT JOIN " +
                TaskContract.NotificationEntry.TABLE_NAME + " ON " + TaskContract.TaskEntry.TABLE_NAME + "." +
                TaskContract.TaskEntry._ID + "=" + TaskContract.NotificationEntry.TABLE_NAME + "." +
                TaskContract.NotificationEntry.COLUMN_TASK_ID);

        String[] fields = new String[]{TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.COLUMN_TASK_NAME, TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME,
                "Count(" + TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry._ID + ") AS " + TaskContract.TaskEntry.ALIAS_NOTIFICATION_COUNT};

        String groupBy = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "," +
                TaskContract.TaskEntry.COLUMN_TASK_NAME + "," + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME;

        try {
            return sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), fields, selection, args, groupBy, null, orderBy);
        } catch (SQLiteException exception) {
            Log.e(LOG_TAG, exception.getMessage());
            return null;
        }
    }

    /**
     * load a notification by its identifier
     *
     * @param id notification identifier
     * @return cursor
     */
    private Cursor getNotification(int id) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TaskContract.NotificationEntry.TABLE_NAME);
        String selection = TaskContract.NotificationEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(id)};

        return sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), null, selection, args, null, null, null);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new TaskDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        int code = mUriMatcher.match(uri);

        switch (code) {
            case TASK:
                cursor = getTasks(selection, selectionArgs, sortOrder);
                break;

            case TASK_WITH_NOTIFICATIONS:
                int taskId = TaskContract.TaskEntry.getUriTaskId(uri);
                cursor = getTasksWithNotifications(taskId);
                break;

            case TASK_NOTIFICATION_STATUS:
                int status = TaskContract.TaskEntry.getUriTaskNotificationStatus(uri);
                cursor = getTasksNotificationStatus(status,
                        TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME);
                break;

            case NOTIFICATION:
                int id = TaskContract.NotificationEntry.getUriNotificationId(uri);
                cursor = getNotification(id);
                break;

            case NOTIFICATIONS:
                cursor = mDbHelper.getReadableDatabase().query(TaskContract.NotificationEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case HISTORY:
                cursor = mDbHelper.getReadableDatabase().query(TaskContract.HistoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        //What kind of URI is it?
        int code = mUriMatcher.match(uri);
        switch (code) {
            case TASK:
                return TaskContract.TaskEntry.CONTENT_TYPE;
            case TASK_WITH_NOTIFICATIONS:
                return TaskContract.TaskEntry.CONTENT_TYPE;
            case TASK_NOTIFICATION_STATUS:
                return TaskContract.TaskEntry.CONTENT_TYPE;
            case NOTIFICATION:
                return TaskContract.NotificationEntry.CONTENT_TYPE;
            case NOTIFICATIONS:
                return TaskContract.NotificationEntry.CONTENT_TYPE;
            case HISTORY:
                return TaskContract.HistoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
