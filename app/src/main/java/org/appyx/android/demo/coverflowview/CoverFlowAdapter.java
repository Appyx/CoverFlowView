package org.appyx.android.demo.coverflowview;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import org.appyx.android.lib.coverflowview.AbstractCoverFlowAdapter;

import java.util.ArrayList;


/**
 * Created by Gstöttner Robert on 01/08/16.
 */

public class CoverFlowAdapter extends AbstractCoverFlowAdapter {

    private ArrayList<Integer> mData = new ArrayList<>();
    private Context mContext = null;

    public CoverFlowAdapter(Context context) {
        mContext = context;
        mData.add(R.drawable.banner1_r_cropped1);
        mData.add(R.drawable.banner1_r_cropped2);
        mData.add(R.drawable.banner1_r_cropped3);
        mData.add(R.drawable.banner1_r_cropped4);
        mData.add(R.drawable.banner1_r_cropped5);
        mData.add(R.drawable.banner1_r_cropped6);
//        mData.add(R.drawable.banner1_r_cropped7);
    }

    @Override
    protected void setImageOnView(int index, ImageView imageView) {
        imageView.setImageResource(mData.get(index));
        //For use with asynchronous image loader.
        //Just set the image on the view when it is finished.
    }

    @Override
    protected Bitmap getImage(int index) {
        return null;
    }

    @Override
    protected String getLabel(int index) {
        return "Khaleesi " + (index + 1);
    }

    @Override
    protected int getSize() {
        return mData.size();
    }


    @Override
    protected int getStartOffset() {
        return 0;
    }

}
