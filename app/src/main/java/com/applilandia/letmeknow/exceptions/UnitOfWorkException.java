package com.applilandia.letmeknow.exceptions;

import android.content.Context;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteDiskIOException;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 25/03/2015.
 */
public class UnitOfWorkException extends RuntimeException {

    /**
     * Constructs a new {@code UnitOfWorkException} that includes the
     * current stack trace.
     */
    public UnitOfWorkException() {
        super();
    }

    /**
     * Constructs a new {@code UnitOfWorkException} with the current
     * stack trace and the specified detail message.
     *
     * @param message the detail message for this exception.
     */
    public UnitOfWorkException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code UnitOfWorkException} with the current
     * stack trace, the specified detail message and the specified cause.
     *
     * @param message the detail message for this exception.
     * @param cause   the optional cause of this exception, may be {@code null}.
     */
    public UnitOfWorkException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code UnitOfWorkException} with the current
     * stack trace and the specified cause.
     *
     * @param cause the optional cause of this exception, may be {@code null}.
     */
    public UnitOfWorkException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }

    public UnitOfWorkException(Context context, Throwable cause) {
        this(getExceptionMessage(context, cause));
    }

    private static String getExceptionMessage(Context context, Throwable cause) {
        if (cause instanceof SQLiteDatabaseLockedException) {
            return (context.getString(R.string.unit_of_work_locked_exception));
        }
        if (cause instanceof SQLiteDiskIOException) {
            return (context.getString(R.string.unit_of_work_io_exception));
        }
        return cause.toString();
    }

}
