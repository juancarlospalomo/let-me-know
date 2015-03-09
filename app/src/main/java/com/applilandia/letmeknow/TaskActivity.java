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

    public final static String EXTRA_WORK_MODE = "WorkMode";

    private TypeWorkMode mWorkMode = TypeWorkMode.New;
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
        }
    }

    /**
     * Add the Fragment for the content
     */
    private void createFragmentContent() {
        mTaskFragment = new TaskFragment();
        mTaskFragment.setWorkMode(mWorkMode);
        mTaskFragment.setOnTaskFragmentListener(new TaskFragment.OnTaskFragmentListener() {
            @Override
            public void onTaskSaved() {
                setResult(RESULT_OK);
                finish();
            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, mTaskFragment);
        transaction.commit();
    }



}
