package com.applilandia.letmeknow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.cross.Message;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.loaders.TaskLoader;
import com.applilandia.letmeknow.loaders.TaskNotificationStatusLoader;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;
import com.applilandia.letmeknow.views.SnackBar;

import java.util.List;

/**
 * Created by JuanCarlos on 17/03/2015.
 */
public class NotificationListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Task>> {

    private final static String LOG_TAG = NotificationListFragment.class.getSimpleName();
    //State keys
    private final static String KEY_LIST_FIRST_POSITION = "firstPosition";
    private final static String KEY_FIRST_POSITION_OFFSET = "firstPositionOffset";
    private final static String KEY_DAILY_SOURCE = "dailySource";

    private static final int LOADER_ID = 1;

    public interface OnNotificationListListener {
        /**
         * Trigger when a task has been selected
         *
         * @param id task identifier of the selected task
         */
        public void onSelectedTask(int id);

        /**
         * Trigger when an item has been removed from List
         *
         * @param count number of current items
         */
        public void onItemRemoved(int count);

        /**
         * Trigger when the list has not any more element
         */
        public void onListEmpty();
    }

    private OnNotificationListListener mOnNotificationListListener;
    //State instance variables
    private int mFirstVisiblePosition = ListView.INVALID_POSITION;
    private int mFirstPositionOffset;
    private boolean mDeliveredData = false;
    //Flag source daily
    private boolean mSourceDaily = false;
    private SnackBar mSnackBar;
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mFirstVisiblePosition = savedInstanceState.getInt(KEY_LIST_FIRST_POSITION);
            mFirstPositionOffset = savedInstanceState.getInt(KEY_FIRST_POSITION_OFFSET);
            mSourceDaily = savedInstanceState.getBoolean(KEY_DAILY_SOURCE);
        }

        mSnackBar = (SnackBar) getView().findViewById(R.id.snackBarNotifications);
        initRecyclerView();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFirstVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        View view = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findViewByPosition(mFirstVisiblePosition);
        if (view != null) {
            mFirstPositionOffset = view.getTop();
        }
        outState.putInt(KEY_LIST_FIRST_POSITION, mFirstVisiblePosition);
        outState.putInt(KEY_FIRST_POSITION_OFFSET, mFirstPositionOffset);
        outState.putBoolean(KEY_DAILY_SOURCE, mSourceDaily);
    }

    /**
     * Set if the fragment has be show the tasks based on the notifications or on the day tasks
     *
     * @param value daily source = true
     */
    public void setDailySource(boolean value) {
        mSourceDaily = value;
    }

    /**
     * Init recycler view
     */
    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewNotifies);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Set the event listener for the fragment
     *
     * @param l listener
     */
    public void setOnNotificationListListener(OnNotificationListListener l) {
        mOnNotificationListListener = l;
    }

    @Override
    public Loader<List<Task>> onCreateLoader(int id, Bundle args) {
        if (!mSourceDaily) {
            return new TaskNotificationStatusLoader(getActivity(), Notification.TypeStatus.Sent, false);
        } else {
            return new TaskLoader(getActivity(), Task.TypeTask.Today);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == LOADER_ID) {
            if ((data != null) && (data.size() > 0)) {
                if (mFirstVisiblePosition != ListView.INVALID_POSITION) {
                    if (!mDeliveredData) {
                        mAdapter = new TaskAdapter(data);
                        mRecyclerView.setAdapter(mAdapter);
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(mFirstVisiblePosition, mFirstPositionOffset);
                    } else {
                        mFirstVisiblePosition = ListView.INVALID_POSITION;
                    }
                } else {
                    mAdapter = new TaskAdapter(data);
                    mRecyclerView.setAdapter(mAdapter);
                }
            } else {
                if (mOnNotificationListListener != null) {
                    mOnNotificationListListener.onListEmpty();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        mAdapter = null;
    }

    /**
     * Adapter for Recycler View with cards
     */
    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

        private List<com.applilandia.letmeknow.models.Task> mTaskList;

        public TaskAdapter(List<com.applilandia.letmeknow.models.Task> data) {
            this.mTaskList = data;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTextViewHeadLine;
            private TextView mTextViewBody;
            private ImageView mImageViewNotifications;
            private ImageView mImageActionShare;
            private ImageView mImageViewCheck;
            private ImageView mImageClose;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextViewHeadLine = (TextView) itemView.findViewById(R.id.textViewHeadline);
                mTextViewBody = (TextView) itemView.findViewById(R.id.textViewBody);
                mImageViewNotifications = (ImageView) itemView.findViewById(R.id.imageViewNotifications);
                mImageActionShare = (ImageView) itemView.findViewById(R.id.imageViewActionShare);
                mImageViewCheck = (ImageView) itemView.findViewById(R.id.imageViewCheck);
                mImageClose = (ImageView) itemView.findViewById(R.id.imageViewClose);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.card_notification_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Task task = mTaskList.get(position);
            if (task != null) {
                if (task.typeTask == Task.TypeTask.Expired) {
                    holder.mTextViewHeadLine.setText(getResources().getStringArray(R.array.text_tile_content)[0]);
                    holder.mTextViewHeadLine.setBackgroundColor(getColor(0));
                }
                if (task.typeTask == Task.TypeTask.Today) {
                    holder.mTextViewHeadLine.setText(getResources().getStringArray(R.array.text_tile_content)[1]);
                    holder.mTextViewHeadLine.setBackgroundColor(getColor(1));
                }
                if (task.typeTask == Task.TypeTask.Future) {
                    holder.mTextViewHeadLine.setText(getResources().getStringArray(R.array.text_tile_content)[2]);
                    holder.mTextViewHeadLine.setBackgroundColor(getColor(2));
                }
                String textBody = task.name;
                if ((task.targetDateTime != null) && (!task.targetDateTime.isNull())) {
                    textBody += "\n" + task.targetDateTime.getDisplayFormat(getActivity());
                }
                holder.mTextViewBody.setText(textBody);
                if (task.getCurrentNotificationsCount() > 0) {
                    holder.mImageViewNotifications.setImageResource(R.drawable.ic_alarm_on);
                } else {
                    holder.mImageViewNotifications.setImageResource(R.drawable.ic_alarm_off);
                }
                holder.mImageClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NotificationSet notificationSet = new NotificationSet(getActivity());
                        notificationSet.deleteSentNotification(task._id);
                        mTaskList.remove(task);
                        notifyItemRangeRemoved(position, 1);
                        if (mOnNotificationListListener != null) {
                            //Notify one item has been removed from the list
                            mOnNotificationListListener.onItemRemoved(mTaskList.size());
                        }
                    }
                });
                holder.mImageActionShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        Message message = new Message(getActivity());
                        intent.putExtra(Intent.EXTRA_TEXT, message.getFormattedTaskMessage(task));
                        startActivity(intent);
                    }
                });
                holder.mImageViewCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSnackBar.getVisibility() == View.GONE) {
                            holder.mImageViewCheck.setImageResource(R.drawable.ic_check_on);
                            mSnackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                                @Override
                                public void onClose() {
                                    UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                                    if (useCaseTask.setTaskAsCompleted(task)) {
                                        //Remove it from Recycler View
                                        mTaskList.remove(task);
                                        notifyItemRangeRemoved(position, 1);
                                        if (mOnNotificationListListener != null) {
                                            //Notify an item has been removed
                                            mOnNotificationListListener.onItemRemoved(mTaskList.size());
                                        }
                                    }
                                }

                                @Override
                                public void onUndo() {
                                    mSnackBar.undo();
                                    holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                                }
                            });
                            mSnackBar.show(R.string.snack_bar_task_completed_text);
                        } else {
                            mSnackBar.undo();
                            holder.mImageViewCheck.setImageResource(R.drawable.ic_check_off);
                        }
                    }
                });
                holder.mImageViewNotifications.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnNotificationListListener != null) {
                            mOnNotificationListListener.onSelectedTask((int) getItemId(position));
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (mTaskList != null) {
                return mTaskList.size();
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (mTaskList != null) {
                Task task = mTaskList.get(position);
                if (task != null) {
                    return task._id;
                }
            }
            return 0;
        }

        /**
         * Returns the color according to the required tile position
         *
         * @param position position in the adapter
         * @return color
         */
        private int getColor(int position) {
            switch (position) {
                case 1:
                    return getResources().getColor(R.color.tile_today_content_background);
                case 2:
                    return getResources().getColor(R.color.tile_future_content_background);
                default:
                    return getResources().getColor(R.color.tile_expired_content_background);
            }
        }

    }


}
