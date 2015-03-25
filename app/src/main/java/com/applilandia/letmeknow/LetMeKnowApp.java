package com.applilandia.letmeknow;

import android.app.Application;
import android.content.Context;

import com.applilandia.letmeknow.usecases.UseCaseNotification;

/**
 * Created by JuanCarlos on 24/03/2015.
 */
public class LetMeKnowApp extends Application {

    private final static String LOG_TAG = LetMeKnowApp.class.getSimpleName();

    private static Context mContext; //Context static reference
    private static Boolean mSentNotifications = null; //State if there are any notification sent
    //It will have the Android default uncaught exception handler
    private Thread.UncaughtExceptionHandler mDefaultException;

    private Thread.UncaughtExceptionHandler mUncaughtHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //For now, the custom exception handler is a fake
            //that only throw the default Android exception handler
            mDefaultException.uncaughtException(thread, ex);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //We set the default exception Handler
        mDefaultException = Thread.getDefaultUncaughtExceptionHandler();
        //Set the custom exception handler
        Thread.setDefaultUncaughtExceptionHandler(mUncaughtHandler);
    }

    /**
     * Return if there are any notification sent
     *
     * @return
     */
    public static boolean anySentNotification() {
        UseCaseNotification useCaseNotification = new UseCaseNotification(mContext);
        return useCaseNotification.existInSentStatus();
    }

    /**
     * Reset member variables
     */
    public static void clearSentNotification() {
        mSentNotifications = null;
    }
}
