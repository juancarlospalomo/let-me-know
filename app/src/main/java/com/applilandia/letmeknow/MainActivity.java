package com.applilandia.letmeknow;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.applilandia.letmeknow.cross.Settings;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.fragments.HomeFragment;
import com.applilandia.letmeknow.fragments.NotificationListFragment;
import com.applilandia.letmeknow.fragments.TaskFragment;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 13/02/2015.
 */
public class MainActivity extends ActionBarActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //The first operation is init the App
        initApp();
        //Configure the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        createNavigationDrawer();
        addHomeFragment();
        addIfNotificationFragment();
    }

    /**
     * Configure the app for the first time is executed
     */
    private void initApp() {
        if (!Settings.existPreferenceFile(this)) {
            //It´s the first time the app is executed
            //Create the preference file and daily alarm
            Settings.createPreferenceFile(this);
            NotificationSet.Alarm alarm = new NotificationSet(this).new Alarm();
            alarm.create(null);
        }
    }

    /**
     * Create and init the navigation drawer
     */
    private void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter(this));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case DrawerListAdapter.SETTINGS_INDEX:
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerToggle.syncState();
    }

    /**
     * Add home fragment to Activity
     */
    private void addHomeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_frame, new HomeFragment());
        fragmentTransaction.commit();
    }

    /**
     * If there are any notification sent, add Fragment to show the notification list
     */
    private void addIfNotificationFragment() {
        if (LetMeKnowApp.anySentNotification()) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(UseCaseNotification.LET_ME_KNOW_NOTIFICATION_ID);
            NotificationListFragment fragment = new NotificationListFragment();
            fragment.setOnNotificationListListener(new NotificationListFragment.OnNotificationListListener() {
                @Override
                public void onSelectedTask(int id) {
                    addTaskFragment(id);
                }

                @Override
                public void onItemRemoved(int count) {
                    if (count == 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
            getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        UseCaseNotification useCaseNotification = new UseCaseNotification(MainActivity.this);
                        useCaseNotification.removeSent();
                    }
                }
            });
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Add a fragment to view one task
     * @param id task identifier
     */
    private void addTaskFragment(int id) {
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.setWorkMode(TaskActivity.TypeWorkMode.View, id, "", "");
        taskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved(Task task) {
                getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onClose() {
                getSupportFragmentManager().popBackStack();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.content_frame, taskFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newChange) {
        super.onConfigurationChanged(newChange);
        mDrawerToggle.onConfigurationChanged(newChange);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItem {
        public int iconResId;
        public int textResId;

        private DrawerItem(int iconResId, int textResId) {
            this.iconResId = iconResId;
            this.textResId = textResId;
        }
    }

    private class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

        public final static int SETTINGS_INDEX = 0;

        private List<DrawerItem> mDrawerListItems;

        private DrawerListAdapter(Context context) {
            super(context, R.layout.drawer_list_item);
            mDrawerListItems = new ArrayList<>();
            mDrawerListItems.add(new DrawerItem(R.drawable.ic_settings, R.string.menu_settings));
        }


        @Override
        public int getCount() {
            return mDrawerListItems.size();
        }

        @Override
        public DrawerItem getItem(int position) {
            return mDrawerListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
            switch (position) {
                case SETTINGS_INDEX:
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.imageDrawerAction);
                    imageView.setImageResource(mDrawerListItems.get(position).iconResId);
                    TextView textView = (TextView) convertView.findViewById(R.id.textViewDrawerAction);
                    textView.setText(mDrawerListItems.get(position).textResId);
                    break;
                default:
                    super.getView(position, convertView, parent);
            }
            return convertView;
        }
    }


}
