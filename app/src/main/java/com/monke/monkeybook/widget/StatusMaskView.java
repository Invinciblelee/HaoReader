package com.monke.monkeybook.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.utils.ScreenUtils;

public class StatusMaskView extends View {

    private final int mStatusHeight;

    public StatusMaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mStatusHeight = isInEditMode() ? 50 : ScreenUtils.getStatusBarHeight();

        if (!isInEditMode()) {
            final boolean isStatusTrans = AppConfigHelper.get().getPreferences().getBoolean("immersionStatusBar", false);
            setBackgroundColor(isStatusTrans ? Color.TRANSPARENT : getResources().getColor(R.color.colorStatusBar));
        } else {
            setBackgroundColor(getResources().getColor(R.color.colorStatusBar));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mStatusHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
