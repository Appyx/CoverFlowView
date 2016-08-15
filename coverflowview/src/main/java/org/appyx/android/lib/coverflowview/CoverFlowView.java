package org.appyx.android.lib.coverflowview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Gstöttner Robert on 02/06/16.
 */

public class CoverFlowView extends FrameLayout {
    private Context mContext = null;
    private Builder mBuilder = null;
    private CoverFlowEngine mEngine = null;
    private Point mCenterPosition = null;
    private GestureDetectorCompat mGestureDetector = null;
    private AbstractCoverFlowAdapter mAdapter = null;
    private CoverListener mListener = null;


    public CoverFlowView(Context context) {
        super(context);
        mContext = context;

    }

    public CoverFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    /**
     * Called after Builder.build()
     */
    //TODO: repeat setup in order to be able to call notify with a changed size
    private void setup() {
        if (mAdapter.getSize() < mBuilder.getCoverCount()) {
            mBuilder.setCoverCount(mAdapter.getSize());
        }
        mCenterPosition = new Point(Math.round(getX() + getWidth()) / 2, Math.round(getY() + getHeight() / 2));
        mEngine = new CoverFlowEngine(mContext, mBuilder, this);
        mAdapter.setBuilder(mBuilder);
        mAdapter.setEngine(mEngine);
        mEngine.setViewSizes();
        mEngine.rotate();
        mEngine.bringToFront();
        requestLayout();
        post(new Runnable() {
            @Override
            public void run() {
                //set views
                mEngine.setViewPositionsAndVisibleNodes(mCenterPosition);
                mBuilder.setCoverCount(mEngine.getVisibleNodesCount());
                mEngine.setVisibleImages(mAdapter.init());
                //init animations
                mEngine.getAnimator().doDown(0);
                mEngine.getAnimator().doMove(0, 1);
                mGestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
                mListener.onCoverChosen(mEngine.getStart().getIndex());
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event); //gesture detector event
        }

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) { // raw events
            case MotionEvent.ACTION_MOVE: {
                mEngine.getAnimator().doMove(event.getRawX(), 2);
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                mEngine.getAnimator().doDown(event.getRawX());
                break;
            }
            case MotionEvent.ACTION_UP: {
                mEngine.getAnimator().doUp(event.getRawX());
                break;
            }
        }


        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    static class Size {
        float width = -1;
        float height = -1;
        float ratio = -1;
        private float scaleX = -1;
        private float scaleY = -1;

        Size(float width, float height) {
            this.height = height;
            this.width = width;
            this.ratio = width / height;
        }

        Size(Size copy) {
            width = copy.width;
            height = copy.height;
            ratio = copy.ratio;
        }

        Size() {

        }

        int getHeight() {
            return Math.round(height);
        }

        int getWidth() {
            return Math.round(width);
        }

        void scaleX(float scale) {
            scaleX = scale;
            width *= scale;
        }

        void scaleY(float scale) {
            scaleY = scale;
            height *= scale;
        }

        void scale(float scale) {
            scaleX(scale);
            scaleY(scale);
        }

        float getScaleX() {
            return scaleX;
        }

        float getScaleY() {
            return scaleY;
        }
    }


    public Builder getBuilder() {
        mBuilder = new Builder();
        return mBuilder;
    }

    public void setAdapter(AbstractCoverFlowAdapter adapter) {
        mAdapter = adapter;
    }

    public AbstractCoverFlowAdapter getAdapter() {
        return mAdapter;
    }

    public void setListener(CoverListener listener) {
        mListener = listener;
    }

    CoverListener getListener() {
        return mListener;
    }


    public class Builder {
        private ArrayList<Integer> mResources = null;
        private float mCoverSpacing = 0.5f;
        private float mCoverScale = 1f; //not yet supported
        private Size mImageSize = null;
        private int mCoverCount = 3; // can be anything -  overwritten by builder
        private float mZoom = 1;
        private float mZoomFirst = 1;
        private int mRotation = 0;
        private float mRotationInterpolator = 1f; //not yet supported
        private float mSpacingFirst = 0;
        private boolean mStartModeLeft = true;
        private boolean mEndlessMode = false;
        private boolean mMatchMiddleMode = true;
        private int mLabelColor = Color.WHITE;
        private int mLabelSize = 20;
        private int mLabelGravity = Gravity.BOTTOM;
        private int mLabelBackgroundColor = Color.TRANSPARENT;
        private ImageView.ScaleType mImageScaleType = ImageView.ScaleType.CENTER_CROP;
        private float mImageWidthPercentage = 1;
        private boolean mLabelEnabled = true;
        private int mCoverLayoutResource = -1;
        private int mLabelRes = -1;
        private int mImageRes = -1;

        private Builder() {
        }

        float getCoverSpacing() {
            return mCoverSpacing;
        }

        float getCoverScale() {
            return mCoverScale;
        }

        Size getImageSize() {
            return mImageSize;
        }

        float getZoom() {
            return mZoom;
        }

        float getZoomFist() {
            return mZoomFirst;
        }

        int getRotation() {
            return mRotation;
        }

        float getRotationInterpolator() {
            return mRotationInterpolator;
        }

        int getCoverCount() {
            return mCoverCount;
        }

        float getSpacingFirst() {
            return mSpacingFirst;
        }

        boolean isEndlessMode() {
            return mEndlessMode;
        }

        /**
         * Alias of setMaxCovers()
         *
         * @param coverCount
         */
        void setCoverCount(int coverCount) {
            if (coverCount < 2) {
                coverCount = 2;
            }
            mCoverCount = coverCount;
        }

        boolean isMatchMiddleMode() {
            return mMatchMiddleMode;
        }

        boolean isStartModeLeft() {
            return mStartModeLeft;
        }

        void calculateImageSize() {
            mImageSize = new Size(CoverFlowView.this.getHeight() * getImageWidthPercentage(), CoverFlowView.this.getHeight());
            mImageSize.scale(mZoom);
        }

        int getLabelColor() {
            return mLabelColor;
        }

        int getLabelSize() {
            return mLabelSize;
        }

        int getLabelGravity() {
            return mLabelGravity;
        }

        int getLabelBackgroundColor() {
            return mLabelBackgroundColor;
        }

        ImageView.ScaleType getImageScaleType() {
            return mImageScaleType;
        }

        float getImageWidthPercentage() {
            return mImageWidthPercentage;
        }

        int getCoverLayoutResource() {
            return mCoverLayoutResource;
        }

        int getLabelRes() {
            return mLabelRes;
        }

        int getImageRes() {
            return mImageRes;
        }

        /**
         * Use this method to create your own layout.
         * Make sure to use only relative attributes in the layout. - Things may differ in the result.
         * Note that the text size for the label in the layout can be set to any value - use the builder method to change the size.
         * If you use this method, some other UI-specific builder methods may not work.
         * You can also use ImageView descendants e.g. CircularImageView.
         *
         * @param layoutResID The layout resource id. Can NOT be -1.
         * @param imageID     The id of the ImageView or a descendant in the layout. Can NOT be -1.
         * @param labelID     The id of the TextView which is used as label. Can be -1 to disable the label.
         * @return The builder Object.
         */
        public Builder setCoverResource(int layoutResID, int imageID, int labelID) {
            mCoverLayoutResource = layoutResID;
            mImageRes = imageID;
            mLabelRes = labelID;
            return this;
        }

        /**
         * Sets whether the Label should be enabled or not.
         *
         * @param labelEnabled true to enable
         * @return
         */
        public Builder setLabelEnabled(boolean labelEnabled) {
            mLabelEnabled = labelEnabled;
            return this;
        }

        /**
         * Sets the witdh of the cover in percent of the height which equals the height of the whole CoverflowView.
         *
         * @param imageWidthPercentage
         * @return The builder object.
         */
        public Builder setCoverWidthPercentage(float imageWidthPercentage) {
            mImageWidthPercentage = imageWidthPercentage;
            return this;
        }

        /**
         * Sets the scale type of the image in the cover
         *
         * @param imageScaleType
         * @return The builder object.
         */
        public Builder setImageScaleType(ImageView.ScaleType imageScaleType) {
            mImageScaleType = imageScaleType;
            return this;
        }

        /**
         * Sets the background color of the label.
         *
         * @param color The color in hex
         * @return The builder object.
         */
        public Builder setLabelBackgroundColor(String color) {
            mLabelBackgroundColor = Color.parseColor(color);
            return this;
        }

        /**
         * Sets the layout-gravity of the label
         *
         * @param labelGravity e.g Gravity.BOTTOM
         * @return The builder object.
         */
        public Builder setLabelGravity(int labelGravity) {
            mLabelGravity = labelGravity;
            return this;
        }

        /**
         * Sets the size of the label.
         *
         * @param labelSize The size in dp
         * @return The builder object.
         */
        public Builder setLabelSize(int labelSize) {
            mLabelSize = labelSize;
            return this;
        }

        /**
         * Sets the color for the label
         *
         * @param color The color in hex
         * @return The builder object.
         */
        public Builder setLabelColor(String color) {
            mLabelColor = Color.parseColor(color);
            return this;
        }

        /**
         * Set the rotation of the pictures in degrees.
         *
         * @param degrees
         * @return The builder object
         */
        public Builder setRotation(int degrees) {
            mRotation = degrees;
            return this;
        }

        /**
         * Set the general spacing between the covers in percent of the width of each cover.
         *
         * @param spacing A float value in percent
         * @return The builder object
         */
        public Builder setCoverSpacing(float spacing) {
            mCoverSpacing = spacing;
            return this;
        }

        /**
         * Set the maximum amount of calculated covers. This can significantly affect the performance on low-level devices.
         *
         * @param coverCount The amount of covers which are used for calculation.
         * @return The builder object
         */
        public Builder setMaxCovers(int coverCount) {
            if (coverCount < 2) {
                coverCount = 2;
            }
            mCoverCount = coverCount;
            return this;
        }

        /**
         * Scale all covers equally per a percentage value.
         *
         * @param zoom A float value in percent
         * @return The builder object
         */
        public Builder setZoomAll(float zoom) {
            mZoom = zoom;
            return this;
        }

        /**
         * Scale only the middle cover per a percentage value.
         *
         * @param zoom A float value in percent
         * @return The builder object
         */
        public Builder setZoomFirst(float zoom) {
            mZoomFirst = zoom;
            return this;
        }

        /**
         * Sets the spacing between the left-middle and the right-middle cover.
         *
         * @param spacing
         * @return The builder object
         */
        public Builder setSpacingFirst(float spacing) {
            mSpacingFirst = spacing;
            return this;
        }


        /**
         * Set whether the covers should repeat after the last was shown or not.
         *
         * @param endlessMode True for repeat
         * @return The builder object
         */
        public Builder setEndlessMode(boolean endlessMode) {
            mEndlessMode = endlessMode;
            return this;
        }

        /**
         * Sets whether the covers should start in the middle of the view or on the left side.
         *
         * @param isLeft True for starting at the left side.
         * @return The builder object
         */
        public Builder setStartModeLeft(boolean isLeft) {
            mStartModeLeft = isLeft;
            return this;
        }

        /**
         * Sets whether the middle of the images in the adapter should match the middle cover of the view.
         *
         * @param matchMiddleMode True for matching the adapters middle element with the middle cover.
         * @return The builder object
         */
        public Builder setMatchMiddleMode(boolean matchMiddleMode) {
            mMatchMiddleMode = matchMiddleMode;
            return this;
        }


        /**
         * To be called when the configuration of the view is done.
         * Make sure to set an adapter first.
         * Other configurations are not possible after this call.
         */
        public void build() {
            if (mAdapter == null) {
                throw new IllegalArgumentException("you have to set an adapter first");
            }
            if (mListener == null) {
                throw new IllegalArgumentException("you have to set a listener first");
            }

            if (!mLabelEnabled) {
                setLabelSize(0);
            }

            CoverFlowView.this.post(new Runnable() {
                @Override
                public void run() {
                    calculateImageSize();
                    setup();
                }
            });

        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mEngine.getAnimator().doFling(velocityX);
            return true;
        }
    }

    public interface CoverListener {
        void onCoverChosen(int index);
    }
}
