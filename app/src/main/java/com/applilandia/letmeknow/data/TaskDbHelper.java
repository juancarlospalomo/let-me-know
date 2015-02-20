package com.applilandia.letmeknow.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.applilandia.letmeknow.data.TaskContract.TaskEntry;
import com.applilandia.letmeknow.data.TaskContract.NotificationEntry;
import com.applilandia.letmeknow.data.TaskContract.HistoryEntry;

/**
 * Created by JuanCarlos on 18/02/2015.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    //Scheme version of database. Each time the scheme is changed
    //the number has to be increased
    private static final int DATABASE_VERSION = 1;

    //Database file name
    public static final String DATABASE_NAME = "letmeknow.db";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * SQL to build the Task table
     * @return SQL
     */
    private static final String getSQLTaskTable() {
        return "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskEntry.COLUMN_TASK_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
                TaskEntry.COLUMN_TARGET_DATE_TIME + " DATE); ";

    }

    /**
     * SQL to build the Notification table
     * @return SQL
     */
    private static final String getSQLNotificationTable() {
        return "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NotificationEntry.COLUMN_TASK_ID + " INTEGER, " +
                NotificationEntry.COLUMN_DATE_TIME + " DATE, " +
                NotificationEntry.COLUMN_TYPE + " INTEGER, " +
                NotificationEntry.COLUMN_STATUS + " INTEGER, " +
                " FOREIGN KEY (" + NotificationEntry.COLUMN_TASK_ID + ") REFERENCES " +
                TaskEntry.TABLE_NAME + " (" + TaskEntry._ID + "));";
    }

    /**
     * SQL to build the History table
     * @return SQL
     */
    private static final String getSQLHistoryTable() {
        return "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HistoryEntry.COLUMN_TASK_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
                HistoryEntry.COLUMN_TARGET_DATE_TIME + " DATE, " +
                HistoryEntry.COLUMN_COMPLETED_DATE_TIME + " DATE); ";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getSQLTaskTable());
        db.execSQL(getSQLNotificationTable());
        db.execSQL(getSQLHistoryTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
