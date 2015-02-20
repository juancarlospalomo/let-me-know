package com.applilandia.letmeknow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by JuanCarlos on 19/02/2015.
 */
public class UnitOfWork {

    //State if the UnitOfWork has been started (database opened)
    //if it doesn't, no operation can be done againts the database
    private boolean mWorkStarted = false;
    private Context mContext;
    protected SQLiteDatabase mDatabase;

    public UnitOfWork(Context context) {
        mContext = context;
        mDatabase = new TaskDbHelper(mContext).getWritableDatabase();
    }

    public void init() {
        mDatabase.beginTransaction();
        mWorkStarted = true;
    }

    public void commit() {
        if (mDatabase.inTransaction()) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }
        mDatabase.close();
        mWorkStarted = false;
    }

    public void rollback() {
        if (mDatabase.inTransaction()) {
            mDatabase.endTransaction();
        }
        mDatabase.close();
        mWorkStarted = false;
    }

    public long add(String table, String nullColumnHack, ContentValues values) {
        return mDatabase.insert(table, nullColumnHack, values);
    }

    public int update(String table, ContentValues values, String where, String[] args) {
        return mDatabase.update(table, values, where, args);
    }

    public int delete(String table, String where, String[] args) {
        return mDatabase.delete(table, where, args);
    }

}
