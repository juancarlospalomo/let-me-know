package com.applilandia.letmeknow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.applilandia.letmeknow.fragments.TaskFragment;
import com.applilandia.letmeknow.models.Task;


public class TaskActivity extends ActionBarActivity {

    public enum TypeWorkMode {
        New(0),
        Update(1),
        View(2);

        private int mValue;

        private TypeWorkMode(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static TypeWorkMode map(int value) {
            return TypeWorkMode.values()[value];
        }
    }

    //Extras
    public final static String EXTRA_WORK_MODE = "WorkMode"; //State the work mode of the Activity according to the TypeWorkMode
    public final static String EXTRA_TASK_ID = "TaskId"; //Initial Task Id
    public final static String EXTRA_TASK_NAME = "TaskName"; //Initial Task Name
    public final static String EXTRA_TASK_DATE = "TaskDate"; //Initial Task Date
    public final static String EXTRA_TASK_TYPE = "TaskType"; //Type of task.  It is an out extra

    private TypeWorkMode mWorkMode = TypeWorkMode.New;
    private int mInitialTaskId; //Variable for initial TaskId
    private String mInitialTaskName; //Variable for initial Task Name
    private String mInitialTaskDate; //Variable for initial Task Date
    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Configure Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadExtras();
        if (savedInstanceState != null) {
            createFragmentContent(false);
        } else {
            createFragmentContent(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load the extra params got through the Intent
     */
    private void loadExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mWorkMode = TypeWorkMode.map(bundle.getInt(EXTRA_WORK_MODE));
            mInitialTaskId = bundle.getInt(EXTRA_TASK_ID);
            mInitialTaskName = bundle.getString(EXTRA_TASK_NAME);
            mInitialTaskDate = bundle.getString(EXTRA_TASK_DATE);
        }
    }

    /**
     * Add the Fragment for the content
     */
    private void createFragmentContent(boolean addNew) {
        if (addNew) {
            mTaskFragment = new TaskFragment();
            mTaskFragment.setWorkMode(mWorkMode, mInitialTaskId, mInitialTaskName, mInitialTaskDate);
        } else {
            mTaskFragment = (TaskFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        }
        mTaskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved(Task task) {
                Intent intent = null;
                if (task != null) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_TASK_TYPE, task.typeTask.getValue());
                    intent.putExtra(EXTRA_TASK_ID, task._id);
                }
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onClose() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        if (addNew) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, mTaskFragment);
            transaction.commit();
        }

    }


}
