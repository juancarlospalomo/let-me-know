package com.applilandia.letmeknow.loaders;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.applilandia.letmeknow.data.TaskContract;
import com.applilandia.letmeknow.models.Summary;
import com.applilandia.letmeknow.usecases.UseCaseTask;

import java.util.List;

/**
 * Created by JuanCarlos on 24/02/2015.
 * Async Task Loader for getting the tasks summary
 */
public class SummaryLoader extends AsyncTaskLoader<List<Summary>> {

    private static final String LOG_TAG = SummaryLoader.class.getSimpleName();

    private Context mContext;
    private List<Summary> mSummaryList;
    private ContentObserver mObserver; //Observer to be notified when a change occurs

    public SummaryLoader(Context context) {
        super(context);
        mContext = context;
        mObserver = new TaskObserver(new Handler());
        mContext.getContentResolver().registerContentObserver(TaskContract.TaskEntry.CONTENT_URI,
                true, mObserver);
    }

    /**
     * Async Task Loader
     * @return Summary List of Tasks
     */
    @Override
    public List<Summary> loadInBackground() {
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        List<Summary> result = useCaseTask.getTaskSummary();
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it
     * @param data list to be delivered
     */
    @Override
    public void deliverResult(List<Summary> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            if (data!=null) {
                onReleaseResources(data);
            }
            return;
        }
        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<Summary> oldList = mSummaryList;
        mSummaryList = data;
        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }
        // Invalidate the old data as we don't need it any more
        if (oldList!=null && oldList!=data) {
            mSummaryList = null;
        }
    }

    /**
     * Handle the request to start the loader
     */
    @Override
    protected void onStartLoading() {
        if (mSummaryList != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mSummaryList);
        }
        if (takeContentChanged() || mSummaryList == null) {
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
        if (mSummaryList != null) {
            onReleaseResources(mSummaryList);
        }
    }

    /**
     * Manage the request to cancel the loader
     * @param data List of Tasks loaded
     */
    @Override
    public void onCanceled(List<Summary> data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(mSummaryList);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        onReleaseResources(data);
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Summary> data) {
        data.clear();
        mSummaryList = null;
        //Unregister the observer to avoid GC doesn't eliminate the class object
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    private final class TaskObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public TaskObserver(Handler handler) {
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
