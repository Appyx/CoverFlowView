package org.appyx.android.lib.coverflowview;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Gstöttner Robert on 01/08/16.
 */

public abstract class AbstractCoverFlowAdapter {

    private int mCoverCount = -1;
    private int mLeftIndex = 0;
    private int mRightIndex = 0;
    private CoverFlowView.Builder mBuilder = null;
    private CoverFlowEngine mEngine = null;

    IndexedBitmap getLeftImage() {
        mLeftIndex -= 1;
        mRightIndex -= 1;

        if (mBuilder.isEndlessMode() && mLeftIndex < 0) {
            mLeftIndex = getSize() - 1;
        }
        if (mBuilder.isEndlessMode() && mRightIndex < 0) {
            mRightIndex = getSize() - 1;
        }
        if (mLeftIndex >= 0 && mLeftIndex < getSize()) {
            return new IndexedBitmap(getImage(mLeftIndex), mLeftIndex);
        } else {
            return new IndexedBitmap();
        }
    }


    IndexedBitmap getRightImage() {
        mRightIndex += 1;
        mLeftIndex += 1;

        if (mBuilder.isEndlessMode() && mRightIndex > getSize() - 1) {
            mRightIndex = 0;
        }
        if (mBuilder.isEndlessMode() && mLeftIndex > getSize() - 1) {
            mLeftIndex = 0;
        }
        if (mRightIndex < getSize() && mRightIndex >= 0) {
            return new IndexedBitmap(getImage(mRightIndex), mRightIndex);
        } else {
            return new IndexedBitmap();
        }
    }

    ArrayList<IndexedBitmap> init() {
        mCoverCount = mBuilder.getCoverCount();
        int offset = getStartOffset();
        mRightIndex += offset;

        ArrayList<IndexedBitmap> images = new ArrayList<>();
        if (getSize() == 0) {
            return images;
        }
        for (int i = 0; i < mCoverCount; i++) {
            if (mRightIndex < getSize()) {
                images.add(new IndexedBitmap(getImage(mRightIndex), mRightIndex));
            }
            mRightIndex += 1;

        }
        mRightIndex -= 1;
        if (mBuilder.isStartModeLeft()) {
            mLeftIndex += offset;
        } else {
            mLeftIndex = -mCoverCount + 1 + offset;
        }
        return images;
    }

    String getLabelText(int index) {
        if (index > -1 && index < getSize()) {
            return getLabel(index);
        }
        return "";
    }

    /**
     * Use this method if you want to set an image asynchronously via Picasso for example.
     * Use either this method or getImage(int index).
     *
     * @param index     the index of the Image
     * @param imageView the View reference
     */
    protected abstract void setImageOnView(int index, ImageView imageView);

    /**
     * Use this method in all cases, where you load the images synchronously.
     * Use either this method or setImageOnView(int index, ImageView imageView).
     *
     * @param index The index of the Image
     * @return The bitmap to set or null if using the other method.
     */
    protected abstract Bitmap getImage(int index);

    /**
     * The final size must be known in order to get correct results.
     * This will change in future releases.
     *
     * @return The final amount of images.
     */
    protected abstract int getSize();

    /**
     * You don't have to set the label.
     * To hide it completely use the builder options.
     *
     * @param index The index of the image.
     * @return The String for the label.
     */
    protected abstract String getLabel(int index);

    /**
     * Use this to enable an offset from the first Image.
     *
     * @return 0 if unused.
     */
    protected abstract int getStartOffset();

    int getCoverCount() {
        return mCoverCount;
    }

    void setBuilder(CoverFlowView.Builder builder) {
        mBuilder = builder;
    }

    void setEngine(CoverFlowEngine engine) {
        mEngine = engine;
    }


    void setOffset(int offset) {
        mLeftIndex -= offset;
        mRightIndex -= offset;
    }

    class IndexedBitmap {
        private Bitmap mBitmap = null;
        private int mIndex = -1;

        private IndexedBitmap(Bitmap bitmap, int index) {
            mBitmap = bitmap;
            mIndex = index;
        }

        public IndexedBitmap() {

        }

        Bitmap getBitmap() {
            return mBitmap;
        }

        int getIndex() {
            return mIndex;
        }

        void setIndex(int index) {
            mIndex = index;
        }
    }

    /**
     * Reloads all visible images.
     * At the moment it is not possible to change the size of the adapter and reload the images.
     */
    public void notifyDataChanged() {
        if (mEngine != null) {
            mEngine.reloadImages();
        }
    }
}
