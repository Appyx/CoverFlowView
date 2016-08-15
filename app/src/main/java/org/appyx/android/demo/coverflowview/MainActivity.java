package org.appyx.android.demo.coverflowview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;

import org.appyx.android.lib.coverflowview.CoverFlowView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CoverFlowView view = (CoverFlowView) findViewById(R.id.coverFlowView);
        view.setListener(new CoverFlowView.CoverListener() {
            @Override
            public void onCoverChosen(int index) {
                Log.i("chosen", "index: " + index);
            }
        });
        setupCoverFlowView(view);
    }

    public void setupCoverFlowView(CoverFlowView view) {
        view.setAdapter(new CoverFlowAdapter(this));

        CoverFlowView.Builder builder = view.getBuilder();

        builder.setCoverSpacing(0.5f);
        builder.setEndlessMode(true);
        builder.setMatchMiddleMode(true);
        builder.setStartModeLeft(true);
        builder.setMaxCovers(7);
        builder.setZoomAll(0.7f);
        builder.setZoomFirst(1.4f);
        builder.setRotation(40);
        builder.setSpacingFirst(0.2f);
        builder.setLabelSize(13);
        builder.setLabelBackgroundColor("#55eeeeee");
        builder.setLabelGravity(Gravity.BOTTOM);
        builder.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        builder.setCoverWidthPercentage(0.9f);

        builder.build();
    }
}
