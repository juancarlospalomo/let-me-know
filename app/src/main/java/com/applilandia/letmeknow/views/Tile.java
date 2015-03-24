package com.applilandia.letmeknow.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applilandia.letmeknow.R;

/**
 * Created by JuanCarlos on 13/02/2015.
 * This class build a layout in tile format.
 * It is made up of two sections: Content, where a big text can be showed and
 * Footer, where primary text and icon will be managed
 * It is useful for Grids, for instance
 */
public class Tile extends LinearLayout {

    //Context
    private Context mContext;
    //Tile content
    private Content mContent;
    //Tile footer
    private Footer mFooter;

    /**
     * default constructor
     *
     * @param context
     */
    public Tile(Context context) {
        this(context, null);
    }

    /**
     * Constructor with attributes
     *
     * @param context
     * @param attrs
     */
    public Tile(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.tileStyle);
        mContext = context;
        //Create Content layout
        mContent = new Content(mContext, attrs);
        //Create Footer layout
        mFooter = new Footer(mContext, attrs);
        setOrientation(LinearLayout.VERTICAL);
        addView(mContent);
        addView(mFooter);
    }

    /**
     * set the OnClickListener event handler for the Content
     *
     * @param l
     */
    public void setContentOnClickListener(OnClickListener l) {
        mContent.setOnClickListener(l);
    }

    /**
     * set the OnClickListener event handler for the text on the Footer
     *
     * @param l
     */
    public void setFooterTextOnClickListener(OnClickListener l) {
        mFooter.setTextOnClickListener(l);
    }

    /**
     * set the OnClickListener event handler for the icon on the Footer
     *
     * @param l
     */
    public void setIconOnClickListener(OnClickListener l) {
        mFooter.setIconOnClickListener(l);
    }

    /**
     * Method exposes the possibility of changing content background in runtime
     *
     * @param color color number
     */
    public void setContentBackgroundColor(int color) {
        mContent.setBackgroundColor(color);
    }

    /**
     * Get the current background color from the Content
     *
     * @return color number
     */
    public int getContentBackgroundColor() {
        int color = Color.TRANSPARENT;
        Drawable drawable = mContent.getBackground();
        if (drawable instanceof ColorDrawable) {
            color = ((ColorDrawable) drawable).getColor();
        }
        return color;
    }

    /**
     * Set the text for the content section
     *
     * @param text text to set
     */
    public void setContentText(String text) {
        mContent.setTextContent(text);
    }

    /**
     * Set background for content
     *
     * @param resId background resourceId
     */
    public void setContentBackground(int resId) {
        mContent.setBackgroundResource(resId);
    }

    /**
     * Method exposes the possibility of changing the primary text on footer in runtime
     *
     * @param text text to set
     */
    public void setFooterPrimaryLine(String text) {
        mFooter.setPrimaryLine(text);
    }

    /**
     * Method exposes the possibility of changing the secondary text on footer in runtime
     *
     * @param text text to set/
     */
    public void setFooterSecondaryLine(String text) {
        mFooter.setSecondaryLine(text);
    }

    /**
     * Change the icon in runtime
     *
     * @param resId resource identifier to set up
     */
    public void setFooterIcon(int resId) {
        mFooter.setIcon(resId);
    }

    /**
     * Content View of the Tile
     */
    private class Content extends FrameLayout implements OnClickListener {

        //TextView of the Content
        private TextView mTextContent;
        //Listener for onClick Event
        private OnClickListener mOnClickListener;

        public Content(Context context) {
            this(context, null);
        }

        private Content(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            createLayout();
            setAttributes(attrs);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private Content(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            createLayout();
            setAttributes(attrs);
        }

        /**
         * Set the values and style attributes
         *
         * @param attrs attrs
         */
        private void setAttributes(AttributeSet attrs) {
            /* Values attributes */
            TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.Tile, 0, 0);
            //Content text view
            try {
                String textContent = typedArray.getString(R.styleable.Tile_textContent);
                mTextContent.setText(textContent);
            } finally {
                typedArray.recycle();
            }
            //Style attributes
            typedArray = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.TileAppearance, R.attr.tileStyle, 0);
            try {
                String textFamily = typedArray.getString(R.styleable.TileAppearance_tileTextContentFamily);
                mTextContent.setTypeface(Typeface.create(textFamily, Typeface.BOLD));
                float textSize = typedArray.getDimension(R.styleable.TileAppearance_tileTextContentSize, 0);
                mTextContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                int color = typedArray.getColor(R.styleable.TileAppearance_tileTextContentColor, 0);
                mTextContent.setTextColor(color);
                color = typedArray.getColor(R.styleable.TileAppearance_tileContentBackground, 0);
                setBackgroundColor(color);
            } finally {
                typedArray.recycle();
            }
        }

        /**
         * It is called to determinate the size of the view
         *
         * @param widthMeasureSpec  width max constraint imposed by the parent
         * @param heightMeasureSpec height max constraint imposed by the parent
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSize == 0 && heightSize == 0) {
                // If there are no constraints on size, let FrameLayout measure
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);

                // Now use the smallest of the measured dimensions for both dimensions
                final int minSize = Math.min(getMeasuredWidth(), getMeasuredHeight());
                setMeasuredDimension(minSize, minSize);
                return;
            }

            final int size;
            if (widthSize == 0 || heightSize == 0) {
                // If one of the dimensions has no restriction on size, set both dimensions to be the
                // on that does
                size = Math.max(widthSize, heightSize);
            } else {
                // Both dimensions have restrictions on size, set both dimensions to be the
                // smallest of the two
                size = Math.min(widthSize, heightSize);
            }
            final int newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

            super.onMeasure(newMeasureSpec, newMeasureSpec);
        }

        /**
         * Create the layout for this view, including its children
         */
        private void createLayout() {
            mTextContent = new TextView(mContext);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mTextContent.setLayoutParams(layoutParams);
            mTextContent.setGravity(Gravity.CENTER);
            addView(mTextContent);
        }

        /**
         * set the text for the content
         *
         * @param value string to set
         */
        public void setTextContent(String value) {
            mTextContent.setText(value);
        }

        /**
         * Set background for content
         *
         * @param resId background resourceId
         */
        public void setContentBackground(int resId) {
            mTextContent.setBackgroundResource(resId);
        }

        @Override
        public void setOnClickListener(OnClickListener l) {
            super.setOnClickListener(this);
            mOnClickListener = l;
        }

        /**
         * When Content is clicked on
         *
         * @param v
         */
        @Override
        public void onClick(final View v) {
            mOnClickListener.onClick(v);
        }
    }

    /**
     * Footer view of the Tile, with two lines and icon
     */
    private class Footer extends LinearLayout {

        private final String LOG_TAG = Footer.class.getSimpleName();

        private Context mContext;
        //Layout for text views
        private LinearLayout mTextLayout;
        //TextView for primary line
        private TextView mTextPrimaryLine;
        //TextView for secondary line
        private TextView mTextSecondaryLine;
        //ImageView for the icon
        private ImageView mIcon;
        //OnClickListener for Text Layout
        private OnClickListener mTextLayoutOnClickListener;
        //OnClickListener for Icon Layout
        private OnClickListener mIconOnClickListener;
        /**
         * Style properties for the footer
         */
        private float mTileFooterHeight;
        private float mIconSize;
        private int mBackgroundColor;
        private float mTileTextFooterPrimarySize;
        private float mTileTextFooterSecondarySize;
        private float mTextFooterPadding;
        private int mBackgroundResId;

        private Footer(Context context, AttributeSet attrs) {
            super(context, attrs);
            mContext = context;
            loadStyleAttrs(attrs);
            createLayout(attrs);
            setAttributes(attrs);
        }

        /**
         * Set the values attributes
         *
         * @param attrs attributes from layout
         */
        private void setAttributes(AttributeSet attrs) {
            //First, it´ll get the values attributes
            TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.Tile, 0, 0);
            //Set values to the views
            try {
                String value = typedArray.getString(R.styleable.Tile_footerPrimaryLine);
                mTextPrimaryLine.setText(value);
                value = typedArray.getString(R.styleable.Tile_footerSecondaryLine);
                mTextSecondaryLine.setText(value);
                int iconResId = typedArray.getResourceId(R.styleable.Tile_footerIcon, -1);
                mIcon.setImageResource(iconResId);
            } finally {
                typedArray.recycle();
            }
        }

        /**
         * Load style attributes from theme into the class private variables
         */
        private void loadStyleAttrs(AttributeSet attrs) {
            TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.TileAppearance, R.attr.tileStyle, 0);
            try {
                mTileFooterHeight = typedArray.getDimension(R.styleable.TileAppearance_tileFooterHeight, 0);
                mIconSize = typedArray.getDimension(R.styleable.TileAppearance_tileIconSize, 0);
                mBackgroundColor = typedArray.getColor(R.styleable.TileAppearance_tileFooterBackground, 0);
                mTileTextFooterPrimarySize = typedArray.getDimension(R.styleable.TileAppearance_tileTextFooterPrimarySize, 0);
                mTileTextFooterSecondarySize = typedArray.getDimension(R.styleable.TileAppearance_tileTextFooterSecondarySize, 0);
                mTextFooterPadding = typedArray.getDimension(R.styleable.TileAppearance_tileTextFooterPadding, 0);
            } finally {
                typedArray.recycle();
            }
            typedArray = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
            try {
                mBackgroundResId = typedArray.getResourceId(0, 0);
            } finally {
                typedArray.recycle();
            }
        }

        /**
         * Create layout for the footer
         */
        private void createLayout(AttributeSet attrs) {
            mTextPrimaryLine = new TextView(mContext);
            mTextSecondaryLine = new TextView(mContext);
            mIcon = new ImageView(mContext);
            setOrientation(HORIZONTAL);
            //Align on the bottom of the tile
            setGravity(Gravity.BOTTOM);
            //Set height for the footer Layout
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mTileFooterHeight));
            setBackgroundColor(mBackgroundColor);
            //Inside, we create a new vertical LinearLayout to write the Primary and Secondary line
            addTextLayout();
            //Now, we add the icon to the footer, with the size defined in the style
            addIcon();
        }

        /**
         * Create the linear layout for the two lines text
         */
        private void addTextLayout() {
            mTextLayout = new LinearLayout(mContext);
            //Its width will be all the space left, for that the width is 0 and weight is 1
            mTextLayout.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            mTextLayout.setOrientation(VERTICAL);
            mTextLayout.setGravity(Gravity.CENTER);
            mTextLayout.setPadding((int) mTextFooterPadding,
                    (int) mTextFooterPadding,
                    (int) mTextFooterPadding,
                    (int) mTextFooterPadding);
            mTextLayout.setBackgroundResource(mBackgroundResId);
            //Primary line
            mTextPrimaryLine.setSingleLine();
            //To show the three dots at the end when the text is truncated
            mTextPrimaryLine.setEllipsize(TextUtils.TruncateAt.END);
            mTextPrimaryLine.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextFooterPrimarySize);
            mTextLayout.addView(mTextPrimaryLine);
            //Secondary line
            mTextPrimaryLine.setSingleLine();
            mTextSecondaryLine.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextFooterSecondarySize);
            mTextLayout.addView(mTextSecondaryLine);
            addView(mTextLayout);
        }

        /**
         * Add the icon to the footer
         */
        private void addIcon() {
            LinearLayout.LayoutParams layoutParams = new LayoutParams((int) mIconSize, (int) mIconSize);
            layoutParams.gravity = Gravity.CENTER;
            mIcon.setLayoutParams(layoutParams);
            int padding = (int) getResources().getDimension(R.dimen.padding_action_icon);
            mIcon.setPadding(padding, padding, padding, padding);
            mIcon.setBackgroundResource(mBackgroundResId);
            addView(mIcon);
        }

        /**
         * Set text to the primary line
         *
         * @param value text
         */
        public void setPrimaryLine(String value) {
            mTextPrimaryLine.setText(value);
        }

        /**
         * Set text to the secondary line
         *
         * @param value text
         */
        public void setSecondaryLine(String value) {
            mTextSecondaryLine.setText(value);
        }

        /**
         * Set the icon resource
         *
         * @param resId
         */
        public void setIcon(int resId) {
            mIcon.setImageResource(resId);
        }

        /**
         * Set the OnClickListener handler for the Linear Layout with textviews
         *
         * @param l handler
         */
        public void setTextOnClickListener(OnClickListener l) {
            mTextLayoutOnClickListener = l;
            mTextLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTextLayoutOnClickListener != null) {
                        mTextLayoutOnClickListener.onClick(v);
                    }
                }
            });
        }

        /**
         * Save the handler to send the onClick event and
         * set the onClick handler for icon
         *
         * @param l
         */
        public void setIconOnClickListener(OnClickListener l) {
            mIconOnClickListener = l;
            mIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIconOnClickListener != null) {
                        mIconOnClickListener.onClick(v);
                    }
                }
            });
        }

        /**
         * Set the background color for the whole text layout
         *
         * @param color color number
         */
        public void setTextBackgroundColor(int color) {
            Log.v(LOG_TAG, "setTextBackgroundColor " + String.valueOf(color));
            mTextLayout.setBackgroundColor(color);
        }

        /**
         * get the background color from the text layout
         *
         * @return color number
         */
        public int getTextBackgroundColor() {
            int color = Color.TRANSPARENT;
            Drawable drawable = mTextLayout.getBackground();
            if (drawable instanceof ColorDrawable) {
                color = ((ColorDrawable) drawable).getColor();
            }
            return color;
        }

        /**
         * set the color background for the icon
         *
         * @param color color number
         */
        public void setIconBackgroundColor(int color) {
            mIcon.setBackgroundColor(color);
        }

        /**
         * get the color background from the icon
         *
         * @return color number
         */
        public int getIconBackgroundColor() {
            int color = Color.TRANSPARENT;
            Drawable drawable = mIcon.getBackground();
            if (drawable instanceof ColorDrawable) {
                color = ((ColorDrawable) drawable).getColor();
            }
            return color;
        }

    }


}
