package com.applilandia.letmeknow.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by JuanCarlos on 16/03/2015.
 */
public class RecyclerViewClickListener implements RecyclerView.OnItemTouchListener {

    private final static String LOG_TAG = RecyclerViewClickListener.class.getSimpleName();

    public interface RecyclerViewOnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    private View mView; //View touched
    private int mPosition; //Position of the view inside the recycler view
    private RecyclerViewOnItemClickListener mListener; //Listener for item events
    private GestureDetector mGestureDetector;

    public RecyclerViewClickListener(Context context, final RecyclerViewOnItemClickListener mListener) {
        this.mListener = mListener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mListener.onItemClick(mView, mPosition);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                mListener.onItemLongClick(mView, mPosition);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent event) {
        mView = recyclerView.findChildViewUnder(event.getX(), event.getY());
        if ((mView != null) && (mListener != null)) {
            mPosition = recyclerView.getChildPosition(mView);
            Log.v(LOG_TAG, String.valueOf(mPosition));
            return mGestureDetector.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }
}
