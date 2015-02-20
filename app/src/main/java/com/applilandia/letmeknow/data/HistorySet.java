package com.applilandia.letmeknow.data;

import android.content.ContentValues;
import android.content.Context;

import com.applilandia.letmeknow.cross.Dates;
import com.applilandia.letmeknow.models.History;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class HistorySet extends DbSet<History> {

    public HistorySet(Context context) {
        super(context);
    }

    public HistorySet(Context context, UnitOfWork unitOfWork) {
        super(context, unitOfWork);
    }

    @Override
    public long create(History history) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.HistoryEntry.COLUMN_TASK_NAME, history.name);
        values.put(TaskContract.HistoryEntry.COLUMN_TARGET_DATE_TIME, Dates.castToDatabaseFormat(history.targetDate));
        values.put(TaskContract.HistoryEntry.COLUMN_COMPLETED_DATE_TIME, Dates.castToDatabaseFormat(history.completedDate));
        long rowId = mUnitOfWork.add(TaskContract.HistoryEntry.TABLE_NAME, null, values);
        if (rowId > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.HistoryEntry.CONTENT_URI, null);
        }
        return rowId;
    }

    @Override
    public int update(History history) {
        ContentValues values = new ContentValues();
        values.put(TaskContract.HistoryEntry.COLUMN_TASK_NAME, history.name);
        values.put(TaskContract.HistoryEntry.COLUMN_TARGET_DATE_TIME, Dates.castToDatabaseFormat(history.targetDate));
        values.put(TaskContract.HistoryEntry.COLUMN_COMPLETED_DATE_TIME, Dates.castToDatabaseFormat(history.completedDate));

        String where = TaskContract.HistoryEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(history._id)};

        int rowsAffected = mUnitOfWork.update(TaskContract.HistoryEntry.TABLE_NAME, values, where, args);
        if (rowsAffected > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.HistoryEntry.CONTENT_URI, null);
        }
        return rowsAffected;
    }

    @Override
    public boolean delete(History history) {
        String where = TaskContract.HistoryEntry._ID + "=?";
        String[] args = new String[]{String.valueOf(history._id)};
        int rowsAffected = mUnitOfWork.delete(TaskContract.HistoryEntry.TABLE_NAME, where, args);
        if (rowsAffected > 0) {
            mContext.getContentResolver().notifyChange(TaskContract.HistoryEntry.CONTENT_URI, null);
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
