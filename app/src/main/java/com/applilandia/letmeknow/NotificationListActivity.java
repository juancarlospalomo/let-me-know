package com.applilandia.letmeknow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.fragments.NotificationListFragment;
import com.applilandia.letmeknow.fragments.TaskFragment;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseNotification;


public class NotificationListActivity extends ActionBarActivity {

    private final static String LOG_TAG = NotificationListActivity.class.getSimpleName();

    private final static int FRAGMENT_NOTIFICATION_LIST = 0;
    private final static int FRAGMENT_TASK = 1;


    public final static String INTENT_ACTION = "action";
    public final static String EXTRA_TASK_ID = "task_id";

    public final static int ACTION_NONE = 0;
    public final static int ACTION_VIEW = 1;

    private int mAction = ACTION_NONE;
    private int mTaskId = 0;
    private int mCurrentFragment = FRAGMENT_NOTIFICATION_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        //Load parameters
        loadIntent();
        //Configure Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        switch (mAction) {
            case ACTION_VIEW:
                createTaskFragment(mTaskId);
                cancelNotification();
                break;

            default:
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
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    /**
     * Run the process to finish activity
     */
    private void goBack() {
        if (mAction != ACTION_VIEW && mCurrentFragment == FRAGMENT_TASK) {
            popFragmentBackStack();
        } else {
            //If we are in the first fragment, then close the activity
            NotificationSet notificationSet = new NotificationSet(this);
            notificationSet.deleteNotifications(Notification.TypeStatus.Sent);
            finish();
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

            @Override
            public void onItemRemoved(int count) {
                if (count == 0) {
                    //No more elements, so we go back
                    goBack();
                }
            }

            @Override
            public void onListEmpty() {
                //Notification list is empty, so we go back
                goBack();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        //Save the current visible fragment
        mCurrentFragment = FRAGMENT_NOTIFICATION_LIST;
    }

    /**
     * Create a fragment to display the task stated
     *
     * @param id task identifier
     */
    private void createTaskFragment(int id) {
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.setWorkMode(TaskActivity.TypeWorkMode.View, id, "", "");
        taskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved(Task task) {
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
        //Remove the notification from database
        NotificationSet notificationSet = new NotificationSet(this);
        notificationSet.deleteSentNotification(id);
        //Save the current visible fragment
        mCurrentFragment = FRAGMENT_TASK;
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
     * Remove a notification from the notification bar
     */
    private void cancelNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(UseCaseNotification.LET_ME_KNOW_NOTIFICATION_ID);
        //Remove daily notification
        notificationManager.cancel(UseCaseNotification.LET_ME_KNOW_DAILY_NOTIFICATION);
    }

}
