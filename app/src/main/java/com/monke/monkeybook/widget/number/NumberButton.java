package com.monke.monkeybook.widget.number;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.widget.AppCompat;

import java.text.DecimalFormat;

public class NumberButton extends FrameLayout implements View.OnClickListener {
    public static final int INT = 0;
    public static final int FLOAT = 1;

    private OnChangedListener onChangedListener;
    private TextView tvNumber;
    private DecimalFormat decimalFormat = new DecimalFormat("#");
    private int numberType = INT;
    private float minNumber = 0;
    private float maxNumber = 10;
    private float stepNumber = 1;
    private String tile = "请选择";

    public NumberButton(Context context) {
        this(context, null);
    }

    public NumberButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_number_buttom, this);

        TextView addButton = findViewById(R.id.button_add);
        addButton.setOnClickListener(this);
        TextView subButton = findViewById(R.id.button_sub);
        subButton.setOnClickListener(this);
        tvNumber = findViewById(R.id.tv_number);
        tvNumber.setOnClickListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NumberButton, 0, 0);
        if(a.hasValue(R.styleable.NumberButton_themeColor)){
            int themeColor = a.getColor(R.styleable.NumberButton_themeColor, context.getResources().getColor(R.color.tv_text_default));
            addButton.setTextColor(themeColor);
            subButton.setTextColor(themeColor);
            tvNumber.setTextColor(themeColor);
        }
        if(a.hasValue(R.styleable.NumberButton_android_background)){
            ViewCompat.setBackground(this, null);
            ViewCompat.setBackground(getChildAt(0), a.getDrawable(R.styleable.NumberButton_android_background));
        }
        a.recycle();
    }

    public NumberButton setTitle(@NonNull String title) {
        this.tile = title;
        return this;
    }

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    public float getNumber() {
        try {
            return Float.parseFloat(tvNumber.getText().toString());
        } catch (NumberFormatException e) {
            tvNumber.setText(decimalFormat.format(minNumber));
            return minNumber;
        }
    }

    public NumberButton setFormat(String pattern) {
        decimalFormat = new DecimalFormat(pattern);
        return this;
    }

    public NumberButton setNumber(float number) {
        tvNumber.setText(decimalFormat.format(number));
        return this;
    }

    public NumberButton setMinNumber(float minNumber) {
        this.minNumber = minNumber;
        return this;
    }

    public NumberButton setMaxNumber(float maxNumber) {
        this.maxNumber = maxNumber;
        return this;
    }

    public NumberButton setStepNumber(float stepNumber) {
        this.stepNumber = stepNumber;
        return this;
    }

    public NumberButton setNumberType(int numberType) {
        this.numberType = numberType;
        return this;
    }

    @Override
    public void onClick(View view) {
        float count = getNumber();
        switch (view.getId()) {
            case R.id.button_add:
                if (count < maxNumber) {
                    changeNumber(count + stepNumber);
                }
                break;
            case R.id.button_sub:
                if (count > minNumber) {
                    changeNumber(count - stepNumber);
                }
                break;
            case R.id.tv_number:
                if (numberType == INT) {
                    NumberPickerDialog npd = new NumberPickerDialog(getContext());
                    npd.setTitle(tile)
                            .setMaxValue((int) maxNumber)
                            .setMinValue((int) minNumber)
                            .setValue((int) getNumber())
                            .setListener(this::changeNumber)
                            .create()
                            .show();
                }
                break;
        }
    }

    private void changeNumber(float f) {
        tvNumber.setText(decimalFormat.format(f));
        if (onChangedListener != null) {
            onChangedListener.numberChange(f);
        }
    }

    public interface OnChangedListener {
        void numberChange(float number);
    }

}
