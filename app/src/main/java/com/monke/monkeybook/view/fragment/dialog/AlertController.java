package com.monke.monkeybook.view.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.monke.monkeybook.R;

import java.lang.ref.WeakReference;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

final class AlertController {

    private static final String ARG_THEME = "ARG_THEME";
    private static final String ARG_TITLE_TEXT = "ARG_TITLE_TEXT";
    private static final String ARG_TITLE_TEXT_ID = "ARG_TITLE_TEXT_ID";
    private static final String ARG_MESSAGE_TEXT = "ARG_MESSAGE_TEXT";
    private static final String ARG_MESSAGE_TEXT_ID = "ARG_MESSAGE_TEXT_ID";
    private static final String ARG_MESSAGE_TEXT_ALIGNMENT = "ARG_MESSAGE_TEXT_ALIGNMENT";
    private static final String ARG_VIEW_LAYOUT_ID = "ARG_VIEW_LAYOUT_ID";
    private static final String ARG_VIEW_LAYOUT_WIDTH = "ARG_VIEW_LAYOUT_WIDTH";
    private static final String ARG_VIEW_LAYOUT_HEIGHT = "ARG_VIEW_LAYOUT_HEIGHT";
    private static final String ARG_VIEW_LAYOUT_GRAVITY = "ARG_VIEW_LAYOUT_GRAVITY";
    private static final String ARG_POSITIVE_TEXT = "ARG_POSITIVE_TEXT";
    private static final String ARG_POSITIVE_TEXT_ID = "ARG_POSITIVE_TEXT_ID";
    private static final String ARG_NEGATIVE_TEXT = "ARG_NEGATIVE_TEXT";
    private static final String ARG_NEGATIVE_TEXT_ID = "ARG_NEGATIVE_TEXT_ID";

    private final Context mContext;
    final AlertDialog mDialog;
    private final LayoutInflater mInflater;
    private FragmentManager mFragmentManager;

    Handler mHandler;

    private int mTheme;

    private boolean mCancelable;

    private View mAlertView;

    AppCompatTextView mTitleView;
    private CharSequence mTitleText;

    AppCompatTextView mMessageView;
    private CharSequence mMessageText;
    private int mMessageTextAlignment;

    AppCompatButton mButtonPositive;
    Message mButtonPositiveMessage;
    private CharSequence mPositiveText;
    private AlertDialog.OnClickListener mPositiveClickListener;

    AppCompatButton mButtonNegative;
    Message mButtonNegativeMessage;
    private CharSequence mNegativeText;
    private AlertDialog.OnClickListener mNegativeClickListener;

    FrameLayout mContentFrame;

    LinearLayout mTopPanel;
    LinearLayout mContentPanel;
    LinearLayout mButtonsPanel;

    private View mNoCustomDivider;
    private View mNoTitleDivider;
    private View mNoButtonsDivider;
    private int mViewLayoutResId;
    private FrameLayout.LayoutParams mViewParams;

    private AlertDialog.OnViewCreatedCallback mViewCreatedCallback;

    private AlertDialog.OnDismissListener mDismissListener;

    private final View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Message m;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else {
                m = null;
            }

            if (m != null) {
                m.sendToTarget();
            }

            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialog)
                    .sendToTarget();
        }
    };


    private static class ButtonHandler extends Handler {

        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<AlertDialog> mDialog;

        private ButtonHandler(AlertDialog dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case AlertDialog.BUTTON_POSITIVE:
                case AlertDialog.BUTTON_NEGATIVE:
                    ((AlertDialog.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((AlertDialog) msg.obj).dismiss();
            }
        }
    }

    AlertController(Context context, AlertDialog di) {
        mContext = context;
        mDialog = di;
        mInflater = LayoutInflater.from(mContext);
        mHandler = new ButtonHandler(mDialog);

        if (mTheme != 0) {
            mDialog.setStyle(DialogFragment.STYLE_NO_TITLE, mTheme);
        }

        mDialog.setCancelable(mCancelable);

        setupArguments();
    }

    View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        if (mAlertView == null) {
            mAlertView = inflater.inflate(R.layout.dialog_alert, container, false);
        }
        return mAlertView;
    }

    void onViewCreated(@NonNull View view) {
        setupViews(view);
        if (mViewCreatedCallback != null) {
            mViewCreatedCallback.onViewCreated(mDialog, view);
        }
    }

    void onDestroy() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    void restoreState(@NonNull Bundle savedInstanceState) {
        AlertParams params = new AlertParams(mContext, savedInstanceState);
        mDialog.setAlertParams(params);
    }

    void applyAlertParams(AlertParams params) {
        params.apply(this);
    }

    private void setupArguments() {
        Bundle args = mDialog.getArguments();
        if (args != null) {
            AlertParams p = new AlertParams(mContext, args);
            mTheme = p.mTheme;
            mTitleText = p.mTitleText;
            mMessageText = p.mMessageText;
            mMessageTextAlignment = p.mMessageTextAlignment;
            mPositiveText = p.mPositiveText;
            mNegativeText = p.mNegativeText;
            mViewLayoutResId = p.mViewLayoutResId;
            mViewParams = p.mViewParams;
        }
    }

    private void setupViews(View alertView) {
        mTitleView = alertView.findViewById(R.id.tv_title);
        mMessageView = alertView.findViewById(R.id.tv_msg);
        mButtonPositive = alertView.findViewById(R.id.btn_done);
        mButtonNegative = alertView.findViewById(R.id.btn_cancel);
        mContentFrame = alertView.findViewById(R.id.customPanel);
        mNoCustomDivider = alertView.findViewById(R.id.titleDividerNoCustom);
        mNoTitleDivider = alertView.findViewById(R.id.textSpacerNoTitle);
        mNoButtonsDivider = alertView.findViewById(R.id.textSpacerNoButtons);
        mTopPanel = alertView.findViewById(R.id.topPanel);
        mContentPanel = alertView.findViewById(R.id.contentPanel);
        mButtonsPanel = alertView.findViewById(R.id.buttonsBar);

        if (mTitleText != null) {
            mTitleView.setText(mTitleText);
        } else {
            mTopPanel.setVisibility(View.GONE);
            mNoTitleDivider.setVisibility(View.VISIBLE);
        }

        if (mMessageText != null) {
            mMessageView.setText(mMessageText);
        } else {
            mContentPanel.setVisibility(View.GONE);
        }

        mMessageView.setTextAlignment(mMessageTextAlignment);

        final View customView;
        if (mViewLayoutResId != 0) {
            customView = mInflater.inflate(mViewLayoutResId, mContentFrame, false);
        } else {
            customView = null;
        }

        if (customView != null) {
            if (mViewParams == null) {
                mViewParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            }
            mContentFrame.setVisibility(View.VISIBLE);
            mNoCustomDivider.setVisibility(View.GONE);
            mContentFrame.addView(customView, mViewParams);
        }


        if (mPositiveText != null) {
            mButtonPositive.setText(mPositiveText);
            mButtonPositive.setOnClickListener(mButtonClickListener);
        } else {
            mButtonPositive.setVisibility(View.GONE);
        }

        if (mNegativeText != null) {
            mButtonNegative.setText(mNegativeText);
            mButtonNegative.setOnClickListener(mButtonClickListener);
        } else {
            mButtonNegative.setVisibility(View.GONE);
        }

        final boolean hasButtons = mNegativeText != null || mPositiveText != null;
        if (!hasButtons) {
            mButtonsPanel.setVisibility(View.GONE);
            if (customView == null) {
                mNoButtonsDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    private void setOnViewCreatedCallback(AlertDialog.OnViewCreatedCallback callback) {
        mViewCreatedCallback = callback;
    }

    private void setOnDismissListener(AlertDialog.OnDismissListener listener) {
        mDismissListener = listener;
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        mDialog.setCancelable(cancelable);
    }

    public void setTitle(CharSequence title) {
        mTitleText = title;
        if (mTitleView != null) {
            mTitleView.setText(mTitleText);
            mTopPanel.setVisibility(mTitleText != null ? View.VISIBLE : View.GONE);
        }
    }

    public void setTitle(@StringRes int titleResId) {
        setTitle(mContext.getText(titleResId));
    }

    public void setMessage(CharSequence message) {
        mMessageText = message;
        if (mMessageView != null) {
            mMessageView.setText(mMessageText);
            mContentPanel.setVisibility(mMessageText != null ? View.VISIBLE : View.GONE);
        }
    }

    public void setMessage(@StringRes int messageResId) {
        setMessage(mContext.getText(messageResId));
    }

    public void setMessageTextAlignment(int textAlignment) {
        mMessageTextAlignment = textAlignment;
        if (mMessageView != null) {
            mMessageView.setTextAlignment(mMessageTextAlignment);
        }
    }

    public void setView(@LayoutRes int layoutResId) {
        mViewLayoutResId = layoutResId;

        if (mContentFrame != null) {
            mContentFrame.removeAllViews();

            if (mViewLayoutResId != 0) {
                if (mViewParams == null) {
                    mViewParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                }
                View customView = mInflater.inflate(mViewLayoutResId, mContentFrame, false);
                mContentFrame.setVisibility(View.VISIBLE);
                mNoCustomDivider.setVisibility(View.GONE);
                mContentFrame.addView(customView, mViewParams);
            }
        }
    }

    public void setView(@LayoutRes int layoutResId, FrameLayout.LayoutParams params) {
        mViewLayoutResId = layoutResId;
        mViewParams = params;

        if (mContentFrame != null) {
            mContentFrame.removeAllViews();

            if (mViewLayoutResId != 0) {
                if (mViewParams == null) {
                    mViewParams = new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                }
                View customView = mInflater.inflate(mViewLayoutResId, mContentFrame, false);
                mContentFrame.setVisibility(View.VISIBLE);
                mNoCustomDivider.setVisibility(View.GONE);
                mContentFrame.addView(customView, mViewParams);
            }
        }
    }

    public void setButton(int whichButton, CharSequence text,
                          AlertDialog.OnClickListener listener) {

        final Message msg;
        if (listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        } else {
            msg = null;
        }

        switch (whichButton) {
            case AlertDialog.BUTTON_POSITIVE:
                mPositiveText = text;
                mPositiveClickListener = listener;
                mButtonPositiveMessage = msg;
                if (mButtonPositive != null) {
                    mButtonPositive.setText(mPositiveText);
                    if (mPositiveText != null) {
                        mButtonPositive.setOnClickListener(mButtonClickListener);
                        mButtonPositive.setVisibility(View.VISIBLE);
                    } else {
                        mButtonPositive.setVisibility(View.GONE);
                    }
                }
                break;
            case AlertDialog.BUTTON_NEGATIVE:
                mNegativeText = text;
                mNegativeClickListener = listener;
                mButtonNegativeMessage = msg;
                if (mButtonNegative != null) {
                    mButtonNegative.setText(mNegativeText);
                    if (mNegativeText != null) {
                        mButtonNegative.setOnClickListener(mButtonClickListener);
                        mButtonNegative.setVisibility(View.VISIBLE);
                    } else {
                        mButtonNegative.setVisibility(View.GONE);
                    }
                }
                break;
        }
        final boolean hasButtons = mNegativeText != null || mPositiveText != null;
        if (mButtonsPanel != null) {
            mButtonsPanel.setVisibility(hasButtons ? View.VISIBLE : View.GONE);
            if (!hasButtons) {
                mNoButtonsDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setButton(int whichButton, @StringRes int textResId,
                          AlertDialog.OnClickListener listener) {
        setButton(whichButton, mContext.getText(textResId), listener);
    }

    public void show() {
        if (mFragmentManager != null) {
            mDialog.show(mFragmentManager, "alert");
        }
    }

    static class AlertParams {
        int mTheme;
        FragmentManager mFragmentManager;

        boolean mCancelable;

        int mViewLayoutResId;
        FrameLayout.LayoutParams mViewParams;

        int mMessageTextAlignment = -1;

        CharSequence mTitleText;
        int mTitleTextResId;
        CharSequence mMessageText;
        int mMessageTextResId;
        CharSequence mPositiveText;
        int mPositiveTextResId;
        CharSequence mNegativeText;
        int mNegativeTextResId;

        AlertDialog.OnClickListener mPositiveClickListener;
        AlertDialog.OnClickListener mNegativeClickListener;

        AlertDialog.OnViewCreatedCallback mViewCreatedCallback;

        AlertDialog.OnDismissListener mDismissListener;

        AlertParams() {
            mCancelable = true;
        }

        AlertParams(@NonNull Context context, @NonNull Bundle bundle) {
            mTheme = bundle.getInt(ARG_THEME);

            mTitleText = bundle.getCharSequence(ARG_TITLE_TEXT);
            if (mTitleText == null) {
                int titleResId = bundle.getInt(ARG_TITLE_TEXT_ID, 0);
                if (titleResId != 0) {
                    mTitleText = context.getText(titleResId);
                }
            }

            mMessageText = bundle.getCharSequence(ARG_MESSAGE_TEXT);
            if (mMessageText == null) {
                int messageResId = bundle.getInt(ARG_MESSAGE_TEXT_ID, 0);
                if (messageResId != 0) {
                    mMessageText = context.getText(messageResId);
                }
            }

            mMessageTextAlignment = bundle.getInt(ARG_MESSAGE_TEXT_ALIGNMENT, View.TEXT_ALIGNMENT_TEXT_START);

            mPositiveText = bundle.getCharSequence(ARG_POSITIVE_TEXT);
            if (mPositiveText == null) {
                int positiveResId = bundle.getInt(ARG_POSITIVE_TEXT_ID, 0);
                if (positiveResId != 0) {
                    mPositiveText = context.getText(positiveResId);
                }
            }

            mNegativeText = bundle.getCharSequence(ARG_NEGATIVE_TEXT);
            if (mNegativeText == null) {
                int negativeResId = bundle.getInt(ARG_NEGATIVE_TEXT_ID, 0);
                if (negativeResId != 0) {
                    mNegativeText = context.getText(negativeResId);
                }
            }

            mViewLayoutResId = bundle.getInt(ARG_VIEW_LAYOUT_ID, 0);

            int viewWidth = bundle.getInt(ARG_VIEW_LAYOUT_WIDTH, 0);
            int viewHeight = bundle.getInt(ARG_VIEW_LAYOUT_HEIGHT, 0);
            if (viewWidth != 0 && viewHeight != 0) {
                mViewParams = new FrameLayout.LayoutParams(viewWidth, viewHeight);
                int gravity = bundle.getInt(ARG_VIEW_LAYOUT_GRAVITY, 0);
                if (gravity != 0) {
                    mViewParams.gravity = gravity;
                }
            }
        }

        Bundle createArguments() {
            Bundle bundle = new Bundle();
            if (mTheme != 0) {
                bundle.putInt(ARG_THEME, mTheme);
            }

            if (mTitleText != null) {
                bundle.putCharSequence(ARG_TITLE_TEXT, mTitleText);
            } else if (mTitleTextResId != 0) {
                bundle.putInt(ARG_TITLE_TEXT_ID, mTitleTextResId);
            }

            if (mMessageText != null) {
                bundle.putCharSequence(ARG_MESSAGE_TEXT, mMessageText);
            } else if (mMessageTextResId != 0) {
                bundle.putInt(ARG_MESSAGE_TEXT_ID, mMessageTextResId);
            }

            if (mMessageTextAlignment != -1) {
                bundle.putInt(ARG_MESSAGE_TEXT_ALIGNMENT, mMessageTextAlignment);
            }

            if (mPositiveText != null) {
                bundle.putCharSequence(ARG_POSITIVE_TEXT, mPositiveText);
            } else if (mPositiveTextResId != 0) {
                bundle.putInt(ARG_POSITIVE_TEXT_ID, mPositiveTextResId);
            }

            if (mNegativeText != null) {
                bundle.putCharSequence(ARG_NEGATIVE_TEXT, mNegativeText);
            } else if (mNegativeTextResId != 0) {
                bundle.putInt(ARG_NEGATIVE_TEXT_ID, mNegativeTextResId);
            }

            if (mViewLayoutResId != 0) {
                bundle.putInt(ARG_VIEW_LAYOUT_ID, mViewLayoutResId);
            }

            if (mViewParams != null) {
                bundle.putInt(ARG_VIEW_LAYOUT_WIDTH, mViewParams.width);
                bundle.putInt(ARG_VIEW_LAYOUT_HEIGHT, mViewParams.height);
                bundle.putInt(ARG_VIEW_LAYOUT_GRAVITY, mViewParams.gravity);
            }

            return bundle;
        }

        void apply(AlertController dialog) {
            dialog.setFragmentManager(mFragmentManager);
            dialog.setCancelable(mCancelable);

            if (mTitleText != null) {
                dialog.setTitle(mTitleText);
            } else if (mTitleTextResId != 0) {
                dialog.setTitle(mTitleTextResId);
            }

            if (mMessageText != null) {
                dialog.setMessage(mMessageText);
            } else if (mMessageTextResId != 0) {
                dialog.setMessage(mMessageTextResId);
            }

            if (mMessageTextAlignment != -1) {
                dialog.setMessageTextAlignment(mMessageTextAlignment);
            }

            if (mPositiveText != null) {
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, mPositiveText, mPositiveClickListener);
            } else if (mPositiveTextResId != 0) {
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, mPositiveTextResId, mPositiveClickListener);
            }

            if (mNegativeText != null) {
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, mNegativeText, mNegativeClickListener);
            } else if (mNegativeTextResId != 0) {
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, mNegativeTextResId, mNegativeClickListener);
            }

            if (mViewCreatedCallback != null) {
                dialog.setOnViewCreatedCallback(mViewCreatedCallback);
            }

            if (mDismissListener != null) {
                dialog.setOnDismissListener(mDismissListener);
            }

            if (mMessageTextAlignment != -1) {
                dialog.setMessageTextAlignment(mMessageTextAlignment);
            }

            if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId, mViewParams);
            }
        }
    }
}
