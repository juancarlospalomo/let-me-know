package com.applilandia.letmeknow.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;

import com.applilandia.letmeknow.NotificationListActivity;
import com.applilandia.letmeknow.cross.Message;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseNotification;
import com.applilandia.letmeknow.usecases.UseCaseTask;

/**
 * Created by JuanCarlos on 18/03/2015.
 */
public class NotificationService extends IntentService {

    private final static String LOG_TAG = NotificationListActivity.class.getSimpleName();

    //Service name for the worker thread.
    private final static String SERVICE_NAME = NotificationService.class.getSimpleName();

    public final static String INTENT_ACTION = "action";
    public final static String EXTRA_TASK_ID = "task_id";

    public final static int ACTION_NONE = 0;
    public final static int ACTION_END_TASK = 2;
    public final static int ACTION_DISMISS = 3;
    public final static int ACTION_SHARE = 4;

    private int mAction = ACTION_NONE;
    private int mTaskId = 0;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NotificationService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        loadIntent(intent);

        switch (mAction) {

            case ACTION_SHARE:
                shareTask(mTaskId);
                cancelNotification();
                break;

            case ACTION_END_TASK:
                endTask(mTaskId);
                cancelNotification();
                break;

            case ACTION_DISMISS:
                dismissNotification(mTaskId);
                cancelNotification();
                break;
        }
    }

    /**
     * Load extras from Intent into member variables
     */
    private void loadIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAction = extras.getInt(INTENT_ACTION);
            mTaskId = extras.getInt(EXTRA_TASK_ID);
        }
    }

    /**
     * Set a task as completed
     * @param id
     */
    private void endTask(int id) {
        UseCaseTask useCaseTask = new UseCaseTask(this);
        Task task = useCaseTask.getTask(id);
        if (task != null) {
            useCaseTask.setTaskAsCompleted(task);
        }
    }

    /**
     * Share a task by means an intent that matches ACTION_SEND
     * @param id task id
     */
    private void shareTask(int id) {
        UseCaseTask useCaseTask = new UseCaseTask(this);
        Task task = useCaseTask.getTask(id);
        if (task != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            Message message = new Message(this);
            intent.putExtra(Intent.EXTRA_TEXT, message.getFormattedTaskMessage(task));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * Delete the notification
     * @param id
     */
    private void dismissNotification(int id) {
        NotificationSet notificationSet = new NotificationSet(this);
        notificationSet.deleteSentNotification(id);
    }

    /**
     * Remove a notification from the notification bar
     */
    private void cancelNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(UseCaseNotification.LET_ME_KNOW_NOTIFICATION_ID);
    }
}
