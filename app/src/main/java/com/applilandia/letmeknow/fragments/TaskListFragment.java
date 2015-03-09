package com.applilandia.letmeknow.fragments;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.loaders.TaskLoader;
import com.applilandia.letmeknow.models.Task;

import java.util.List;

/**
 * Created by JuanCarlos on 27/02/2015.
 */
public class TaskListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    //For logging purpose
    private final static String LOG_TAG = TaskListFragment.class.getSimpleName();

    //LoaderId for task loader
    private static final int TASK_LOADER_ID = 1;
    //Type task to be loaded in the fragment
    private Task.TypeTask mTypeTask;
    private ProgressBar mProgressBar;
    private RecyclerView mTaskRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TaskAdapter mAdapter;

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
        View view = getView();
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressActivityTask);
        mTaskRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewTasks);
        //Change in content will not change the layout size of the recycler view
        //Of this way, we improve the performance
        mTaskRecyclerView.setHasFixedSize(true);
        //It will use a LinearLayout
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTaskRecyclerView.setLayoutManager(mLayoutManager);
        mTaskRecyclerView.addItemDecoration(new TaskItemDecoration());
        //Init loader.  Data will be set to Adapter in onLoadFinished()
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);
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
     * Adapter for task.  It use the recyclerview adapter
     */
    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

        //Dataset for the Adapter
        private List<Task> mTaskList;

        /**
         * View Holder pattern
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView mAvatarView;
            private TextView mTextPrimaryText;
            private TextView mTextSecondaryText;
            private ImageView mIconView;

            public ViewHolder(View itemView) {
                super(itemView);
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
        public void onBindViewHolder(TaskAdapter.ViewHolder holder, int position) {
            Task task = mTaskList.get(position);
            if (task.getCurrentNotificationsCount() > 0) {
                holder.mAvatarView.setImageResource(R.drawable.ic_alarm_on);
            } else {
                holder.mAvatarView.setImageResource(R.drawable.ic_alarm_off);
            }
            holder.mIconView.setImageResource(R.drawable.ic_check_off);
            holder.mTextPrimaryText.setText(task.name);
            if (task.targetDateTime != null) {
                holder.mTextSecondaryText.setVisibility(View.GONE);
                holder.mTextSecondaryText.setText(task.targetDateTime.getDisplayFormat(getActivity()));
            } else {
                holder.mTextPrimaryText.setVisibility(View.GONE);
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

    }

    public class TaskItemDecoration extends RecyclerView.ItemDecoration {

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


}
