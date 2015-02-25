package com.applilandia.letmeknow.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;
import java.util.List;

/**
 * Created by JuanCarlos on 24/02/2015.
 * Async Task Loader for getting the tasks summary
 */
public class SummaryLoader extends AsyncTaskLoader<List<Task>> {

    private Context mContext;
    private List<Task> mTaskList;
    private ForceLoadContentObserver mObserver; //Observer to be notified when a change occurs

    public SummaryLoader(Context context) {
        super(context);
        mContext = context;
        mObserver = new ForceLoadContentObserver();
    }

    /**
     * Async Task Loader
     * @return Summary List of Tasks
     */
    @Override
    public List<Task> loadInBackground() {
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Task> result = useCaseTask.getTaskSummary();
        if (result!=null) {
            mContext.getContentResolver().registerContentObserver(TaskContract.TaskEntry.CONTENT_URI,
                    true, mObserver);
        }
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it
     * @param data list to be delivered
     */
    @Override
    public void deliverResult(List<Task> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            if (data!=null) {
                onReleaseResources(data);
            }
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Task> oldList = mTaskList;
        mTaskList = data;
        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }
        // Invalidate the old data as we don't need it any more
        if (oldList!=null && oldList!=data) {
            onReleaseResources(oldList);
        }
    }

    /**
     * Handle the request to start the loader
     */
    @Override
    protected void onStartLoading() {
        if (mTaskList != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mTaskList);
        }
        if (takeContentChanged() || mTaskList == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
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

    /**
     * Manages the request to reset the loader
     */
    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();
        // At this point we can release the resources.
        if (mTaskList != null) {
            onReleaseResources(mTaskList);
        }
    }

    /**
     * Manage the request to cancel the loader
     * @param data List of Tasks loaded
     */
    @Override
    public void onCanceled(List<Task> data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(mTaskList);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        onReleaseResources(data);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Task> data) {
        data.clear();
        mTaskList = null;
    }

}
