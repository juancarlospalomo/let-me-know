package com.applilandia.letmeknow.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
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
        public void onTaskSelected(int id);

        public void onTaskLongPressed();
    }

    //LoaderId for task loader
    private static final int TASK_LOADER_ID = 1;
    //Variables to state the init recyclerview scroll
    private int mShowRowPosition = -1;
    private int mShowRowTask = -1; //Task Id of the row
    //ActionBar status
    private boolean mActionBarActived = false;
    //Type task to be loaded in the fragment
    private Task.TypeTask mTypeTask;
    private ProgressBar mProgressBar;
    private RecyclerView mTaskRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TaskAdapter mAdapter;
    private OnTaskListFragmentListener mOnTaskListFragmentListener;

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

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressActivityTask);
        initRecyclerView();
        //Init loader.  Data will be set to Adapter in onLoadFinished()
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
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
                        if (!mActionBarActived) {
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
                                }
                            }
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
        mTaskRecyclerView.setOnTouchListener(new RecyclerViewMotion(mTaskRecyclerView,
                new RecyclerViewMotion.OnRecyclerViewMotion() {
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
                                                mAdapter.mTaskList.remove(position);
                                                mAdapter.notifyDataSetChanged();
                                                mAdapter.toggle(position);
                                                AsyncTaskList asyncTaskList = new AsyncTaskList();
                                                asyncTaskList.execute(mAdapter.getSelectedList());
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
                }));

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mActionBarActived) {
            inflater.inflate(R.menu.menu_task_list, menu);
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
        mActionBarActived = true;
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Deactivate the action bar to remove the actions
     */
    public void deactivateToolbarActions() {
        mActionBarActived = false;
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
        if (loader.getId() == TASK_LOADER_ID) {
            if (data != null) {
                mAdapter = new TaskAdapter(data);
                mTaskRecyclerView.setAdapter(mAdapter);
                if (mShowRowTask != -1) {
                    convertTaskIdToPosition();
                    mTaskRecyclerView.scrollToPosition(mShowRowPosition);
                }
            }
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
        public TaskAdapter(List<Task> data) {
            this.mTaskList = data;
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
                holder.mTextSecondaryText.setText(task.targetDateTime.getDisplayFormat(getActivity()));
            } else {
                holder.mTextSecondaryText.setVisibility(View.GONE);
            }
            holder.mIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mIconView.setImageResource(R.drawable.ic_check_on);
                    SnackBar snackBar = (SnackBar) getActivity().findViewById(R.id.snackBarTasks);
                    snackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                        @Override
                        public void onClose() {
                            UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                            if (useCaseTask.setTaskAsCompleted(task)) {
                                //Remove it from Recycler View
                                mTaskList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, mTaskList.size());
                            }
                        }
                    });
                    snackBar.show(R.string.snack_bar_task_completed_text);
                }
            });
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
            view.animate().alpha(1f).setDuration(3000)
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
            return mTasksSelected.get(position);
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
            return result;
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
                        return new Boolean(false);
                    } else {
                        return new Boolean(true);
                    }
                }
            }
            return new Boolean(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mAdapter.notifyDataSetChanged();
            if (mActionBarActived) {
                deactivateToolbarActions();
            }
        }
    }


}
