package com.applilandia.letmeknow.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by JuanCarlos on 18/02/2015.
 */
public class TaskProvider extends ContentProvider {

    //Uri codes when Uri match
    private static final int TASK = 100;
    private static final int TASK_WITH_NOTIFICATIONS = 101;
    private static final int NOTIFICATION = 200;
    private static final int HISTORY = 300;

    private TaskDbHelper mDbHelper;

    //Init UriMatcher in static block when class is initialized
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASK, TASK);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_TASK + "/#", TASK_WITH_NOTIFICATIONS);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_NOTIFICATION + "/#", NOTIFICATION);
        mUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.PATH_HISTORY, HISTORY);
    }

    /**
     * Return a task with its notifications
     *
     * @param id local task identifier
     * @return Cursor
     */
    private Cursor getTaskWithNotifications(int id) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        //Build join
        sqLiteQueryBuilder.setTables(TaskContract.TaskEntry.TABLE_NAME + " LEFT JOIN " +
                TaskContract.NotificationEntry.TABLE_NAME + " ON " + TaskContract.TaskEntry.TABLE_NAME + "." +
                TaskContract.TaskEntry._ID + "=" + TaskContract.NotificationEntry.TABLE_NAME + "." +
                TaskContract.NotificationEntry.COLUMN_TASK_ID);
        //Filter
        String selection = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=?";
        //Columns
        String[] projection = new String[]{TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + " AS " + TaskContract.TaskEntry.ALIAS_ID,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TASK_NAME,
                TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry._ID,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_DATE_TIME,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_STATUS,
                TaskContract.NotificationEntry.TABLE_NAME + "." + TaskContract.NotificationEntry.COLUMN_TYPE};
        //Task identifier value condition
        String[] args = new String[]{String.valueOf(id)};

        return sqLiteQueryBuilder.query(mDbHelper.getReadableDatabase(), projection, selection, args, null, null, null);
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
                cursor = mDbHelper.getReadableDatabase().query(TaskContract.TaskEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case TASK_WITH_NOTIFICATIONS:
                int taskId = TaskContract.TaskEntry.getUriTaskId(uri);
                cursor = getTaskWithNotifications(taskId);
                break;

            case NOTIFICATION:
                int id = TaskContract.NotificationEntry.getUriNotificationId(uri);
                cursor = getNotification(id);
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
            case NOTIFICATION:
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
