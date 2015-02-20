package com.applilandia.letmeknow;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.data.TaskDbHelper;

import java.util.Date;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class testDb extends AndroidTestCase {

    public void testShouldCreateTask() throws Throwable {
        SQLiteDatabase db = new TaskDbHelper(mContext).getWritableDatabase();
        ContentValues values = createTaskMandatoryValues();
        long rowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        assertTrue(rowId > 0);

        values = createTaskAllValues();
        rowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        assertTrue(rowId > 0);
        db.close();
    }

    static ContentValues createTaskMandatoryValues() {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, "Task with mandatory name");
        return values;
    }

    static ContentValues createTaskAllValues() {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, "Task 2");
        values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, Dates.castToDatabaseFormat(new Date()));
        return values;
    }

}
