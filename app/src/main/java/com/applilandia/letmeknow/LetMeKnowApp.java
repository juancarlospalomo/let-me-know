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
    private static Boolean mSentNotifications = false; //State if there are any notification sent

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        UseCaseNotification useCaseNotification = new UseCaseNotification(mContext);
        mSentNotifications = useCaseNotification.existInSentStatus();
    }

    /**
     * Return if there are any notification sent
     * @return
     */
    public static boolean anySentNotification() {
        if (mSentNotifications == null) {

            UseCaseNotification useCaseNotification = new UseCaseNotification(mContext);
            mSentNotifications = useCaseNotification.existInSentStatus();
        }
        return mSentNotifications.booleanValue();
    }

    /**
     * Reset member variables
     */
    public static void clearSentNotification() {
        mSentNotifications = false;
    }
}
