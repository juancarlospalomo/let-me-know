package com.applilandia.letmeknow.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.Message;
import com.applilandia.letmeknow.listeners.RecyclerViewClickListener;
import com.applilandia.letmeknow.listeners.RecyclerViewMotion;
import com.applilandia.letmeknow.loaders.TaskLoader;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;
import com.applilandia.letmeknow.views.SnackBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 27/02/2015.
 */
public class TaskListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    //For logging purpose
    private final static String LOG_TAG = TaskListFragment.class.getSimpleName();

    public interface OnTaskListFragmentListener {
        /**
         * Triggered when a Task has been clicked and action bar is not activated
         *
         * @param id task identifier
         */
        public void onTaskSelected(int id);

        /**
         * Triggered when the user does a long press on a row
         */
        public void onTaskLongPressed();

        /**
         * Triggered when a task has been deleted
         */
        public void onTaskDeleted();

        /**
         * Triggered when the actions are removed from the toolbar
         */
        public void onToolbarActionClose();
    }

    //LoaderId for task loader
    private final static int TASK_LOADER_ID = 1;
    //State KEYS
    private final static String KEY_LIST_SCROLL_POSITION = "scrollPosition";
    private final static String KEY_LIST_SCROLL_POSITION_OFFSET = "scrollPositionOffset";
    private final static String KEY_TASKS_LIST_SELECTED = "tasksSelected";
    private final static String KEY_TOOLBAR_STATE = "toolBarState";
    private final static String KEY_TASK_TYPE = "taskType";
    //Variables to state the init recyclerview scroll
    private int mShowRowPosition = -1;
    private int mShowRowTask = -1; //Task Id of the row
    //State before configuration changed
    private int mListFirstPosition = ListView.INVALID_POSITION;
    private int mListFirstPositionOffset = 0;
    private ArrayList<Task> mTaskListSelectedInstanceState = null;
    //There is an issue with LoaderManager in Fragments when orientation changes:
    //      - loader is retained
    //      - If initLoader is called in onActivityCreated or onStart, onLoadFinished is called twice.
    //              One inside InitLoaded and the other on finishRetain.
    //   See source code of LoaderManager
    //   We did a workaround to avoid the two loads, detecting if an orientation change was produced
    //   and if data has been already delivered after an orientation change
    private boolean mDeliveredData = false;
    //ActionBar status
    private boolean mActionBarActivated = false;
    //Type task to be loaded in the fragment
    private Task.TypeTask mTypeTask;
    private ProgressBar mProgressBar;
    private SnackBar mSnackBar;
    private RecyclerView mTaskRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TaskAdapter mAdapter;
    private OnTaskListFragmentListener mOnTaskListFragmentListener;
    private RecyclerViewMotion mRecyclerViewMotion;
    private ShareActionProvider mShareActionProvider;

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
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    /**
     * Tells the fragment that its activity has completed its own Activity.onCreated()
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        View view = getView();

        if (savedInstanceState != null) {
            mListFirstPosition = savedInstanceState.getInt(KEY_LIST_SCROLL_POSITION);
            mListFirstPositionOffset = savedInstanceState.getInt(KEY_LIST_SCROLL_POSITION_OFFSET);
            mTaskListSelectedInstanceState = savedInstanceState.getParcelableArrayList(KEY_TASKS_LIST_SELECTED);
            mActionBarActivated = savedInstanceState.getBoolean(KEY_TOOLBAR_STATE, false);
            mTypeTask = Task.TypeTask.map(savedInstanceState.getInt(KEY_TASK_TYPE));
        }

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressActivityTask);
        mSnackBar = (SnackBar) view.findViewById(R.id.snackBarTasks);
        initRecyclerView();
        //Init loader.  Data will be set to Adapter in onLoadFinished()
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        //If it comes from a orientation change, mActionBarActivated could be true
        if (mActionBarActivated) {
            activateToolbarActions();
            if (mOnTaskListFragmentListener != null) {
                mOnTaskListFragmentListener.onTaskLongPressed();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            outState.putInt(KEY_LIST_SCROLL_POSITION, ((LinearLayoutManager) mTaskRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
            View view = mTaskRecyclerView.getChildAt(0);
            if (view != null) {
                mListFirstPositionOffset = view.getTop();
            }
            outState.putInt(KEY_LIST_SCROLL_POSITION_OFFSET, mListFirstPositionOffset);
            List<Task> selectedList = mAdapter.getSelectedList();
            if (selectedList != null) {
                mTaskListSelectedInstanceState = new ArrayList<>(selectedList.size());
                mTaskListSelectedInstanceState.addAll(selectedList);
            }
            outState.putParcelableArrayList(KEY_TASKS_LIST_SELECTED, mTaskListSelectedInstanceState);
            outState.putBoolean(KEY_TOOLBAR_STATE, mActionBarActivated);
            outState.putInt(KEY_TASK_TYPE, mTypeTask.getValue());
        }
    }

    private void initRecyclerView() {
        mTaskRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewTasks);
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mTaskRecyclerView.setHasFixedSize(true);
        //It will use a LinearLayout
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTaskRecyclerView.setLayoutManager(mLayoutManager);
        mTaskRecyclerView.addItemDecoration(new TaskItemDecoration());
        mTaskRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(getActivity(),
                new RecyclerViewClickListener.RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        if (!mActionBarActivated) {
                            AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.touch_feedback_animator);
                            animatorSet.setTarget(view);
                            animatorSet.start();
                            animatorSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (mOnTaskListFragmentListener != null) {
                                        mOnTaskListFragmentListener.onTaskSelected((int) mAdapter.getItemId(position));
                                    }
                                }
                            });
                        } else {
                            mAdapter.toggle(position);
                            if (mAdapter.isSelected(position)) {
                                view.setBackgroundResource(R.drawable.list_row_background_selected);
                                view.findViewById(R.id.layout_primary_action_content).setBackgroundResource(R.drawable.list_row_background_selected);
                            } else {
                                view.setBackgroundResource(R.drawable.list_row_background);
                                view.findViewById(R.id.layout_primary_action_content).setBackgroundResource(R.drawable.item_background);
                                if (mAdapter.getSelectedCount() == 0) {
                                    deactivateToolbarActions();
                                    if (mOnTaskListFragmentListener != null) {
                                        mOnTaskListFragmentListener.onToolbarActionClose();
                                    }
                                }
                            }
                            setShareIntent();
                        }
                    }

                    /**
                     * Remove List item applying an animation
                     * @param view Row
                     * @param task task to remove
                     */
                    private void animEndTask(final View view, final Task task) {
                        final ViewGroup.LayoutParams lp = view.getLayoutParams();
                        final int originalHeight = view.getHeight();

                        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));

                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setVisibility(View.INVISIBLE);
                                UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                                if (!useCaseTask.setTaskAsCompleted(task)) {
                                    AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.error_title),
                                            getString(R.string.unit_of_work_exception), null, getString(R.string.error_button_ok));
                                    alertDialogFragment.show(getFragmentManager(), "errorDialog");
                                }
                                // Send a cancel event
                                long time = SystemClock.uptimeMillis();
                                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                                mTaskRecyclerView.dispatchTouchEvent(cancelEvent);
                            }
                        });

                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                lp.height = (Integer) valueAnimator.getAnimatedValue();
                                view.setLayoutParams(lp);
                            }
                        });
                        animator.start();
                    }

                    @Override
                    public void onItemSecondaryActionClick(final View view, final int position) {
                        if (mSnackBar.getVisibility() == View.GONE) {
                            ((ImageView) view).setImageResource(R.drawable.ic_check_on);
                            mSnackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                                @Override
                                public void onClose() {
                                    Task task = mAdapter.mTaskList.get(position);
                                    if (task != null) {
                                        animEndTask((LinearLayout) view.getParent(), task);
                                    }
                                }

                                @Override
                                public void onUndo() {
                                    ((ImageView) view).setImageResource(R.drawable.ic_check_off);
                                }
                            });
                            mSnackBar.show(R.string.snack_bar_task_completed_text);
                        } else {
                            //Undo and set the task is not being ended yet
                            ((ImageView) view).setImageResource(R.drawable.ic_check_off);
                            mSnackBar.undo();
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        activateToolbarActions();
                        mAdapter.toggle(position);
                        view.setBackgroundResource(R.drawable.list_row_background_selected);
                        view.findViewById(R.id.layout_primary_action_content).setBackgroundResource(R.drawable.list_row_background_selected);
                        if (mOnTaskListFragmentListener != null) {
                            mOnTaskListFragmentListener.onTaskLongPressed();
                        }
                    }
                }));
        mRecyclerViewMotion = new RecyclerViewMotion(mTaskRecyclerView, new RecyclerViewMotion.OnRecyclerViewMotion() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(final View view, final int position) {
                if (view != null) {
                    AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getResources().getString(R.string.delete_task_dialog_title),
                            "", getResources().getString(R.string.delete_task_dialog_cancel_text),
                            getResources().getString(R.string.delete_task_dalog_ok_text));
                    alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                                final ViewGroup.LayoutParams lp = view.getLayoutParams();
                                final int originalHeight = view.getHeight();

                                ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));

                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        ViewGroup.LayoutParams lp;
                                        // Reset view presentation
                                        view.setAlpha(1f);
                                        view.setTranslationX(0);
                                        lp = view.getLayoutParams();
                                        lp.height = originalHeight;
                                        view.setLayoutParams(lp);
                                        mAdapter.toggle(position);
                                        AsyncTaskList asyncTaskList = new AsyncTaskList();
                                        asyncTaskList.execute(mAdapter.getSelectedList());
                                        mAdapter.mTaskList.remove(position);
                                        // Send a cancel event
                                        long time = SystemClock.uptimeMillis();
                                        MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                                                MotionEvent.ACTION_CANCEL, 0, 0, 0);
                                        mTaskRecyclerView.dispatchTouchEvent(cancelEvent);
                                    }
                                });

                                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        lp.height = (Integer) valueAnimator.getAnimatedValue();
                                        view.setLayoutParams(lp);
                                    }
                                });
                                animator.start();

                            } else {
                                view.animate()
                                        .translationX(0)
                                        .alpha(1)
                                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                        .setListener(null);
                                // Send a cancel event
                                long time = SystemClock.uptimeMillis();
                                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                                mTaskRecyclerView.dispatchTouchEvent(cancelEvent);
                            }
                        }
                    });
                    alertDialog.show(getFragmentManager(), "dialog");
                }
            }
        });
        mTaskRecyclerView.setOnTouchListener(mRecyclerViewMotion);
    }

    /**
     * Delete the tasks selected
     */
    private void delete() {
        String title;
        if (mAdapter.getSelectedCount() > 1) {
            title = getResources().getString(R.string.delete_tasks_dialog_title);
        } else {
            title = getResources().getString(R.string.delete_task_dialog_title);
        }
        AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(title,
                "", getResources().getString(R.string.delete_task_dialog_cancel_text),
                getResources().getString(R.string.delete_task_dalog_ok_text));
        alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                    AsyncTaskList asyncTaskList = new AsyncTaskList();
                    asyncTaskList.execute(mAdapter.getSelectedList());
                }
            }
        });
        alertDialog.show(getFragmentManager(), "dialog");
    }

    /**
     * Update the share intent
     */
    private void setShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        Message message = new Message(getActivity());
        intent.putExtra(Intent.EXTRA_TEXT, message.getFormattedTaskMessage(mAdapter.getSelectedList()));
        mShareActionProvider.setShareIntent(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mActionBarActivated) {
            inflater.inflate(R.menu.menu_task_list, menu);

            MenuItem menuItem = menu.findItem(R.id.menu_item_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            setShareIntent();
        } else {
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Activate the action icons in the toolbar
     */
    public void activateToolbarActions() {
        mActionBarActivated = true;
        mRecyclerViewMotion.setEnabled(false);
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Deactivate the action bar to remove the actions
     */
    public void deactivateToolbarActions() {
        mActionBarActivated = false;
        mRecyclerViewMotion.setEnabled(true);
        getActivity().invalidateOptionsMenu();
        mAdapter.resetSelected();
    }

    /**
     * Set the task type that fragment has to manage
     *
     * @param typeTask type task
     */
    public void setTypeTask(Task.TypeTask typeTask) {
        mTypeTask = typeTask;
    }

    /**
     * Set the task id of the row that list has to scroll
     *
     * @param id
     */
    public void scrollToTask(int id) {
        mShowRowTask = id;
    }

    /**
     * Set the handler for the events
     *
     * @param l
     */
    public void setOnTaskListFragmentListener(OnTaskListFragmentListener l) {
        mOnTaskListFragmentListener = l;
    }

    /**
     * Show the Empty List Image
     */
    private void showEmptyList() {
        ImageView view = (ImageView) getView().findViewById(R.id.imageEmptyList);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the Empty List Image
     */
    private void hideEmptyList() {
        ImageView view = (ImageView) getView().findViewById(R.id.imageEmptyList);
        view.setVisibility(View.GONE);
    }

    /**
     * Triggered by Loader Manager after init the loaded when it doesn't exist
     * or when restartLoader is called
     *
     * @param id   Loader Id identifier
     * @param args arguments received
     * @return
     */
    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new TaskLoader(getActivity(), mTypeTask);
    }

    /**
     * Called when Cursor Loader wants to deliver the result to a loader
     *
     * @param loader Loader than supply the data
     * @param data   data supplied
     */
    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (mListFirstPosition != ListView.INVALID_POSITION) {
            //Configuration orientation change occurred
            if (!mDeliveredData) {
                //Data has not been already loaded (or delivered)
                mDeliveredData = true;
                loadData(loader, data);
            } else {
                //It has already been delivered, so reset the position saved
                //to forget it was an orientation change
                mListFirstPosition = ListView.INVALID_POSITION;
            }
        } else {
            //It doesn't come from an orientation change
            loadData(loader, data);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Called when the loader is stopped
     *
     * @param loader loader is stopped
     */
    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        if (loader.getId() == TASK_LOADER_ID) {
            mTaskRecyclerView.setAdapter(null);
        }
    }

    private void loadData(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == TASK_LOADER_ID) {
            if (data != null) {
                if (data.size() > 0) {
                    hideEmptyList();
                } else {
                    showEmptyList();
                }
                mAdapter = new TaskAdapter(data, mTaskListSelectedInstanceState);
                mTaskRecyclerView.setAdapter(mAdapter);
                if (mShowRowTask != -1) {
                    convertTaskIdToPosition();
                    mTaskRecyclerView.scrollToPosition(mShowRowPosition);
                } else {
                    if (mListFirstPosition != ListView.INVALID_POSITION) {
                        ((LinearLayoutManager) mTaskRecyclerView.getLayoutManager())
                                .scrollToPositionWithOffset(mListFirstPosition, mListFirstPositionOffset);
                    }
                }
            } else {
                mTaskRecyclerView.setAdapter(null);
                showEmptyList();
            }
        }
    }

    /**
     * Init the position members
     */
    private void initPositionValues() {
        mShowRowTask = -1;
        mShowRowPosition = -1;
    }

    /**
     * Through the task id, get its position in the adapter
     */
    private void convertTaskIdToPosition() {
        if (mAdapter.mTaskList != null) {
            int index = 0;
            for (Task task : mAdapter.mTaskList) {
                if (task._id == mShowRowTask) {
                    mShowRowPosition = index;
                }
                index++;
            }
        }
    }

    /**
     * Adapter for task.  It use the recyclerview adapter
     */
    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
        private int mSelectedCountRef = 0;
        //Dataset for the Adapter
        private List<Task> mTaskList;
        private SparseBooleanArray mTasksSelected;

        /**
         * View Holder pattern
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private LinearLayout mLayoutPrimaryAction;
            private ImageView mAvatarView;
            private TextView mTextPrimaryText;
            private TextView mTextSecondaryText;
            private ImageView mIconView;

            public ViewHolder(View itemView) {
                super(itemView);
                mLayoutPrimaryAction = (LinearLayout) itemView.findViewById(R.id.layout_primary_action_content);
                mAvatarView = (ImageView) itemView.findViewById(R.id.image_primary_action_avatar);
                mTextPrimaryText = (TextView) itemView.findViewById(R.id.textview_primary_text);
                mTextSecondaryText = (TextView) itemView.findViewById(R.id.textview_secondary_text);
                mIconView = (ImageView) itemView.findViewById(R.id.image_secondary_action_icon);
            }
        }

        /**
         * Constructor
         *
         * @param data
         */
        public TaskAdapter(List<Task> data, ArrayList<Task> selectedList) {
            this.mTaskList = data;
            if (selectedList != null) {
                setSelectedList(selectedList);
            }
        }

        /**
         * Create a new ViewHolder when it is required from LayoutManager
         *
         * @param parent   RecyclerView viewgroup
         * @param viewType
         * @return ViewHolder
         */
        @Override
        public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Create a new list item inflating the layout

            View view = TaskListFragment.this.getActivity().getLayoutInflater().inflate(R.layout.list_item_task, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        /**
         * Replace the contents of a list item when it is called from the LayoutManager
         *
         * @param holder:   ViewHolder containing the views
         * @param position: position from the Dataset to be showed
         */
        @Override
        public void onBindViewHolder(final TaskAdapter.ViewHolder holder, final int position) {
            final Task task = mTaskList.get(position);
            if (task.getCurrentNotificationsCount() > 0) {
                holder.mAvatarView.setImageResource(R.drawable.ic_alarm_on);
            } else {
                holder.mAvatarView.setImageResource(R.drawable.ic_alarm_off);
            }
            holder.mIconView.setImageResource(R.drawable.ic_check_off);
            holder.mTextPrimaryText.setText(task.name);
            if (task.targetDateTime != null) {
                holder.mTextSecondaryText.setVisibility(View.VISIBLE);
                if (mTypeTask == Task.TypeTask.All) {
                    holder.mTextSecondaryText.setText(task.targetDateTime.getDisplayFormatWithToday(getActivity()));
                } else {
                    holder.mTextSecondaryText.setText(task.targetDateTime.getDisplayFormat(getActivity()));
                }
            } else {
                holder.mTextSecondaryText.setVisibility(View.GONE);
            }
            if (isSelected(position)) {
                holder.itemView.setBackgroundResource(R.drawable.list_row_background_selected);
                holder.mLayoutPrimaryAction.setBackgroundResource(R.drawable.list_row_background_selected);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.list_row_background);
                holder.mLayoutPrimaryAction.setBackgroundResource(R.drawable.list_row_background);
            }
            if (position == mShowRowPosition) {
                animateRowEmergence(holder.itemView);
                //After use it, reset values for avoiding using them again
                initPositionValues();
            }
        }

        /**
         * Called to know the total number of items in the data set hold by the adapter
         *
         * @return the total number of items
         */
        @Override
        public int getItemCount() {
            return mTaskList.size();
        }

        @Override
        public long getItemId(int position) {
            if ((mTaskList != null) && (position < mTaskList.size())) {
                return mTaskList.get(position)._id;
            } else {
                return super.getItemId(position);
            }
        }

        /**
         * Animate a view to emergence
         *
         * @param view it is the row to animate
         */
        private void animateRowEmergence(final View view) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(1000)
                    .start();
        }

        /**
         * Get the rows selected on the list
         *
         * @return
         */
        public int getSelectedCount() {
            return mSelectedCountRef;
        }

        /**
         * Return if a task is selected or not
         *
         * @param position task list position
         * @return true or false
         */
        public boolean isSelected(int position) {
            if (mTasksSelected != null) {
                return mTasksSelected.get(position);
            } else {
                return false;
            }
        }

        /**
         * Swap the value of the task selected to not selected and vice versa
         *
         * @param position task list position
         */
        public void toggle(int position) {
            if (mTasksSelected == null) {
                mTasksSelected = new SparseBooleanArray();
            }
            if (isSelected(position)) {
                mTasksSelected.delete(position);
                mSelectedCountRef--;
            } else {
                mSelectedCountRef++;
                mTasksSelected.put(position, true);
            }
        }

        /**
         * Get the list of task selected
         *
         * @return
         */
        public List<Task> getSelectedList() {
            List<Task> result = null;
            if (mTasksSelected != null) {
                int position = 0;
                for (Task task : mTaskList) {
                    if (mTasksSelected.get(position)) {
                        if (result == null) {
                            result = new ArrayList<>();
                        }
                        result.add(task);
                    }
                    position++;
                }
            }
            return result;
        }

        /**
         * Fill the SparseBooleanArray with the tasks selected in selectedList
         *
         * @param selectedList subset of selected tasks
         */
        public void setSelectedList(ArrayList<Task> selectedList) {
            int position = 0;
            if (mTasksSelected == null) {
                mTasksSelected = new SparseBooleanArray();
            }
            for (Task task : mTaskList) {
                if (selectedList.contains(task)) {
                    mTasksSelected.put(position, true);
                    mSelectedCountRef++;
                }
                position++;
            }
            selectedList.clear();
            selectedList = null;
        }

        /**
         * Remove all items from selected state
         */
        public void resetSelected() {
            if (mSelectedCountRef > 0) {
                mTasksSelected = null;
                mSelectedCountRef = 0;
                notifyDataSetChanged();
            }
        }

    }

    /**
     * Item decoration for recycler view
     */
    public class TaskItemDecoration extends android.support.v7.widget.RecyclerView.ItemDecoration {

        Drawable mDivider;

        public TaskItemDecoration() {
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

    /**
     * Make a delete operation for a list of task in an asynchronous way
     */
    private class AsyncTaskList extends AsyncTask<List<Task>, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(List<Task>... params) {
            int deleteErrorCount = 0;
            if (params != null) {
                List<Task> taskList = params[0];
                if (taskList != null) {
                    UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                    for (Task task : taskList) {
                        if (!useCaseTask.deleteTask(task)) {
                            deleteErrorCount++;
                        }
                    }
                    if (deleteErrorCount == 0) {
                        return new Boolean(true);
                    } else {
                        return new Boolean(false);
                    }
                }
            }
            return new Boolean(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result.booleanValue()) {
                AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.error_title),
                        getString(R.string.unit_of_work_exception), null, getString(R.string.error_button_ok));
                alertDialogFragment.show(getFragmentManager(), "errorDialog");
            }
            if (mOnTaskListFragmentListener != null) {
                mOnTaskListFragmentListener.onTaskDeleted();
            }
            if (mActionBarActivated) {
                deactivateToolbarActions();
            }
        }
    }


}
