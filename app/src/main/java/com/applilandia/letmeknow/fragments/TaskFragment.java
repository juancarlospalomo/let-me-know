package com.applilandia.letmeknow.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.TaskActivity;
import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.exceptions.UnitOfWorkException;
import com.applilandia.letmeknow.loaders.TaskLoader;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.models.ValidationResult;
import com.applilandia.letmeknow.usecases.UseCaseTask;
import com.applilandia.letmeknow.views.ValidationField;

import java.text.ParseException;
import java.util.List;

/**
 * Created by JuanCarlos on 27/02/2015.
 */
public class TaskFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    private final static String LOG_TAG = TaskFragment.class.getSimpleName();
    private final static int LOADER_ID = 1;

    //Keys for saving state when configuration change occurs
    private static final String KEY_WORK_MODE = "workMode";
    private static final String KEY_TASK = "task";

    public interface OnTaskFragmentListener {
        public void onTaskSaved(Task task);

        public void onClose();
    }

    /**
     * depicts the different transition states of the UI
     */
    private enum UIState {
        DateTimeEmpty,
        DateSet,
        TimeSet
    }

    //Working mode of the fragment
    private TaskActivity.TypeWorkMode mWorkMode = TaskActivity.TypeWorkMode.New;
    //Task identifier when the fragment is in update or view modes
    private int mTaskId;
    //Task entity bound to the screen
    private Task mTask = null;
    //Views
    private TextView mTextViewTaskName;
    private TextView mTextViewTaskDateTime;
    private ValidationField mValidationFieldTaskName;
    private ValidationField mValidationFieldDate;
    private ValidationField mValidationFieldTime;
    private ImageView mImageViewClear;
    private RecyclerView mRecyclerViewNotifies;
    private NotificationAdapter mNotificationAdapter;
    private Button mButtonOk;
    //Listener handler
    private OnTaskFragmentListener mOnTaskFragmentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hideSoftKeyboard();
        if (savedInstanceState != null) {
            //Configuration change occurred
            mTask = savedInstanceState.getParcelable(KEY_TASK);
            mWorkMode = TaskActivity.TypeWorkMode.map(savedInstanceState.getInt(KEY_WORK_MODE));
        }
        //Get fixed views from inflated layout
        mRecyclerViewNotifies = (RecyclerView) getView().findViewById(R.id.recyclerViewNotifies);
        mButtonOk = (Button) getView().findViewById(R.id.buttonOk);
        //Init the fragment to set in the correct state
        initWorkMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_WORK_MODE, mWorkMode.getValue());
        mTask.name = mValidationFieldTaskName.getText();
        outState.putParcelable(KEY_TASK, mTask);
    }

    /**
     * Init the UI according to the work mode state
     */
    private void initWorkMode() {
        mValidationFieldTaskName = (ValidationField) getView().findViewById(R.id.validationViewTaskName);
        mValidationFieldDate = (ValidationField) getView().findViewById(R.id.validationViewDate);
        mValidationFieldTime = (ValidationField) getView().findViewById(R.id.validationViewTime);
        mImageViewClear = (ImageView) getView().findViewById(R.id.imageViewClear);
        createRecyclerViewNotifications();
        if (mWorkMode == TaskActivity.TypeWorkMode.New) {
            //Init Views with initial values
            mValidationFieldTaskName.setText(mTask.name);
            if (mTask.targetDateTime != null) {
                mValidationFieldDate.setText(mTask.targetDateTime.getDisplayFormatDate());
                if (!mTask.targetDateTime.isTimeNull()) {
                    mValidationFieldTime.setText(mTask.targetDateTime.getDisplayFormatTime(getActivity()));
                    refreshUIStatus(UIState.TimeSet);
                } else {
                    refreshUIStatus(UIState.DateSet);
                }
            } else {
                refreshUIStatus(UIState.DateTimeEmpty);
            }
            //Create Handlers
            createValidationTaskNameHandler();
            createDateTimeHandlers();
            createClearHandler();
            //Set initial State

        }
        if (mWorkMode == TaskActivity.TypeWorkMode.View) {
            mValidationFieldTaskName.setVisibility(View.GONE);
            getView().findViewById(R.id.layoutDateTimeFields).setVisibility(View.GONE);
            mTextViewTaskName = (TextView) getView().findViewById(R.id.textViewTaskName);
            mTextViewTaskDateTime = (TextView) getView().findViewById(R.id.textViewDateTime);
            getView().findViewById(R.id.layoutTaskName).setVisibility(View.VISIBLE);
            mTextViewTaskDateTime.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.imageViewEdit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeToUpdateWorkMode();
                }
            });
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
        createButtonOkHandler();
    }

    /**
     * Change working mode to update, changing the UI according to it
     */
    private void changeToUpdateWorkMode() {
        //Create Handlers
        createValidationTaskNameHandler();
        createDateTimeHandlers();
        createClearHandler();
        //Change UI Views
        mValidationFieldTaskName.setVisibility(View.VISIBLE);
        mValidationFieldTaskName.setAlpha(0);
        mValidationFieldTaskName.animate().alpha(1)
                .setInterpolator(new LinearInterpolator())
                .setDuration(1000)
                .start();
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.layoutDateTimeFields);
        layout.setVisibility(View.VISIBLE);
        mValidationFieldDate.animate().alpha(1)
                .setInterpolator(new LinearInterpolator())
                .setDuration(1000)
                .start();
        mValidationFieldTime.animate().alpha(1)
                .setInterpolator(new LinearInterpolator())
                .setDuration(1000)
                .start();
        //Hide view controls
        getView().findViewById(R.id.layoutTaskName).setVisibility(View.GONE);
        mTextViewTaskDateTime.setVisibility(View.GONE);
        //Fill data
        mValidationFieldTaskName.setText(mTask.name);
        if (mTask.targetDateTime != null) {
            mValidationFieldDate.setText(mTask.targetDateTime.getDisplayFormatDate());
            if (!mTask.targetDateTime.isTimeNull()) {
                mValidationFieldTime.setText(mTask.targetDateTime.getDisplayFormatTime(getActivity()));
                refreshUIStatus(UIState.TimeSet);
            } else {
                refreshUIStatus(UIState.DateSet);
            }
        } else {
            refreshUIStatus(UIState.DateTimeEmpty);
        }
        mWorkMode = TaskActivity.TypeWorkMode.Update;
    }


    /**
     * Create handler for task name edition field
     */
    private void createValidationTaskNameHandler() {
        mValidationFieldTaskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationFieldTaskName.removeError();
            }
        });
    }

    /**
     * Init the recycler view for notifications
     */
    private void createRecyclerViewNotifications() {
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mRecyclerViewNotifies.setHasFixedSize(true);
        //It will use a LinearLayout
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerViewNotifies.setLayoutManager(layoutManager);
        mRecyclerViewNotifies.addItemDecoration(new NotificationItemDecoration());
        mNotificationAdapter = new NotificationAdapter(false);
        mRecyclerViewNotifies.setAdapter(mNotificationAdapter);
    }

    /**
     * Create handler for Ok Button
     */
    private void createButtonOkHandler() {
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWorkMode == TaskActivity.TypeWorkMode.View) {
                    if (mOnTaskFragmentListener != null) {
                        mOnTaskFragmentListener.onClose();
                    }
                } else {
                    //Create or update task including date, although it can be null
                    mTask.name = mValidationFieldTaskName.getText();
                    List<ValidationResult> validationResults = mTask.validate();
                    if (validationResults == null) {
                        boolean resultOk = false;
                        try {
                            UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                            if (mWorkMode == TaskActivity.TypeWorkMode.New) {
                                mTask._id = useCaseTask.createTask(mTask);
                                resultOk = (mTask._id > 0);
                            } else if (mWorkMode == TaskActivity.TypeWorkMode.Update) {
                                resultOk = useCaseTask.updateTask(mTask);
                            }
                            if (resultOk) {
                                if (mOnTaskFragmentListener != null) {
                                    mOnTaskFragmentListener.onTaskSaved(mTask);
                                }
                            }
                        } catch (UnitOfWorkException exception) {
                            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.error_title),
                                    getString(R.string.unit_of_work_exception), null, getString(R.string.error_button_ok));
                            alertDialogFragment.show(getFragmentManager(), "errorDialog");
                        }

                    } else {
                        for (ValidationResult validationResult : validationResults) {
                            switch (validationResult.member) {
                                case "name":
                                    if (validationResult.code == ValidationResult.ValidationCode.Empty) {
                                        mValidationFieldTaskName.setError(getString(R.string.error_task_name_empty));
                                    }
                                    if (validationResult.code == ValidationResult.ValidationCode.GreaterThanRange) {
                                        mValidationFieldTaskName.setError(getString(R.string.error_task_name_greater_than_max));
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Hide the input keyboard method
     */
    private void hideSoftKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Create the handler for clear the Date & Time data
     */
    private void createClearHandler() {
        mImageViewClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationFieldDate.setText(R.string.hint_edit_task_date);
                mValidationFieldDate.removeError();
                mValidationFieldTime.setText(R.string.hint_edit_task_time);
                mValidationFieldTime.removeError();
                mTask.targetDateTime = null;
                refreshUIStatus(UIState.DateTimeEmpty);
            }
        });
    }

    /**
     * Disable Time action View
     */
    private void disableTime() {
        mValidationFieldTime.setClickable(false);
        mValidationFieldTime.setEnabled(false);
    }

    /**
     * Enable Time action View
     */
    private void enableTime() {
        mValidationFieldTime.setClickable(true);
        mValidationFieldTime.setEnabled(true);
    }

    /**
     * Update the UI Status according to its state
     */
    private void refreshUIStatus(UIState state) {
        switch (state) {
            case DateTimeEmpty:
                //Notifies and Time must be disabled
                disableTime();
                mNotificationAdapter.setEnabled(false);
                break;

            case DateSet:
                enableTime();
                break;

            case TimeSet:
                mNotificationAdapter.setEnabled(true);
                break;
        }
    }

    /**
     * Create the handlers for Date and Time views
     */
    private void createDateTimeHandlers() {
        //Handler for Date
        mValidationFieldDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidationFieldDate.removeError();
                mValidationFieldDate.requestFocus();
                DateDialogFragment dateDialogFragment = new DateDialogFragment();
                dateDialogFragment.setInitialDate(mTask.targetDateTime);
                dateDialogFragment.setOnDateDialogListener(new DateDialogFragment.OnDateDialogListener() {
                    @Override
                    public void onOk(LocalDate date) {
                        if (date.compareTo(new LocalDate()) >= 0) {
                            mTask.targetDateTime = date;
                            mValidationFieldDate.setText(date.getDisplayFormatDate());
                            refreshUIStatus(UIState.DateSet);
                        } else {
                            mTask.targetDateTime = null;
                            mValidationFieldTime.setText(R.string.hint_edit_task_time);
                            mValidationFieldDate.setText(R.string.hint_edit_task_date);
                            mValidationFieldDate.setError(R.string.error_task_date_less_than_today);
                            refreshUIStatus(UIState.DateTimeEmpty);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                dateDialogFragment.show(getFragmentManager(), "dateDialog");
            }
        });
        //Handler for time
        mValidationFieldTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTask.targetDateTime != null) {
                    mValidationFieldTime.removeError();
                    mValidationFieldTime.requestFocus();
                    TimeDialogFragment timeDialogFragment = new TimeDialogFragment();
                    if (!mTask.targetDateTime.isTimeNull()) {
                        timeDialogFragment.setInitialTime(mTask.targetDateTime.getHour(),
                                mTask.targetDateTime.getMinute());
                    }
                    timeDialogFragment.setOnDateDialogListener(new TimeDialogFragment.OnTimeDialogListener() {
                        @Override
                        public void onOk(int hour, int minute) {
                            mTask.targetDateTime.setTime(hour, minute);
                            if (mTask.targetDateTime.compareTo(new LocalDate()) >= 0) {
                                mValidationFieldTime.setText(mTask.targetDateTime.getDisplayFormatTime(getActivity()));
                                refreshUIStatus(UIState.TimeSet);
                            } else {
                                mValidationFieldTime.setText(R.string.hint_edit_task_time);
                                mTask.targetDateTime.removeTime();
                                mValidationFieldTime.setError(R.string.error_task_time_less_than_today);
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    timeDialogFragment.show(getFragmentManager(), "timeDialog");
                }
            }
        });
    }

    /**
     * Set the working mode for the fragment
     *
     * @param value    type working mode
     * @param taskId   if the working mode is different from new, the task identifier
     * @param taskName initial task name to show
     * @param taskDate initial task date to show
     */
    public void setWorkMode(TaskActivity.TypeWorkMode value, int taskId, String taskName, String taskDate) {
        mWorkMode = value;
        if (mWorkMode == TaskActivity.TypeWorkMode.New) {
            mTask = new Task();
            mTask.name = taskName;
            if (!TextUtils.isEmpty(taskDate)) {
                try {
                    mTask.targetDateTime = new LocalDate(taskDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        mTaskId = taskId;
    }

    /**
     * Set the listener to which send events
     *
     * @param l
     */
    public void setOnTaskFragmentListener(OnTaskFragmentListener l) {
        mOnTaskFragmentListener = l;
    }

    /**
     * Loader Manager
     *
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        return new TaskLoader(getActivity(), mTaskId);
    }

    /**
     * Loader Manager
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == LOADER_ID) {
            if ((data != null) && (data.size() == 1)) {
                mTask = data.get(0);
                mTextViewTaskName.setText(mTask.name);
                if (mTask.targetDateTime != null) {
                    mTextViewTaskDateTime.setText(mTask.targetDateTime.getDisplayFormat(getActivity()));
                    if (!mTask.targetDateTime.isTimeNull()) {
                        refreshUIStatus(UIState.TimeSet);
                    } else {
                        refreshUIStatus(UIState.DateSet);
                    }
                } else {
                    refreshUIStatus(UIState.DateTimeEmpty);
                }
                createRecyclerViewNotifications();
            }
        }
    }

    /**
     * Loader Manager
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        mRecyclerViewNotifies.setAdapter(null);
    }

    /**
     * Adapter class for notifications recycler view
     */
    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private boolean mEnabled = false;
        private String[] mTypeNotifications = getResources().getStringArray(R.array.type_notifications_array);

        /**
         * Constructor that allows to set the enabled/disabled status to the list
         *
         * @param enabled
         */
        private NotificationAdapter(boolean enabled) {
            super();
            mEnabled = enabled;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_notification, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mTextViewNotification.setText(mTypeNotifications[position]);
            if (!mEnabled) {
                holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                disableRow(holder);
            } else {
                if (!mTask.isNotificationAllowed(Notification.TypeNotification.map(position))) {
                    disableRow(holder);
                    return;
                }
                if (mTask.getNotifications() != null) {
                    if (mTask.getNotification(Notification.TypeNotification.map(position)) != null) {
                        holder.mImageViewCheck.setImageResource(R.drawable.ic_check_on);
                    } else {
                        holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                    }
                } else {
                    holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                }
                enableRow(holder);
                holder.mImageViewCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isActive = false;
                        if (mTask.getNotifications() != null) {
                            isActive = (mTask.getNotification(Notification.TypeNotification.map(position)) != null);
                        }
                        if (isActive) {
                            //currently this notification type is active, so
                            //the user wants to remove it
                            holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                            mTask.removeNotification(Notification.TypeNotification.map(position));
                        } else {
                            //currently this notification type isn't active.
                            //User wants to active it
                            holder.mImageViewCheck.setImageResource(R.drawable.ic_check_on);
                            //create notification and add it
                            Notification notification = new Notification();
                            notification.type = Notification.TypeNotification.map(position);
                            notification.status = Notification.TypeStatus.Pending;
                            mTask.addNotification(notification);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mTypeNotifications.length;
        }

        /**
         * View Holder
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTextViewNotification;
            private ImageView mImageViewCheck;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextViewNotification = (TextView) itemView.findViewById(R.id.textViewNotificationType);
                mImageViewCheck = (ImageView) itemView.findViewById(R.id.imageViewCheck);
            }
        }

        /**
         * Set the enable/disable status to the List
         *
         * @param enable
         */
        public void setEnabled(boolean enable) {
            mEnabled = enable;
            notifyDataSetChanged();
        }

        /**
         * Paint row as disabled
         */
        private void disableRow(final ViewHolder holder) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(holder.mImageViewCheck,
                    "alpha", 0.2f);
            objectAnimator.setEvaluator(new FloatEvaluator());
            objectAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            objectAnimator.start();
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    holder.mImageViewCheck.setAlpha(0.2f);
                }
            });
            holder.mTextViewNotification.setTextColor(getResources().getColor(R.color.button_flat_text_disabled));
            holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
        }

        /**
         * Paint row as enable
         *
         * @param holder
         */
        private void enableRow(final ViewHolder holder) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(holder.mImageViewCheck,
                    "alpha", 1f);
            objectAnimator.setEvaluator(new FloatEvaluator());
            objectAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            objectAnimator.start();
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    holder.mImageViewCheck.setAlpha(1f);
                }
            });
            holder.mTextViewNotification.setTextColor(getResources().getColor(R.color.button_flat_text_normal));
        }
    }

    public class NotificationItemDecoration extends RecyclerView.ItemDecoration {

        Drawable mDivider;

        public NotificationItemDecoration() {
            mDivider = getResources().getDrawable(R.drawable.list_row_background);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildPosition(view) < 1) return;
            if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayout.VERTICAL) {
                outRect.top = mDivider.getIntrinsicHeight();
            } else {
                return;
            }
        }
    }

}
