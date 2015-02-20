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
        if (!mWorkStarted) {
            throw new UnsupportedOperationException("not available without a started work");
        } else {
            if (mDatabase.inTransaction()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
            mDatabase.close();
            mWorkStarted = false;
        }
    }

    public void rollback() {
        if (!mWorkStarted) {
            throw new UnsupportedOperationException("not available without a started work");
        } else {
            if (mDatabase.inTransaction()) {
                mDatabase.endTransaction();
            }
            mDatabase.close();
            mWorkStarted = false;
        }
    }

    public long add(String table, String nullColumnHack, ContentValues values) {
        long rowId = mDatabase.insert(table, nullColumnHack, values);
        if (!mWorkStarted) {
            //It is not inside a transaction, we already close the database.
            //If not, it must be confirmed or roll backed manually
            mDatabase.close();
        }
        return rowId;
    }

    public int update(String table, ContentValues values, String where, String[] args) {
        int rowsAffected = mDatabase.update(table, values, where, args);
        if (!mWorkStarted) {
            //It is not inside a transaction, we already close the database.
            //If not, it must be confirmed or roll backed manually
            mDatabase.close();
        }
        return  rowsAffected;
    }

    public int delete(String table, String where, String[] args) {
        int rowsAffected = mDatabase.delete(table, where, args);
        if (!mWorkStarted) {
            //It is not inside a transaction, we already close the database.
            //If not, it must be confirmed or roll backed manually
            mDatabase.close();
        }
        return rowsAffected;
    }

}
