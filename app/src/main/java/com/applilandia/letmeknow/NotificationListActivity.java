package com.applilandia.letmeknow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.applilandia.letmeknow.fragments.NotificationListFragment;
import com.applilandia.letmeknow.fragments.TaskFragment;


public class NotificationListActivity extends ActionBarActivity {

    private final static String LOG_TAG = NotificationListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        initFragment();
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

}
