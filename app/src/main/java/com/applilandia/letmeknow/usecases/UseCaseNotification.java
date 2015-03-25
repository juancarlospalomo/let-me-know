package com.applilandia.letmeknow.usecases;

import android.content.Context;

import com.applilandia.letmeknow.LetMeKnowApp;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.models.Notification;

/**
 * Created by JuanCarlos on 24/03/2015.
 */
public class UseCaseNotification {

    private final static String LOG_TAG = UseCaseNotification.class.getSimpleName();

    private Context mContext;

    public UseCaseNotification(Context context) {
        mContext = context;
    }

    /**
     * Return if sent notifications exist
     * @return
     */
    public boolean existInSentStatus() {
        boolean result = false;

        NotificationSet notificationSet = new NotificationSet(mContext);
        result = notificationSet.getCount(Notification.TypeStatus.Sent) > 0;

        return result;
    }

    /**
     * Remove all sent notifications
     */
    public void removeSent() {
        NotificationSet notificationSet = new NotificationSet(mContext);
        notificationSet.deleteNotifications(Notification.TypeStatus.Sent);
        //clear Sent Notification App variable to state there aren't any more
        LetMeKnowApp.clearSentNotification();
    }

}
