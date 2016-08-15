package org.appyx.android.lib.coverflowview;

import android.widget.FrameLayout;

/**
 * Created by Gstöttner Robert and modified by Adam Oliver on 08/06/16.
 */

public class CoverFlowAnimator {

    private CoverFlowEngine mEngine = null;
    private CoverFlowView.Builder mBuilder = null;

    private int mShownMiddle = 0;
    private float mLastMove = 0;
    private float mMovedDistance = 0;
    private float mXSpacingBefore = 0;
    private float mDistance = 0;
    private float mXLeft = 0;
    private float mMovedLast = 0;
    private float mLeftFling = 0;
    private float mFlingMovedLast = 0;

    private boolean mIsFling = false;
    private boolean mIsUpRunning = false;
    private boolean mIsFlingRunning = false;

    private android.os.Handler mFlingHandler = null;
    private android.os.Handler mUpHandler = null;

    private Runnable mUpRunnable = null;
    private Runnable mFlingRunnable = null;


    CoverFlowAnimator(CoverFlowEngine engine) {
        mEngine = engine;
        mBuilder = mEngine.getBuilder();
        mShownMiddle = Math.round(mEngine.size() / 2);
        mUpHandler = new android.os.Handler();
        mFlingHandler = new android.os.Handler();
        initRunnables();
    }

    private void initRunnables() {
        mFlingRunnable = new Runnable() {
            @Override
            public void run() {
                mEngine.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        float distanceFactor = mMovedDistance / mDistance;
                        if (distanceFactor <= -0.5) {
                            while (distanceFactor < -0.5) {
                                distanceFactor++;
                            }
                        } else if (distanceFactor >= 0.5) {
                            while (distanceFactor > 0.5) {
                                distanceFactor--;
                            }
                        }
                        if (mLeftFling / mDistance + distanceFactor > -0.5 && mLeftFling / mDistance + distanceFactor < 0.5) {
                            mIsFlingRunning = false;
                            mIsFling = false;
                            doUp(mFlingMovedLast + mLeftFling + distanceFactor * mDistance);
                        } else {
                            mFlingMovedLast += mLeftFling / 16;
                            mLeftFling -= mLeftFling / 16;
                            doMove(mFlingMovedLast, 1);
                            postFlingRunnable();
                        }
                    }
                });
            }
        };

        mUpRunnable = new Runnable() {
            @Override
            public void run() {
                mEngine.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (mXLeft < -1 || mXLeft > 1) {
                            mMovedLast += mXLeft / 4;
                            mXLeft -= mXLeft / 4;
                            doMove(mMovedLast, 1);
                            postUpRunnable();
                        } else {
                            //triggered on fixed cover
                            mIsUpRunning = false;
                            doMove(mMovedLast + mXLeft, 1);
                            mEngine.animationStopped();
                        }

                    }
                });
            }
        };
    }


    public void postFlingRunnable() {
        mFlingHandler.postDelayed(mFlingRunnable, 10);
    }

    public void postUpRunnable() {
        mUpHandler.postDelayed(mUpRunnable, 10);
    }


    private float getDistance() {
        FrameLayout actual = mEngine.getStart().getCover();
        FrameLayout next = mEngine.getStart().getRight().getCover();
        float nextMiddle = next.getX() + next.getWidth() / 2;
        float actualMiddle = actual.getX() + actual.getWidth() / 2;
        return nextMiddle - actualMiddle;
    }


    void doDown(float rawX) {
        if (mIsFlingRunning) {
            mFlingHandler.removeCallbacks(mFlingRunnable);
            mIsFlingRunning = false;
            mIsFling = false;
        } else if (mIsUpRunning) {
            mUpHandler.removeCallbacks(mUpRunnable);
            mIsUpRunning = false;
        }

        mLastMove = rawX;
        if (mDistance == 0)
            mDistance = getDistance();
    }

    void doMove(float rawX, float divider) {
        float move = (rawX - mLastMove) / divider;
        mLastMove = rawX;
        mMovedDistance += move;
        float distanceFactor = mMovedDistance / mDistance;

        CoverFlowEngine.Node pointer = null;
        pointer = mEngine.getLeftEnd();

        int isInsideCounter = mShownMiddle;
        int changed = 0;

        while (pointer != null) {
            float scaleFactor = mBuilder.getCoverScale();
            float rotateFactor = mBuilder.getRotation();
            FrameLayout actual = pointer.getCover();
            float offset = actual.getX() + move;

            if (isInsideCounter - 0.5 <= distanceFactor && isInsideCounter + 0.5 >= distanceFactor) {

                if (isInsideCounter + mEngine.size() / 2 != mShownMiddle) {
                    if (isInsideCounter + mEngine.size() / 2 < mShownMiddle)
                        changed = mShownMiddle - (isInsideCounter + mEngine.size() / 2);
                    else if (isInsideCounter + mEngine.size() / 2 > mShownMiddle)
                        changed = mShownMiddle - (isInsideCounter + mEngine.size() / 2);

                    mEngine.bringToFront(pointer);
                }
                float factor = 0;
                if (isInsideCounter < 0)
                    factor = (distanceFactor + isInsideCounter * -1) * 2;
                else
                    factor = (distanceFactor - isInsideCounter) * 2;

                CoverFlowEngine.Node helpNode = pointer;
                float xSpacingOffset = (helpNode.getImageSize().width * mBuilder.getSpacingFirst()) * (1 - Math.abs(factor)) - mXSpacingBefore;
                while (helpNode.hasRight()) {
                    helpNode = helpNode.getRight();
                    helpNode.getCover().setX(helpNode.getCover().getX() + xSpacingOffset);
                }
                helpNode = pointer;
                while (helpNode.hasLeft()) {
                    helpNode = helpNode.getLeft();
                    helpNode.getCover().setX(helpNode.getCover().getX() - xSpacingOffset);
                }
                mXSpacingBefore = (helpNode.getImageSize().width * mBuilder.getSpacingFirst()) * (1 - Math.abs(factor));

                scaleFactor = mBuilder.getZoomFist() - ((mBuilder.getZoomFist() - 1) * Math.abs(factor));
                rotateFactor = -mBuilder.getRotation() * factor;

            } else {
                if (isInsideCounter < distanceFactor)
                    rotateFactor *= -1;
            }

            // Transformations

            actual.setX(offset);
            actual.setRotationY(rotateFactor);
            actual.setScaleX(scaleFactor);
            actual.setScaleY(scaleFactor);

            isInsideCounter--;
            pointer = pointer.getRight();
        }
        if (changed != 0) {
            if (changed > 0) { // bring right pictureView in middleNode
                while (changed != 0) {
                    float startPosX = mEngine.getRightEnd().getCover().getX() + mEngine.getLeftEnd().getImageSize().width * mBuilder.getCoverSpacing();
                    mEngine.getLeftEnd().getCover().setRotationY(mBuilder.getRotation() * -1);
                    mEngine.getLeftEnd().getCover().setX(startPosX);
                    mEngine.swapImageViewsLeft();
                    mEngine.bringToFront();
                    mShownMiddle--;
                    changed--;
                }
            } else if (changed < 0) { // bring left pictureView in middleNode
                while (changed != 0) {
                    float startPosX = mEngine.getLeftEnd().getCover().getX() - mEngine.getLeftEnd().getImageSize().width * mBuilder.getCoverSpacing();
                    mEngine.getRightEnd().getCover().setRotationY(mBuilder.getRotation());
                    mEngine.getRightEnd().getCover().setX(startPosX);
                    mEngine.swapImageViewsRight();
                    mEngine.bringToFront();
                    mShownMiddle++;
                    changed++;
                }
            }
        }
    }

    /**
     * Animate snapping when the finger goes up
     *
     * @param rawX
     */

    void doUp(float rawX) {
        if (mIsFling)
            return;

        float distanceFactor = mMovedDistance / mDistance;
        if (distanceFactor <= -0.5) {
            while (distanceFactor < -0.5) {
                distanceFactor++;
            }
        } else if (distanceFactor >= 0.5) {
            while (distanceFactor > 0.5) {
                distanceFactor--;
            }
        }

        doDown(0);
        mMovedLast = 0;
        mXLeft = distanceFactor * mDistance * -1;

        // scheduling the task at fixed rate delay
        mIsUpRunning = true;
        postUpRunnable();
    }


    void doFling(float velocityX) {
        mIsFling = true;
        doDown(0);
        mFlingMovedLast = 0;
        mLeftFling = velocityX / 25;
        // scheduling the task at fixed rate delay
        mIsFlingRunning = true;
        postFlingRunnable();
    }
}
