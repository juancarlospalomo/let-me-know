package com.applilandia.letmeknow.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 09/03/2015.
 */
public class SnackBar extends RelativeLayout {

    private final static String LOG_TAG = SnackBar.class.getSimpleName();

    public interface OnSnackBarListener {
        public void onClose();

        public void onUndo();
    }

    private OnSnackBarListener mOnSnackBarListener;
    private boolean mUndo = false; //Save if the Undo action has been pressed
    private TextView mText;
    private TextView mActionText;

    public SnackBar(Context context) {
        this(context, null);
    }

    public SnackBar(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.snackBarStyle);

        RelativeLayout.LayoutParams layoutParams = new LayoutParams(context, attrs);
        setLayoutParams(layoutParams);
        setVisibility(View.GONE);
        //Add text and action views
        createSnackBarText(context);
        createSnackBarAction(context);
    }

    /**
     * Create the TextView to show the Text for snack bar
     *
     * @param context
     */
    private void createSnackBarText(Context context) {
        mText = new TextView(context, null, R.attr.snackBarText);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mText.setLayoutParams(layoutParams);
        mText.setGravity(Gravity.CENTER);
        addView(mText);
    }

    /**
     * Create the TextView for the action
     *
     * @param context
     */
    private void createSnackBarAction(Context context) {
        mActionText = new TextView(context, null, R.attr.snackBarAction);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mActionText.setLayoutParams(layoutParams);
        mActionText.setPadding(getPixels(40), 0, 0, 0);
        mActionText.setGravity(Gravity.CENTER);
        mActionText.setText(R.string.snack_bar_action_text);
        mActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSnackBarListener != null) {
                    mUndo = true;
                    mOnSnackBarListener.onUndo();
                    hide();
                }
            }
        });
        addView(mActionText);
    }

    /**
     * Show snack bar
     *
     * @param messageResId message resource to show as the text
     */
    public void show(int messageResId) {
        //When the snack bar is showed, reset the undo value
        mUndo = false;
        mText.setText(messageResId);
        setVisibility(View.VISIBLE);
        setAlpha(1);

        animate().setDuration(2000)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //To avoid this event is called twice in Android v4.x
                        if ((!mUndo) && (getVisibility() == View.VISIBLE)) {
                            hide();
                            if (mOnSnackBarListener != null) {
                                mOnSnackBarListener.onClose();
                            }
                        }
                    }
                }).start();
    }

    /**
     * Hide the snack bar
     */
    public void hide() {
        setVisibility(View.GONE);
        animate().cancel();
    }

    /**
     * Set the action as Undone
     */
    public void undo() {
        mUndo = true;
        hide();
    }

    /**
     * Set the listener
     *
     * @param l
     */
    public void setOnSnackBarListener(OnSnackBarListener l) {
        mOnSnackBarListener = l;
    }

    /**
     * Convert dp´s to pixels
     *
     * @param dpValue
     * @return
     */
    private int getPixels(int dpValue) {
        DisplayMetrics metrics;
        metrics = getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue);
    }
}
