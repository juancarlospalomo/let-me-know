package com.applilandia.letmeknow.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.TaskActivity;
import com.applilandia.letmeknow.TaskListActivity;
import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.cross.VoiceInputToken;
import com.applilandia.letmeknow.loaders.SummaryLoader;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.models.ValidationResult;
import com.applilandia.letmeknow.usecases.UseCaseTask;
import com.applilandia.letmeknow.views.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by JuanCarlos on 24/02/2015.
 * Fragment that displays the home activity screen
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    private final static String LOG_TAG = HomeFragment.class.getSimpleName();

    private final static int LOADER_SUMMARY = 1;

    private final static int REQUEST_CODE_EXPIRED_TASKS = 1;
    private final static int REQUEST_CODE_TODAY_TASKS = 2;
    private final static int REQUEST_CODE_FUTURE_TASKS = 3;
    private final static int REQUEST_CODE_ANYTIME_TASKS = 4;
    private final static int REQUEST_CODE_VOICE_RECOGNIZER = 5;

    private ProgressBar mProgressBar;
    private GridView mGridView;
    private GridTilesAdapter mAdapter; //Adapter for linking to GridView
    private EditText mEditTextTask;
    private ImageView mImageViewActionIcon; //Microphone or Accept

    /**
     * Creates and returns the view hierarchy associated with the fragment
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Tells the fragment that its activity has completed its own Activity.onCreated()
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Once the fragment is attached to the activity, we can hide it
        //because the window token is already available
        hideSoftKeyboard();
        View view = getView();
        //Get views objects
        mGridView = (GridView) view.findViewById(R.id.gridTiles);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressActivityMain);
        mEditTextTask = (EditText) view.findViewById(R.id.editTextTaskName);
        mEditTextTask.addTextChangedListener(new EditTaskWatcher());
        mImageViewActionIcon = (ImageView) view.findViewById(R.id.imageViewAccept);
        mImageViewActionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterCreateTaskAction();
            }
        });
        //Init the loader manager to start loading
        getLoaderManager().initLoader(LOADER_SUMMARY, null, this);
    }

    /**
     * Hide the input keyboard method
     */
    private void hideSoftKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Using the UseCases layer, creates a task
     */
    private void createTask(LocalDate date) {
        //Create task including date, although it can be null
        Task task = new Task();
        task.name = mEditTextTask.getText().toString();
        task.targetDateTime = date;
        List<ValidationResult> validationResults = task.validate();
        if (validationResults == null) {
            UseCaseTask useCaseTask = new UseCaseTask(getActivity());
            if (useCaseTask.createTask(task)>0) {
                mEditTextTask.setText("");
            } else {
                //TODO: Show Error Dialog
            }
        } else {
            for(ValidationResult validationResult : validationResults) {
                switch (validationResult.member) {
                    case "name":
                        mEditTextTask.setError(getString(R.string.error_task_name_greater_than_max));
                        break;
                }
            }
        }
    }

    /**
     * Show the intent recognizer to listen the voice
     */
    private void showVoiceRecognition() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(i, REQUEST_CODE_VOICE_RECOGNIZER);
        } catch (ActivityNotFoundException e) {
            AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getString(R.string.voice_intent_error_title),
                    getString(R.string.voice_intent_error_content), null, getString(R.string.voice_intent_error_ok));
            alertDialog.show(getFragmentManager(), "dialog");
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start the action to create task, calling to a Date Dialog
     */
    private void enterCreateTaskAction() {
        if (mEditTextTask.getText().length() > 0) {
            //Manual enter
            DateDialogFragment dateDialogFragment = new DateDialogFragment();
            dateDialogFragment.setOnDateDialogListener(new DateDialogFragment.OnDateDialogListener() {
                @Override
                public void onOk(LocalDate date) {
                    createTask(date);
                }

                @Override
                public void onCancel() {
                    //Create task without date
                    createTask(null);
                }
            });
            dateDialogFragment.show(getFragmentManager(), "dateDialog");
        } else {
            //Enter by voice
            showVoiceRecognition();
        }

    }

    /**
     * Called by the loader manager when a loader is created or restarted
     *
     * @param id   loader id
     * @param args arguments
     * @return Loader of List of Tasks
     */
    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new SummaryLoader(getActivity());
    }

    /**
     * Called from the LoaderManager when the loader delivers the list
     *
     * @param loader loader
     * @param data   data delivered by the loader
     */
    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = new GridTilesAdapter(data);
            mGridView.setAdapter(mAdapter);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_EXPIRED_TASKS:
                Toast.makeText(getActivity(), "expired", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_VOICE_RECOGNIZER:
                if (resultCode== Activity.RESULT_OK) {
                    ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    navigateToAddTask(words);
                }
            default:
                Toast.makeText(getActivity(), String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @param words
     */
    private void navigateToAddTask(ArrayList<String> words) {
        if (words.size()>0) {
            VoiceInputToken inputToken = new VoiceInputToken(words.get(0));
            inputToken.parse();
            Intent intent = new Intent(getActivity(), TaskActivity.class);
            intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.New);
            intent.putExtra(TaskActivity.EXTRA_TASK_NAME, inputToken.getTaskName());
            startActivity(intent);
        }
    }

    /**
     * Loader is getting reset
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = null;
            mGridView.setAdapter(null);
        }
    }

    /**
     * Class for ViewHolder Pattern
     */
    private static class ViewHolder {
        Tile tile;
    }

    /**
     * Adapter for the Grid
     */
    private class GridTilesAdapter extends BaseAdapter {

        private final static int INDEX_TILE_EXPIRED = 0;
        private final static int INDEX_TILE_TODAY = 1;
        private final static int INDEX_TILE_FUTURE = 2;
        private final static int INDEX_TILE_ANYTIME = 3;

        private final String LOG_TAG = GridTilesAdapter.class.getSimpleName();

        //Task List of the Adapter
        private List<Task> mTaskList;

        private GridTilesAdapter(List<Task> mTaskList) {
            this.mTaskList = mTaskList;
        }

        @Override
        public int getCount() {
            return mTaskList.size();
        }

        @Override
        public Task getItem(int position) {
            return mTaskList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mTaskList.get(position) != null) {
                return mTaskList.get(position)._id;
            } else {
                //TODO: Remove Log
                Log.v(LOG_TAG, String.valueOf(position));
                return 0;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.grid_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tile = (Tile) convertView.findViewById(R.id.tileTask);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tile.setContentText(getResources().getStringArray(R.array.text_tile_content)[position]);
            viewHolder.tile.setContentBackgroundColor(getColor(position));
            Task task = mTaskList.get(position);
            if (task != null) {
                viewHolder.tile.setFooterPrimaryLine(task.name);
                if ((task.targetDateTime != null) && (!task.targetDateTime.isNull())) {
                    viewHolder.tile.setFooterSecondaryLine(task.targetDateTime.getDisplayFormat(getActivity()));
                }
                if (task.getCurrentNotificationsCount() > 0) {
                    viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_on);
                } else {
                    viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_off);
                }
            } else {
                String[] textDefaults = getResources().getStringArray(R.array.text_defaults_primary_line);
                viewHolder.tile.setFooterPrimaryLine(textDefaults[position]);
            }
            viewHolder.tile.setContentOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTasksList(position);
                }
            });
            viewHolder.tile.setFooterTextOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "tile", Toast.LENGTH_SHORT).show();
                }
            });
            viewHolder.tile.setIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "tile", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }

        private void showTasksList(int position) {
            Intent intent = new Intent(getActivity(), TaskListActivity.class);
            switch (position) {
                case INDEX_TILE_EXPIRED:
                    intent.putExtra(TaskListActivity.EXTRA_TYPE_TASK, Task.TypeTask.Expired.getValue());
                    startActivityForResult(intent, REQUEST_CODE_EXPIRED_TASKS);
                    break;

                case INDEX_TILE_TODAY:
                    intent.putExtra(TaskListActivity.EXTRA_TYPE_TASK, Task.TypeTask.Today.getValue());
                    startActivityForResult(intent, REQUEST_CODE_TODAY_TASKS);
                    break;

                case INDEX_TILE_FUTURE:
                    intent.putExtra(TaskListActivity.EXTRA_TYPE_TASK, Task.TypeTask.Future.getValue());
                    startActivityForResult(intent, REQUEST_CODE_FUTURE_TASKS);
                    break;

                case INDEX_TILE_ANYTIME:
                    intent.putExtra(TaskListActivity.EXTRA_TYPE_TASK, Task.TypeTask.AnyTime.getValue());
                    startActivityForResult(intent, REQUEST_CODE_ANYTIME_TASKS);
                    break;
            }
        }

        /**
         * Returns the color according to the required tile position
         *
         * @param position position in the adapter
         * @return color
         */
        private int getColor(int position) {
            switch (position) {
                case INDEX_TILE_EXPIRED:
                    return getResources().getColor(R.color.tile_expired_content_background);
                case INDEX_TILE_TODAY:
                    return getResources().getColor(R.color.tile_today_content_background);
                case INDEX_TILE_FUTURE:
                    return getResources().getColor(R.color.tile_future_content_background);
                case INDEX_TILE_ANYTIME:
                    return getResources().getColor(R.color.tile_anytime_content_background);
                default:
                    return getResources().getColor(R.color.tile_expired_content_background);
            }
        }
    }

    /**
     * Class to watch the Task EditText to change the UI actions according
     * to its content.  If content is empty, microphone is visible else accept icon
     */
    private class EditTaskWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 0) {
                //It´s empty
                mImageViewActionIcon.setImageResource(R.drawable.ic_mic_on);
            } else {
                mImageViewActionIcon.setImageResource(R.drawable.ic_action_check);
            }
        }
    }

}
