package com.applilandia.letmeknow.data;

import android.content.ContentValues;
import android.content.Context;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.models.Task;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class TaskSet extends DbSet<Task> {

    public TaskSet(Context context) {
        super(context);
    }

    public TaskSet(Context context, UnitOfWork unitOfWork) {
        super(context, unitOfWork);
    }

    @Override
    public long create(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, task.name);
        values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, Dates.castToDatabaseFormat(task.targetDatetime));
        long rowId = mUnitOfWork.add(TaskContract.TaskEntry.TABLE_NAME, null, values);
        if (rowId > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
        }
        return rowId;
    }

    @Override
    public int update(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TASK_NAME, task.name);
        values.put(TaskContract.TaskEntry.COLUMN_TARGET_DATE_TIME, Dates.castToDatabaseFormat(task.targetDatetime));

        String where = TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(task._id)};

        int rowsAffected = mUnitOfWork.update(TaskContract.TaskEntry.TABLE_NAME, values, where, args);
        if (rowsAffected > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
        }
        return rowsAffected;
    }

    @Override
    public boolean delete(Task task) {
        String where = TaskContract.TaskEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(task._id)};
        int rowsAffected = mUnitOfWork.delete(TaskContract.TaskEntry.TABLE_NAME, where, args);
        if (rowsAffected > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.TaskEntry.CONTENT_URI, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean find(int id) {
        return false;
    }
}
