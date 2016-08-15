package org.appyx.android.lib.coverflowview;

import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Gstöttner Robert on 03/06/16.
 */

class CoverFlowEngine {
    private int mSize = -1;
    private Node mRightEnd = null;
    private Node mLeftEnd = null;
    private Node mStart = null;
    private Node mVisibleLeft = null;
    private Node mVisibleRight = null;
    private Context mContext = null;
    private CoverFlowView.Builder mBuilder = null;
    private CoverFlowView mCoverFlowView = null;
    private CoverFlowAnimator mAnimator = null;

    CoverFlowEngine(Context context, CoverFlowView.Builder builder, CoverFlowView coverFlowView) {
        mBuilder = builder;
        mContext = context;
        mCoverFlowView = coverFlowView;

        mSize = mBuilder.getCoverCount();
        if (mSize % 2 == 0) {
            mSize += 1;
        }

        init();
        mAnimator = new CoverFlowAnimator(this);
    }

    private void init() {
        Node pointer = null;
        int middleIndex = size() / 2;

        for (int i = 0; i < size(); i++) {
            if (i == 0) {
                mLeftEnd = new Node();
                pointer = mLeftEnd;
            } else {
                Node newNode = new Node();
                pointer.setRight(newNode);
                newNode.setLeft(pointer);
                pointer = newNode;
            }
            if (i == middleIndex) {
                mStart = pointer;
            }
            if (i == size() - 1) {
                mRightEnd = pointer;
            }

            FrameLayout cover = new FrameLayout(mContext);
            pointer.initCover(cover);
            mCoverFlowView.addView(cover);
        }
    }

    Node getLeftEnd() {
        return mLeftEnd;
    }

    Node getRightEnd() {
        return mRightEnd;
    }

    Node getStart() {
        return mStart;
    }

    Node getVisibleLeft() {
        return mVisibleLeft;
    }

    Node getVisibleRight() {
        return mVisibleRight;
    }

    CoverFlowAnimator getAnimator() {
        return mAnimator;
    }

    CoverFlowView.Builder getBuilder() {
        return mBuilder;
    }

    int size() {
        return mSize;
    }

    /**
     * Sets the visible bitmaps from the adapter.
     *
     * @param bitmaps The bitmaps from the Adapter
     */
    void setVisibleImages(ArrayList<AbstractCoverFlowAdapter.IndexedBitmap> bitmaps) {
        Node pointer = null;
        if (mBuilder.isStartModeLeft()) {
            pointer = mVisibleLeft;
        } else {
            pointer = mStart;
        }
        int counter = 0;

        if (mBuilder.isMatchMiddleMode()) {
            int startPos = (mBuilder.getCoverCount() + 1) / 2 - (bitmaps.size() + 1) / 2;
            while (pointer != mVisibleRight.getRight()) {
                if (counter < bitmaps.size() + startPos && counter >= startPos) {
                    pointer.getImageView().setImageBitmap(bitmaps.get(counter - startPos).getBitmap());
                    int bitmapIndex = bitmaps.get(counter - startPos).getIndex();
                    mCoverFlowView.getAdapter().setImageOnView(bitmapIndex, pointer.getImageView());
                    pointer.setIndex(bitmapIndex);
                    pointer.getLabel().setText(mCoverFlowView.getAdapter().getLabel(pointer.getIndex()));
                }
                counter += 1;
                pointer = pointer.getRight();
            }
            mCoverFlowView.getAdapter().setOffset(startPos);
        } else {
            while (pointer != mVisibleRight.getRight()) {
                if (counter < bitmaps.size()) {
                    pointer.getImageView().setImageBitmap(bitmaps.get(counter).getBitmap());
                    mCoverFlowView.getAdapter().setImageOnView(counter, pointer.getImageView());
                    pointer.setIndex(bitmaps.get(counter).getIndex());
                    pointer.getLabel().setText(mCoverFlowView.getAdapter().getLabel(pointer.getIndex()));
                }
                counter += 1;
                pointer = pointer.getRight();
            }
        }
    }


    void bringToFront() {
        Node pointer = null;
        //iterate left
        pointer = mLeftEnd;
        while (pointer != mStart) {
            pointer.getCover().bringToFront();
            pointer = pointer.getRight();
        }

        //iterate left
        pointer = mRightEnd;
        while (pointer != mStart) {
            pointer.getCover().bringToFront();
            pointer = pointer.getLeft();
        }
        mStart.getCover().bringToFront();

        mCoverFlowView.invalidate();
        mCoverFlowView.requestLayout();
    }

    void postRunnable(Runnable runnable) {
        mCoverFlowView.post(runnable);
    }

    void bringToFront(Node node) {
        node.getCover().bringToFront();
        mCoverFlowView.invalidate();
        mCoverFlowView.requestLayout();
    }

    void rotate() {
        //TODO: rotation interpolator
        Node pointer = null;
        float interpolator = 1;
        //iterate left
        pointer = mStart.getLeft();
        interpolator = mBuilder.getRotationInterpolator();
        while (pointer != null) {
            pointer.getCover().setRotationY(mBuilder.getRotation() * (1 + interpolator));
            pointer = pointer.getLeft();
            interpolator += interpolator / 10;
        }

        //iterate right
        pointer = mStart.getRight();
        interpolator = mBuilder.getRotationInterpolator();
        while (pointer != null) {
            pointer.getCover().setRotationY(-mBuilder.getRotation() * (1 + interpolator));
            pointer = pointer.getRight();
            interpolator += interpolator / 10;
        }
    }

    //called after second layout
    void setViewPositionsAndVisibleNodes(Point centerPosition) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mCoverFlowView.getLayoutParams();
        Node pointer = null;
        int centerX = centerPosition.x - params.leftMargin / 2;
        int centerY = centerPosition.y - params.topMargin;


        CoverFlowView.Size firstSize = mStart.getImageSize();
        mStart.setX(centerX - firstSize.width / 2);
        mStart.setY(centerY - firstSize.height / 2);

        //iterate left
        float space = 0;
        pointer = mStart.getLeft();
        while (pointer != null) {
            space += pointer.getImageSize().width * mBuilder.getCoverSpacing();
            pointer.setX(centerX - pointer.getImageSize().width / 2 - space);
            pointer.setY(centerY - pointer.getImageSize().height / 2);

            if (pointer.getX() + pointer.getImageSize().width > 0) {
                mVisibleLeft = pointer;
            }

            pointer = pointer.getLeft();
        }

        //iterate right
        space = 0;
        pointer = mStart.getRight();
        while (pointer != null) {
            space += pointer.getImageSize().width * mBuilder.getCoverSpacing();
            pointer.setX(centerX - pointer.getImageSize().width / 2 + space);
            pointer.setY(centerY - pointer.getImageSize().height / 2);

            if (pointer.getX() < mCoverFlowView.getWidth()) {
                mVisibleRight = pointer;
            }

            pointer = pointer.getRight();
        }
    }

    //called after first layout
    void setViewSizes() {
        Node pointer = null;
        CoverFlowView.Size size = null;
        FrameLayout.LayoutParams params = null;

        size = new CoverFlowView.Size(mBuilder.getImageSize());
        mStart.setImageSize(size);

        pointer = mStart.getLeft();
        while (pointer != null) {
            size = new CoverFlowView.Size(pointer.getRight().getImageSize());
            size.scale(mBuilder.getCoverScale());
            params = new FrameLayout.LayoutParams(size.getWidth(), size.getHeight());
            pointer.getCover().setLayoutParams(params);
            pointer.setImageSize(size);

            pointer = pointer.getLeft();
        }

        pointer = mStart.getRight();
        while (pointer != null) {
            size = new CoverFlowView.Size(pointer.getLeft().getImageSize());
            size.scale(mBuilder.getCoverScale());
            params = new FrameLayout.LayoutParams(size.getWidth(), size.getHeight());
            pointer.getCover().setLayoutParams(params);
            pointer.setImageSize(size);

            pointer = pointer.getRight();
        }


        size = new CoverFlowView.Size(mStart.getImageSize());
        params = new FrameLayout.LayoutParams(size.getWidth(), size.getHeight());
        mStart.setImageSize(size);
        mStart.getCover().setLayoutParams(params);
    }


    boolean swapImageViewsRight() {
        Node pointer = null;
        pointer = mRightEnd;
        FrameLayout first = mRightEnd.getCover();
        ImageView firstImage = mRightEnd.getImageView();
        TextView firstLabel = mRightEnd.getLabel();
        while (pointer != null) {
            if (pointer.hasLeft()) {
                pointer.setCover(pointer.getLeft().getCover());
                pointer.setIndex(pointer.getLeft().getIndex());
                pointer.setLabel(pointer.getLeft().getLabel());
                pointer.setImageView(pointer.getLeft().getImageView());
            } else {
                pointer.setCover(first);
                pointer.setImageView(firstImage);
                pointer.setLabel(firstLabel);
            }
            pointer = pointer.getLeft();
        }

        AbstractCoverFlowAdapter.IndexedBitmap image = mCoverFlowView.getAdapter().getLeftImage();
        mVisibleLeft.getImageView().setImageBitmap(image.getBitmap());
        mVisibleLeft.setIndex(image.getIndex());
        mVisibleLeft.getLabel().setText(mCoverFlowView.getAdapter().getLabelText(image.getIndex()));
        if (mVisibleLeft.getIndex() == -1) {
            mVisibleLeft.getLabel().setVisibility(View.GONE);
        } else {
            mVisibleLeft.getLabel().setVisibility(View.VISIBLE);
            mCoverFlowView.getAdapter().setImageOnView(image.getIndex(), mVisibleLeft.getImageView());  //use pseudo-generated image for the index
        }

        return true;
    }

    boolean swapImageViewsLeft() {
        Node pointer = null;
        pointer = mLeftEnd;
        FrameLayout first = mLeftEnd.getCover();
        ImageView firstImage = mLeftEnd.getImageView();
        TextView firstLabel = mLeftEnd.getLabel();
        while (pointer != null) {
            if (pointer.hasRight()) {
                pointer.setCover(pointer.getRight().getCover());
                pointer.setImageView(pointer.getRight().getImageView());
                pointer.setLabel(pointer.getRight().getLabel());
                pointer.setIndex(pointer.getRight().getIndex());
            } else {
                pointer.setCover(first);
                pointer.setImageView(firstImage);
                pointer.setLabel(firstLabel);
            }
            pointer = pointer.getRight();
        }
        AbstractCoverFlowAdapter.IndexedBitmap image = mCoverFlowView.getAdapter().getRightImage();
        mVisibleRight.getImageView().setImageBitmap(image.getBitmap());
        mVisibleRight.setIndex(image.getIndex());
        mVisibleRight.getLabel().setText(mCoverFlowView.getAdapter().getLabelText(image.getIndex()));
        if (mVisibleRight.getIndex() == -1) {
            mVisibleRight.getLabel().setVisibility(View.INVISIBLE);
        } else {
            mVisibleRight.getLabel().setVisibility(View.VISIBLE);
            mCoverFlowView.getAdapter().setImageOnView(image.getIndex(), mVisibleRight.getImageView()); //use pseudo-generated image for the index
        }
        return true;
    }

    int getVisibleNodesCount() {
        Node current = null;
        if (mBuilder.isStartModeLeft()) {
            current = mVisibleLeft;
        } else {
            current = mStart;
        }

        int count = 0;
        while (current != mVisibleRight.getRight()) {
            count += 1;
            current = current.getRight();
        }
        return count;
    }

    void animationStopped() {
        if (mStart.getIndex() > -1) {
            mCoverFlowView.getListener().onCoverChosen(mStart.getIndex());
        }
    }

    void reloadImages() {
        Node current = null;
        if (mBuilder.isStartModeLeft()) {
            current = mVisibleLeft;
        } else {
            current = mStart;
        }
        if (current == null) return;
        AbstractCoverFlowAdapter adapter = mCoverFlowView.getAdapter();
        while (current != mVisibleRight.getRight()) {
            int index = current.getIndex();
            if (index > -1 && index < adapter.getSize()) {
                current.getImageView().setImageBitmap(adapter.getImage(index));
                mCoverFlowView.getAdapter().setImageOnView(index, current.getImageView());
                current.getLabel().setText(adapter.getLabel(index));
            }
            current = current.getRight();
        }
    }


    class Node {
        private Node mRight = null;
        private Node mLeft = null;
        private ImageView mImageView = null;
        private TextView mLabel = null;
        private FrameLayout mCover = null;
        private CoverFlowView.Size mImageSize = null;
        private float mXpos = -1;
        private float mYpos = -1;
        private int mIndex = -1;

        public Node() {

        }

        public int getIndex() {
            return mIndex;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        void setX(float pos) {
            mCover.setX(pos);
            mXpos = pos;
        }

        float getX() {
            return mXpos;
        }

        void setY(float pos) {
            mCover.setY(pos);
            mYpos = pos;
        }

        float getY() {
            return mYpos;
        }

        void setRight(Node node) {
            mRight = node;
        }

        void setLeft(Node node) {
            mLeft = node;
        }

        Node getLeft() {
            return mLeft;
        }

        Node getRight() {
            return mRight;
        }


        boolean hasLeft() {
            return mLeft != null;
        }

        boolean hasRight() {
            return mRight != null;
        }

        void setImageView(ImageView imageView) {
            mImageView = imageView;
        }

        void setCover(FrameLayout cover) {
            mCover = cover;
        }

        FrameLayout getCover() {
            return mCover;
        }

        ImageView getImageView() {
            return mImageView;
        }

        CoverFlowView.Size getImageSize() {
            return mImageSize;
        }

        void setImageSize(CoverFlowView.Size size) {
            mImageSize = size;
        }

        void initCover(FrameLayout cover) {
            mCover = cover;
            if (mBuilder.getCoverLayoutResource() == -1) {
                mImageView = new ImageView(mContext);
                mImageView.setScaleType(mBuilder.getImageScaleType());
                mImageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mCover.addView(mImageView);
                mLabel = new TextView(mContext);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = mBuilder.getLabelGravity();
                mLabel.setLayoutParams(params);
                mLabel.setGravity(Gravity.CENTER);
                mLabel.setTextColor(mBuilder.getLabelColor());
                mLabel.setTextSize(mBuilder.getLabelSize());
                mLabel.setBackgroundColor(mBuilder.getLabelBackgroundColor());
                mCover.addView(mLabel);
            } else {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(mBuilder.getCoverLayoutResource(), mCover);
                View label = view.findViewById(mBuilder.getLabelRes());
                if (label != null) {
                    mLabel = (TextView) label;
                    mLabel.setText(null);
                } else {
                    mLabel = new TextView(mContext);
                    mLabel.setVisibility(View.GONE);
                }
                mImageView = (ImageView) view.findViewById(mBuilder.getImageRes());
                mImageView.setImageBitmap(null);
            }
        }

        TextView getLabel() {
            return mLabel;
        }

        void setLabel(TextView label) {
            mLabel = label;
        }
    }

}

