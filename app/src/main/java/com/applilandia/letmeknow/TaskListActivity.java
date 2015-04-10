package com.applilandia.letmeknow;

import android.app.Activity;
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

    private final static int INDEX_FRAGMENT_LIST = 0;
    private final static int INDEX_FRAGMENT_TASK = 1;

    public final static String EXTRA_TYPE_TASK = "TypeTask";
    //Request Codes
    private final static int REQUEST_CODE_TASK = 1;

    //Keep track on the toolbar menu is showed or not
    private boolean mShowedToolbarMenu = false;
    private Task.TypeTask mTypeTask = Task.TypeTask.All;
    //Some task id to highlight?
    private int mSelectTaskId = -1;
    //Toolbar
    private Toolbar mToolbar;
    private Spinner mSpinnerType;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        inflateViews();
        initTaskTypeSpinner();
        //Create handlers for the views
        createViewsHandlers();
        //Load parameters
        if (savedInstanceState == null) {
            loadExtras();
            initValues();
        } else {
            restoreFragmentState();
        }
    }

    /**
     * Inflate views on activity
     */
    private void inflateViews() {
        //Inflate views
        mToolbar = (Toolbar) findViewById(R.id.taskToolBar);
        mSpinnerType = (Spinner) findViewById(R.id.spinnerTypeTasks);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab_add_task);
        //Init toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Init the task spinner with data
     */
    private void initTaskTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.type_task_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerType.setAdapter(adapter);
    }

    /**
     * Create the handlers for the views on activity
     */
    private void createViewsHandlers() {
        createFloatingActionButtonHandler();
        createTaskSpinnerHandler();
    }


    /**
     * Set initial values for the views
     */
    private void initValues() {
        mSpinnerType.setSelection(mTypeTask.getValue());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TASK) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Bundle outArgs = data.getExtras();
                    if (outArgs != null) {
                        int value = outArgs.getInt(TaskActivity.EXTRA_TASK_TYPE);
                        mSelectTaskId = outArgs.getInt(TaskActivity.EXTRA_TASK_ID);
                        mSpinnerType.setSelection(value);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
                    refreshToolbarState(false);
                    refreshUIState(INDEX_FRAGMENT_LIST);
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
     * update the status of toolbar according with the actions to be showed or hidden
     */
    private void refreshToolbarState(boolean showActions) {
        mShowedToolbarMenu = showActions;
        invalidateOptionsMenu();
    }

    /**
     * Set the Toolbar in the action bar in the correct state
     *
     * @param index index of the current fragment in the activity
     */
    private void refreshUIState(int index) {
        if (mShowedToolbarMenu) {
            //Actions are displayed in toolbar, then we hide the floating action button
            //and spinner
            mFloatingActionButton.setVisibility(View.INVISIBLE);
            mSpinnerType.setVisibility(View.GONE);
        } else {
            if (index == INDEX_FRAGMENT_LIST) {
                //We are in the main fragment, then the Floating Action Button
                //and spinner must be shown
                mFloatingActionButton.setVisibility(View.VISIBLE);
                mSpinnerType.setVisibility(View.VISIBLE);
            } else {
                //We are not in the main fragment, so we hide Floating Action Button
                //and spinner
                mFloatingActionButton.setVisibility(View.INVISIBLE);
                mSpinnerType.setVisibility(View.GONE);
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

    /**
     * Create handler for the Floating Action Button
     */
    private void createFloatingActionButtonHandler() {
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this, TaskActivity.class);
                intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.New.getValue());
                startActivityForResult(intent, REQUEST_CODE_TASK);
            }
        });
    }

    /**
     * Create the spinner for the type of task
     */
    private void createTaskSpinnerHandler() {
        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        if (mSelectTaskId != -1) taskListFragment.scrollToTask(mSelectTaskId);
        mSelectTaskId = -1; //Reset value for avoiding send the old value again
        setTaskListFragmentHandlers(taskListFragment);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.content_frame, taskListFragment)
                .commit();
        refreshUIState(INDEX_FRAGMENT_LIST);
    }

    /**
     * Set the event handlers for task list fragment
     * @param taskListFragment
     */
    private void setTaskListFragmentHandlers(TaskListFragment taskListFragment) {
        taskListFragment.setOnTaskListFragmentListener(new TaskListFragment.OnTaskListFragmentListener() {
            @Override
            public void onTaskSelected(int id) {
                createTaskFragment(id);
            }

            @Override
            public void onTaskLongPressed() {
                mShowedToolbarMenu = true;
                invalidateOptionsMenu();
                refreshUIState(INDEX_FRAGMENT_LIST);
            }

            @Override
            public void onTaskDeleted() {
                if (mShowedToolbarMenu) {
                    mShowedToolbarMenu = false;
                    invalidateOptionsMenu();
                    refreshUIState(INDEX_FRAGMENT_LIST);
                }
            }

            @Override
            public void onToolbarActionClose() {
                refreshToolbarState(false);
                refreshUIState(INDEX_FRAGMENT_LIST);
            }
        });
    }

    /**
     * Create a fragment to display the task stated
     *
     * @param id task identifier
     */
    private void createTaskFragment(int id) {
        TaskFragment taskFragment;
        taskFragment = new TaskFragment();
        taskFragment.setWorkMode(TaskActivity.TypeWorkMode.View, id, "", "");
        setTaskFragmentHandlers(taskFragment);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out)
                .replace(R.id.content_frame, taskFragment)
                .commit();
        refreshUIState(INDEX_FRAGMENT_TASK);
    }

    /**
     * Set event handlers for task fragment
     * @param taskFragment
     */
    private void setTaskFragmentHandlers(TaskFragment taskFragment) {
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
    }

    /**
     * Restore the state for current fragment
     */
    private void restoreFragmentState() {
        int fragmentIndex = getFragmentNumber();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragmentIndex == INDEX_FRAGMENT_LIST) {
            setTaskListFragmentHandlers((TaskListFragment) fragment);
        } else {
            setTaskFragmentHandlers((TaskFragment) fragment);
        }
        refreshUIState(fragmentIndex);
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof TaskListFragment) {
            return INDEX_FRAGMENT_LIST;
        } else {
            return INDEX_FRAGMENT_TASK;
        }
    }


}
