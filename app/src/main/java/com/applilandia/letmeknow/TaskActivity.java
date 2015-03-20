package com.applilandia.letmeknow;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.applilandia.letmeknow.fragments.TaskFragment;


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
    public final static String EXTRA_TASK_NAME = "TaskName"; //Initial Task Name
    public final static String EXTRA_TASK_DATE = "TaskDate"; //Initial Task Date

    private TypeWorkMode mWorkMode = TypeWorkMode.New;
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
        createFragmentContent();
    }

    /**
     * Load the extra params got through the Intent
     */
    private void loadExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mWorkMode = TypeWorkMode.map(bundle.getInt(EXTRA_WORK_MODE));
            mInitialTaskName = bundle.getString(EXTRA_TASK_NAME);
            mInitialTaskDate = bundle.getString(EXTRA_TASK_DATE);
        }
    }

    /**
     * Add the Fragment for the content
     */
    private void createFragmentContent() {
        mTaskFragment = new TaskFragment();
        mTaskFragment.setWorkMode(mWorkMode, 0, mInitialTaskName, mInitialTaskDate);
        mTaskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onClose() {
                setResult(RESULT_OK);
                finish();
            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, mTaskFragment);
        transaction.commit();
    }



}
