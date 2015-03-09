package com.applilandia.letmeknow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.applilandia.letmeknow.fragments.TaskListFragment;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.views.FloatingActionButton;


public class TaskListActivity extends ActionBarActivity {

    public final static String EXTRA_TYPE_TASK = "TypeTask";

    private Task.TypeTask mTypeTask = Task.TypeTask.All;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadExtras();
        createFloatingActionButton();
        createTaskSpinner();
        createTasksFragment();
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_task_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Create Fragment to show the task list
     */
    private void createTasksFragment() {
        TaskListFragment taskListFragment = new TaskListFragment();
        taskListFragment.setTypeTask(mTypeTask);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, taskListFragment)
                .commit();
    }

}
