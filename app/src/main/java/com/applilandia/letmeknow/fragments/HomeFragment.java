package com.applilandia.letmeknow.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.applilandia.letmeknow.models.Summary;
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
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Summary>> {

    private final static String LOG_TAG = HomeFragment.class.getSimpleName();

    private final static int LOADER_SUMMARY = 1;

    //Keys for saving state when configuration change occurs
    private static final String KEY_FIRST_VISIBLE_POSITION = "firstPosition";
    private static final String KEY_GRID_STATE = "gridState";
    private static final String KEY_ITEM_POSITION = "itemPosition";

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
    //Variables for saving state when configuration change occurs
    private int mFirstVisiblePosition;
    private int mItemPosition;
    private Parcelable mGridState = null;


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
        if (savedInstanceState != null) {
            //Configuration change occurred
            mFirstVisiblePosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_POSITION);
            mItemPosition = savedInstanceState.getInt(KEY_ITEM_POSITION);
            mGridState = savedInstanceState.getParcelable(KEY_GRID_STATE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Init the loader manager to start loading
        if (getLoaderManager().getLoader(LOADER_SUMMARY) == null) {
            getLoaderManager().initLoader(LOADER_SUMMARY, null, this);
        } else {
            getLoaderManager().restartLoader(LOADER_SUMMARY, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FIRST_VISIBLE_POSITION, mGridView.getFirstVisiblePosition());
        outState.putParcelable(KEY_GRID_STATE, mGridView.onSaveInstanceState());
        View view = mGridView.getChildAt(0);
        mItemPosition = view == null ? 0 : view.getTop();
        outState.putInt(KEY_ITEM_POSITION, mItemPosition);
    }

    /**
     * Restore the state before configuration change occurred
     */
    private void restoreSavedState() {
        if (mGridState != null) {
            mGridView.onRestoreInstanceState(mGridState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mGridView.setSelectionFromTop(mFirstVisiblePosition, mItemPosition);
            } else {
                mGridView.setSelection(mFirstVisiblePosition);
            }
            mGridState = null;
        }
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
            if (useCaseTask.createTask(task) > 0) {
                mEditTextTask.setText("");
            } else {
                //TODO: Show Error Dialog
            }
        } else {
            for (ValidationResult validationResult : validationResults) {
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
    public Loader<List<Summary>> onCreateLoader(int id, Bundle args) {
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
    public void onLoadFinished(Loader<List<Summary>> loader, List<Summary> data) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = new GridTilesAdapter(data);
            mGridView.setAdapter(mAdapter);
            restoreSavedState();
        }
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Loader is getting reset
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<List<Summary>> loader) {
        if (loader.getId() == LOADER_SUMMARY) {
            mAdapter = null;
            mGridView.setAdapter(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_VOICE_RECOGNIZER:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    navigateToAddTask(words);
                }
        }
    }

    /**
     * @param words
     */
    private void navigateToAddTask(ArrayList<String> words) {
        if (words.size() > 0) {
            VoiceInputToken inputToken = new VoiceInputToken(words.get(0));
            inputToken.parse();
            Intent intent = new Intent(getActivity(), TaskActivity.class);
            intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.New);
            intent.putExtra(TaskActivity.EXTRA_TASK_NAME, inputToken.getTaskName());
            startActivity(intent);
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
        private List<Summary> mSummaryList;

        private GridTilesAdapter(List<Summary> summaryList) {
            this.mSummaryList = summaryList;
        }

        @Override
        public int getCount() {
            return mSummaryList.size();
        }

        @Override
        public Summary getItem(int position) {
            return mSummaryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (mSummaryList.get(position).task != null) {
                return mSummaryList.get(position).task._id;
            } else {
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

            String contentText = getResources().getStringArray(R.array.text_tile_content)[position];
            contentText += " " + String.valueOf(mSummaryList.get(position).count);
            viewHolder.tile.setContentText(contentText);
            viewHolder.tile.setContentBackground(getBackground(position));
            final Task task = mSummaryList.get(position).task;
            if (task != null) {
                viewHolder.tile.setFooterPrimaryLine(task.name);
                if (position != INDEX_TILE_ANYTIME) {
                    if ((task.targetDateTime != null) && (!task.targetDateTime.isNull())) {
                        viewHolder.tile.setFooterSecondaryLine(task.targetDateTime.getDisplayFormat(getActivity()));
                    }
                    if (task.getCurrentNotificationsCount() > 0) {
                        viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_on);
                    } else {
                        viewHolder.tile.setFooterIcon(R.drawable.ic_alarm_off);
                    }
                } else {
                    viewHolder.tile.setFooterSecondaryLine("");
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
                    if (task != null) {
                        Intent intent = new Intent(getActivity(), TaskActivity.class);
                        intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.View.getValue());
                        intent.putExtra(TaskActivity.EXTRA_TASK_ID, task._id);
                        startActivity(intent);
                    }
                }
            });
            viewHolder.tile.setIconOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (task != null) {
                        Intent intent = new Intent(getActivity(), TaskActivity.class);
                        intent.putExtra(TaskActivity.EXTRA_WORK_MODE, TaskActivity.TypeWorkMode.View.getValue());
                        intent.putExtra(TaskActivity.EXTRA_TASK_ID, task._id);
                        startActivity(intent);
                    }
                }
            });

            return convertView;
        }

        /**
         * Create activity for showing the task list belonging to a type
         * @param position
         */
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
         * Get background resource depending of the type of tile
         * @param position in the adapter
         * @return background resource identifier
         */
        private int getBackground(int position) {
            switch (position) {
                case INDEX_TILE_EXPIRED:
                    return R.drawable.tile_expired_content_background;
                case INDEX_TILE_TODAY:
                    return R.drawable.tile_today_content_background;
                case INDEX_TILE_FUTURE:
                    return R.drawable.tile_future_content_background;
                case INDEX_TILE_ANYTIME:
                    return R.drawable.tile_anytime_content_background;
                default:
                    return  R.drawable.tile_expired_content_background;
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
