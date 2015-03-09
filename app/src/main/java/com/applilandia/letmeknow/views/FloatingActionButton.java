package com.applilandia.letmeknow.views;

import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;


/**
 * Created by JuanCarlos on 28/02/2015.
 */
public class FloatingActionButton extends ImageView {

    private Context mContext;

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Set stateListAnimator only if the version is Lollipop or greater
            setStateListAnimator();
        }
    }

    /**
     * Create a stateListAnimator for view pressed and normal state
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStateListAnimator() {
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(new int[]{android.R.attr.state_pressed},
                createLollipopAnimator(120));
        stateListAnimator.addState(new int[]{}, createLollipopAnimator(0));
        setStateListAnimator(stateListAnimator);
    }

    /**
     * Handler for click event
     * @param l
     */
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(l);
    }

    /**
     * Touch gesture
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startAnimator(0.5f);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startAnimator(1f);
                }

                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * Create elevation animation for Lollipop version
     * @param translationZ Pixels to elevate
     * @return Object animator
     */
    private ObjectAnimator createLollipopAnimator(int translationZ) {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setPropertyName("translationZ");
        objectAnimator.setFloatValues(translationZ);
        return objectAnimator;
    }

    /**
     * Create an animation for set an alpha value
     * @param targetValue alpha to value
     */
    private void startAnimator(float targetValue) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "alpha", targetValue);
        objectAnimator.setEvaluator(new FloatEvaluator());
        objectAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
    }

}
