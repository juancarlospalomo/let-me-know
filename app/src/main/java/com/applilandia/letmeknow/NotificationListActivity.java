package com.applilandia.letmeknow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.fragments.NotificationListFragment;
import com.applilandia.letmeknow.fragments.TaskFragment;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;


public class NotificationListActivity extends ActionBarActivity {

    private final static String LOG_TAG = NotificationListActivity.class.getSimpleName();

    public final static String INTENT_ACTION = "action";
    public final static String EXTRA_TASK_ID = "task_id";
    public final static String EXTRA_NOTIFICATION_ID = "notification_id";

    public final static int ACTION_NONE = 0;
    public final static int ACTION_VIEW = 1;
    public final static int ACTION_END_TASK = 2;
    public final static int ACTION_DISMISS = 3;

    private int mAction = ACTION_NONE;
    private int mTaskId = 0;
    private int mNotificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        loadIntent();

        Log.v(LOG_TAG, String.valueOf(mAction));

        switch (mAction) {
            case ACTION_VIEW:
                createTaskFragment(mTaskId);
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

            default:
                Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
                setSupportActionBar(toolbar);
                initFragment();
                break;
        }

    }

    /**
     * Load extras from Intent into member variables
     */
    private void loadIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAction = extras.getInt(INTENT_ACTION);
            mTaskId = extras.getInt(EXTRA_TASK_ID);
            mNotificationId = extras.getInt(EXTRA_NOTIFICATION_ID);
        }
    }

    /**
     * Create the first fragment
     */
    private void initFragment() {
        createNotificationsFragment();
    }

    /**
     * Create the notification list fragment
     */
    private void createNotificationsFragment() {
        NotificationListFragment fragment = new NotificationListFragment();
        fragment.setOnNotificationListListener(new NotificationListFragment.OnNotificationListListener() {
            @Override
            public void onSelectedTask(int id) {
                createTaskFragment(id);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    /**
     * Create a fragment to display the task stated
     *
     * @param id task identifier
     */
    private void createTaskFragment(int id) {
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.setWorkMode(TaskActivity.TypeWorkMode.View, id);
        taskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved() {
                popFragmentBackStack();
            }

            @Override
            public void onClose() {
                Log.v(LOG_TAG, "onClose");
            }
        });
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.content_frame, taskFragment)
                .commit();
    }

    /**
     * Simulate pop fragment from BackStack.
     */
    private void popFragmentBackStack() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment instanceof TaskFragment) {
            createNotificationsFragment();
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
        notificationManager.cancel(mNotificationId);
    }

}
