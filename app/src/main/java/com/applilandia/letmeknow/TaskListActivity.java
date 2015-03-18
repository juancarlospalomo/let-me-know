package com.applilandia.letmeknow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.applilandia.letmeknow.fragments.TaskFragment;
import com.applilandia.letmeknow.fragments.TaskListFragment;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.views.FloatingActionButton;


public class TaskListActivity extends ActionBarActivity {

    private final static String LOG_TAG = TaskListActivity.class.getSimpleName();

    public final static String EXTRA_TYPE_TASK = "TypeTask";
    //Keep track on the toolbar menu is showed or not
    private boolean mShowedToolbarMenu = false;
    private Task.TypeTask mTypeTask = Task.TypeTask.All;
    //Toolbar
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        mToolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadExtras();
        createFloatingActionButton();
        createTaskSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mShowedToolbarMenu) {
            mToolbar.setNavigationIcon(R.drawable.ic_action_down);
        } else {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        }
        return mShowedToolbarMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mShowedToolbarMenu) {
                    int backStackEntry = getFragmentNumber();
                    if (backStackEntry > 0) {
                        popFragmentBackStack();
                        backStackEntry--;
                        refreshUIState(backStackEntry);
                    } else {
                        finish();
                    }
                } else {
                    mShowedToolbarMenu = false;
                    invalidateOptionsMenu();
                    refreshUIState(0);
                    TaskListFragment taskListFragment = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                    if (taskListFragment != null) {
                        taskListFragment.deactivateToolbarActions();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        int backStackEntry = getFragmentNumber();
        if (backStackEntry > 0) {
            popFragmentBackStack();
            backStackEntry--;
            refreshUIState(backStackEntry);
        } else {
            finish();
        }
    }

    /**
     * Set the Toolbar in the action bar in the correct state
     */
    private void refreshUIState(int count) {
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_add_task);
        Spinner taskSpinner = (Spinner) findViewById(R.id.spinnerTypeTasks);
        if (mShowedToolbarMenu) {
            //Actions are displayed in toolbar, then we hide the floating action button
            //and spinner
            floatingActionButton.setVisibility(View.GONE);
            taskSpinner.setVisibility(View.GONE);
        } else {
            if (count == 0) {
                //We are in the main fragment, then the Floating Action Button
                //and spinner must be shown
                floatingActionButton.setVisibility(View.VISIBLE);
                taskSpinner.setVisibility(View.VISIBLE);
            } else {
                //We are not in the main fragment, so we hide Floating Action Button
                //and spinner
                floatingActionButton.setVisibility(View.GONE);
                taskSpinner.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Load extras in class private members
     */
    private void loadExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTypeTask = Task.TypeTask.map(bundle.getInt(EXTRA_TYPE_TASK));
        }
    }

    private void createFloatingActionButton() {
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_add_task);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this, TaskActivity.class);
                intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.New.getValue());
                startActivity(intent);
            }
        });
    }

    private void createTaskSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerTypeTasks);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.type_task_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(mTypeTask.getValue());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTypeTask = Task.TypeTask.map(position);
                createTaskListFragment();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Create Fragment to show the task list
     */
    private void createTaskListFragment() {
        TaskListFragment taskListFragment = new TaskListFragment();
        taskListFragment.setTypeTask(mTypeTask);
        taskListFragment.setOnTaskListFragmentListener(new TaskListFragment.OnTaskListFragmentListener() {
            @Override
            public void onTaskSelected(int id) {
                createTaskFragment(id);
            }

            @Override
            public void onTaskLongPressed() {
                mShowedToolbarMenu = true;
                invalidateOptionsMenu();
                refreshUIState(0);
            }
        });
        taskListFragment.setTypeTask(mTypeTask);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.content_frame, taskListFragment)
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
        refreshUIState(getSupportFragmentManager().getBackStackEntryCount() + 1);
    }

    /**
     * Simulate pop fragment from BackStack.
     */
    private void popFragmentBackStack() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment instanceof TaskFragment) {
            createTaskListFragment();
        }
    }

    /**
     * Return the current fragment number in the stack
     *
     * @return number depicts the fragment number
     */
    private int getFragmentNumber() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (currentFragment instanceof TaskFragment) {
            return 1;
        }
        return 0;
    }

}
