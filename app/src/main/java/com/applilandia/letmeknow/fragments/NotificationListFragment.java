package com.applilandia.letmeknow.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applilandia.letmeknow.R;
import com.applilandia.letmeknow.data.NotificationSet;
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

    private static final int LOADER_ID = 1;

    public interface OnNotificationListListener {
        /**
         * Trigger when a task has been selected
         * @param id task identifier of the selected task
         */
        public void onSelectedTask(int id);

        /**
         * Trigger when an item has been removed from List
         * @param count numbers of current items
         */
        public void onItemRemoved(int count);
    }

    private OnNotificationListListener mOnNotificationListListener;
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

        initRecyclerView();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * Init recycler view
     */
    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewNotifies);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new RecyclerNoficationsItemAnimator());
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
        return new TaskNotificationStatusLoader(getActivity(), Notification.TypeStatus.Sent);
    }

    @Override
    public void onLoadFinished(Loader<List<Task>> loader, List<Task> data) {
        if (loader.getId() == LOADER_ID) {
            mAdapter = new TaskAdapter(data);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Task>> loader) {
        mAdapter = null;
    }

    private class RecyclerNoficationsItemAnimator extends RecyclerView.ItemAnimator {

        @Override
        public void runPendingAnimations() {
        }

        @Override
        public boolean animateRemove(final RecyclerView.ViewHolder holder) {
            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final int originalHeight = holder.itemView.getHeight();

            ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(holder.itemView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewGroup.LayoutParams lp;
                    // Reset view presentation
                    holder.itemView.setAlpha(1f);
                    holder.itemView.setTranslationX(0);
                    lp = holder.itemView.getLayoutParams();
                    lp.height = originalHeight;
                    holder.itemView.setLayoutParams(lp);
                    dispatchRemoveFinished(holder);
                }
            });

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    lp.height = (Integer) valueAnimator.getAnimatedValue();
                    holder.itemView.setLayoutParams(lp);
                }
            });
            animator.start();
            return false;
        }

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            return false;
        }

        @Override
        public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
            return false;
        }

        @Override
        public void endAnimation(RecyclerView.ViewHolder item) {
        }

        @Override
        public void endAnimations() {
        }

        @Override
        public boolean isRunning() {
            return false;
        }
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
            private ImageView mImageViewCheck;
            private ImageView mImageClose;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextViewHeadLine = (TextView) itemView.findViewById(R.id.textViewHeadline);
                mTextViewBody = (TextView) itemView.findViewById(R.id.textViewBody);
                mImageViewNotifications = (ImageView) itemView.findViewById(R.id.imageViewNotifications);
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
                        mTaskList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeRemoved(position, mTaskList.size());
                        if (mOnNotificationListListener != null) {
                            //Notify one item has been removed from the list
                            mOnNotificationListListener.onItemRemoved(mTaskList.size());
                        }
                    }
                });
                holder.mImageViewCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mImageViewCheck.setImageResource(R.drawable.ic_check_on);
                        SnackBar snackBar = (SnackBar) getActivity().findViewById(R.id.snackBarNotifications);
                        snackBar.setOnSnackBarListener(new SnackBar.OnSnackBarListener() {
                            @Override
                            public void onClose() {
                                UseCaseTask useCaseTask = new UseCaseTask(getActivity());
                                if (useCaseTask.setTaskAsCompleted(task)) {
                                    //Remove it from Recycler View
                                    mTaskList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeRemoved(position, mTaskList.size());
                                    if (mOnNotificationListListener != null) {
                                        //Notify an item has been removed
                                        mOnNotificationListListener.onItemRemoved(mTaskList.size());
                                    }
                                }
                            }

                            @Override
                            public void onUndo() {

                            }
                        });
                        snackBar.show(R.string.snack_bar_task_completed_text);
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
