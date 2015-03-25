package com.applilandia.letmeknow.listeners;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Created by JuanCarlos on 16/03/2015.
 */
public class RecyclerViewMotion implements View.OnTouchListener {

    private final static String LOG_TAG = RecyclerViewMotion.class.getSimpleName();

    public interface OnRecyclerViewMotion {
        public boolean canDismiss(int position);

        public void onDismiss(View view, int position);
    }


    //Width of Recycler View
    private int mViewWidth = 1;
    //Raw position in the screen of the pressed point
    private float mDownX;
    //Position of the view in the recycler view adapter
    private int mDownPosition;
    //User is doing swiping on view or not
    private boolean mSwiping = false;
    //Pixels number to be considered a movement of the pointer
    private int mSlop;
    //View was pressed
    private View mDownView = null;
    private RecyclerView mRecyclerView;
    private OnRecyclerViewMotion mOnRecyclerViewMotion;

    public RecyclerViewMotion(RecyclerView recyclerView, OnRecyclerViewMotion l) {
        mRecyclerView = recyclerView;
        mOnRecyclerViewMotion = l;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(recyclerView.getContext());
        mSlop = viewConfiguration.getScaledTouchSlop();
    }

    /**
     * Get if point pressed are inside a view in recycler view
     *
     * @param event
     * @return view whether the pressed point belongs to a view or null if it isn't
     */
    private View getViewPressed(MotionEvent event) {
        int[] listCoordinates = new int[2];
        mRecyclerView.getLocationOnScreen(listCoordinates);
        int x = (int) event.getRawX() - listCoordinates[0];
        int y = (int) event.getRawY() - listCoordinates[1];

        Rect rect = new Rect();
        int childCount = mRecyclerView.getChildCount();
        View viewChild = null;
        for (int index = 0; index < childCount; index++) {
            //Try to find the view looping through all children
            viewChild = mRecyclerView.getChildAt(index);
            viewChild.getHitRect(rect);
            if (rect.contains(x, y)) {
                return viewChild;
            }
        }
        return viewChild;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.getWidth();
        }
        int actionId = event.getActionMasked();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:
                mDownView = getViewPressed(event);
                if (mDownView != null) {
                    mDownX = event.getRawX();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                }
                break;

            case MotionEvent.ACTION_UP:
                float deltaX = event.getRawX() - mDownX;
                boolean dismiss = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    if (mDownPosition != ListView.INVALID_POSITION) {
                        dismiss = mOnRecyclerViewMotion.canDismiss(mDownPosition);
                    }
                }
                if (dismiss) {
                    final int position = mDownPosition;
                    final View downView = mDownView; //mDownView gets null before animation ends
                    mDownView.animate()
                            .translationX(mViewWidth)
                            .alpha(0)
                            .setDuration(mRecyclerView.getContext().getResources()
                                    .getInteger(android.R.integer.config_shortAnimTime))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mDownPosition = ListView.INVALID_POSITION;
                                    if (mOnRecyclerViewMotion != null) {
                                        mOnRecyclerViewMotion.onDismiss(downView, position);
                                    }
                                }
                            })
                            .start();
                } else {
                    if (mDownView != null) {
                        mDownView.animate()
                                .translationX(0)
                                .setDuration(mRecyclerView.getContext().getResources()
                                        .getInteger(android.R.integer.config_shortAnimTime))
                                .start();
                    }
                }
                mDownX = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mDownView == null) {
                    break;
                }
                deltaX = event.getRawX() - mDownX;
                int swipingSlop = 0;
                if (deltaX > mSlop) {
                    mSwiping = true;
                    swipingSlop = (deltaX > 0) ? mSlop : -mSlop;
                    mRecyclerView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mRecyclerView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    mDownView.setTranslationX(deltaX - swipingSlop);
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mRecyclerView.getResources().getInteger(android.R.integer.config_shortAnimTime))
                            .setListener(null);
                }
                mDownX = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
        }

        return false;
    }

/*
    public void performDismiss(final RecyclerView recyclerView, final View dismissView, final int dismissPosition) {

        if (dismissView != null) {
            AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(recyclerView.getResources().getString(R.string.delete_task_dialog_title),
                    "", recyclerView.getResources().getString(R.string.delete_task_dialog_cancel_text),
                    recyclerView.getResources().getString(R.string.delete_task_dalog_ok_text));
            alertDialog.setButtonOnClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialogFragment.INDEX_BUTTON_YES) {
                        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
                        final int originalHeight = dismissView.getHeight();

                        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(dismissView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));

                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mDownPosition = ListView.INVALID_POSITION;
                                ViewGroup.LayoutParams lp;
                                // Reset view presentation
                                dismissView.setAlpha(1f);
                                dismissView.setTranslationX(0);
                                lp = dismissView.getLayoutParams();
                                lp.height = originalHeight;
                                dismissView.setLayoutParams(lp);
                                mOnRecyclerViewMotion.onDismiss(dismissPosition);
                                // Send a cancel event
                                long time = SystemClock.uptimeMillis();
                                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                                recyclerView.dispatchTouchEvent(cancelEvent);
                            }
                        });

                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                lp.height = (Integer) valueAnimator.getAnimatedValue();
                                dismissView.setLayoutParams(lp);
                            }
                        });
                        animator.start();

                    }
                }
            });
        }
    }
*/


}
