package com.applilandia.letmeknow.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 09/03/2015.
 */
public class SnackBar extends LinearLayout {

    public interface OnSnackBarListener {
        public void onClose();
    }

    private OnSnackBarListener mOnSnackBarListener;
    private TextView mText;
    private TextView mActionText;

    public SnackBar(Context context) {
        this(context, null);
    }

    public SnackBar(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.snackBarStyle);

        LinearLayout.LayoutParams layoutParams = new LayoutParams(context, attrs);
        //Layout_Gravity
        layoutParams.gravity = Gravity.BOTTOM|Gravity.START;
        setLayoutParams(layoutParams);
        //Gravity
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);
        setVisibility(View.GONE);
        //Add text and action views
        createSnackBarText(context);
        createSnackBarAction(context);
    }


    private void createSnackBarText(Context context) {
        mText = new TextView(context, null, R.attr.snackBarText);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mText.setLayoutParams(layoutParams);
        mText.setGravity(Gravity.CENTER);
        addView(mText);
    }

    private void createSnackBarAction(Context context) {
        mActionText = new TextView(context, null, R.attr.snackBarAction);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mActionText.setLayoutParams(layoutParams);
        mActionText.setPadding(getPixels(40), 0, 0, 0);
        mActionText.setGravity(Gravity.CENTER);
        mActionText.setText(R.string.snack_bar_action_text);
        addView(mActionText);
    }

    public void show(int messageResId) {
        mText.setText(messageResId);
        setVisibility(View.VISIBLE);
        setAlpha(1);
        animate().setDuration(5000)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hide();
                        if (mOnSnackBarListener != null) {
                            mOnSnackBarListener.onClose();
                        }
                    }
                }).start();
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void setOnSnackBarListener(OnSnackBarListener l) {
        mOnSnackBarListener = l;
    }

    private int getPixels(int dpValue) {
        DisplayMetrics metrics;
        metrics = getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue);
    }
}
