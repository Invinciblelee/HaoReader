package com.monke.monkeybook.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.monke.monkeybook.R;

public class ScaleDrawableTextView extends android.support.v7.widget.AppCompatTextView {

    public ScaleDrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleDrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScaleDrawableTextView, defStyleAttr, 0);
        if (a == null) {
            return;
        }
        float scale  = a.getFloat(R.styleable.ScaleDrawableTextView_drawableScale, 1.2f);
        ColorStateList tintList = a.getColorStateList(R.styleable.ScaleDrawableTextView_drawableColor);
        a.recycle();

        int size = (int) (getTextSize() * scale);
        Drawable[] drawables = getCompoundDrawablesRelative();
        for (Drawable drawable: drawables){
            AppCompat.setTintList(drawable, tintList);

            if(drawable != null){
                drawable.setBounds(0, 0, size, size);
            }
        }
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3]);
    }
}
