package com.monke.monkeybook.help.keyboard;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class KeyboardHeightProvider extends PopupWindow implements ViewTreeObserver.OnGlobalLayoutListener {
    private Activity mActivity;
    private View rootView;
    private HeightListener listener;
    private int heightMax; // 记录popup内容区的最大高度
    private boolean isKeyboardActive;

    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.mActivity = activity;

        // 基础配置
        rootView = new View(activity);
        setContentView(rootView);

        setBackgroundDrawable(new ColorDrawable(0));

        // 设置宽度为0，高度为全屏
        setWidth(0);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        // 设置键盘弹出方式
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        setOnDismissListener(() -> {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(KeyboardHeightProvider.this);
        });
    }

    public KeyboardHeightProvider start() {
        if (!isShowing()) {
            final View view = mActivity.getWindow().getDecorView();
            // 延迟加载popupwindow，如果不加延迟就会报错
            view.post(() -> {
                if(!mActivity.isFinishing() && !mActivity.isDestroyed()){
                    showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
                }
            });
        }
        return this;
    }

    public KeyboardHeightProvider setHeightListener(HeightListener listener) {
        this.listener = listener;
        return this;
    }

    public boolean isKeyboardActive() {
        return isKeyboardActive;
    }

    public void stop(){
        dismiss();
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        // 监听全局Layout变化
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        if (rect.bottom > heightMax) {
            heightMax = rect.bottom;
        }

        // 两者的差值就是键盘的高度
        int keyboardHeight = heightMax - rect.bottom;

        isKeyboardActive = keyboardHeight > heightMax / 5;

        if (listener != null) {
            listener.onHeightChanged(keyboardHeight);
        }
    }


    public interface HeightListener {
        void onHeightChanged(int height);
    }
}