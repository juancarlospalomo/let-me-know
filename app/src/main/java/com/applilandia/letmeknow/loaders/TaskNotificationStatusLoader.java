package com.applilandia.letmeknow.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;

import java.util.List;

/**
 * Created by JuanCarlos on 17/03/2015.
 */
public class TaskNotificationStatusLoader extends AsyncTaskLoader<List<Task>> {

    private Notification.TypeStatus mTypeStatus;
    private Context mContext;
    private List<Task> mTaskList;
    private NotificationObserver mObserver;

    public TaskNotificationStatusLoader(Context context, Notification.TypeStatus typeStatus) {
        super(context);
        mContext = context;
        mTypeStatus = typeStatus;
        mObserver = new NotificationObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(TaskContract.NotificationEntry.CONTENT_URI,
                true, mObserver);
    }

    @Override
    public List<Task> loadInBackground() {
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        mTaskList = useCaseTask.getTasksByNotificationStatus(mTypeStatus);
        return mTaskList;
    }

    @Override
    public void deliverResult(List<Task> data) {
        if (isReset()) {
            onReleaseResources(data);
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Task> oldList = mTaskList;
        mTaskList = data;
        if (isStarted()) {
            //Deliver result to the client as the loader is in started state
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mTaskList != null) {
            deliverResult(mTaskList);
        }
        if (takeContentChanged() || mTaskList == null) {
            //If the content changed (we know this by the Observer) or
            //data have not been loaded yet, start the load
            forceLoad();
        }
    }

    /**
     * Manage the request to stop the loader
     */
    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Task> data) {
        //Attempt to cancel the asynchronous load
        super.onCanceled(data);
        //Release resources
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        //ensure the loader has been stopped
        onStopLoading();
        onReleaseResources(mTaskList);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Task> data) {
        if (data!=null) {
            data.clear();
        }
        //Unregister the observer to avoid GC doesn't eliminate the class object
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    private final class NotificationObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public NotificationObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            onContentChanged();
        }
    }


}
