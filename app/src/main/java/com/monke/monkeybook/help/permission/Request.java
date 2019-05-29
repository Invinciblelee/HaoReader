package com.monke.monkeybook.help.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.monke.monkeybook.R;
import com.monke.monkeybook.view.fragment.dialog.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * create on 2019/2/13
 * <p>
 * by 咸鱼
 */
final class Request implements OnRequestPermissionsResultCallback {

    static final int TYPE_REQUEST_PERMISSION = 1;
    static final int TYPE_REQUEST_SETTING = 2;

    private final long mRequestTime;
    private int mRequestCode = TYPE_REQUEST_PERMISSION;
    private RequestSource mSource;
    private ArrayList<String> mPermissions;
    private OnPermissionsResultCallback mResultCallback;
    private OnPermissionsGrantedCallback mGrantedCallback;
    private OnPermissionsDeniedCallback mDeniedCallback;
    private CharSequence mRationale;
    private int mRationaleResId;

    private AlertDialog mRationaleDialog;

    private Handler mHandler;

    private static class CallbackHandler extends Handler {

        private static final int MSG_GRANTED = 1;
        private static final int MSG_DENIED = 2;


        private WeakReference<Request> mRequest;

        private CallbackHandler(Request request) {
            mRequest = new WeakReference<>(request);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GRANTED:
                    mRequest.get().dispatchGrantedEvent(msg.arg1);
                    break;
                case MSG_DENIED:
                    mRequest.get().dispatchDeniedEvent(msg.arg1, (String[]) msg.obj);
                    break;
            }
        }
    }

    Request(@NonNull AppCompatActivity activity) {
        mRequestTime = System.currentTimeMillis();
        mHandler = new CallbackHandler(this);
        mSource = new ActivitySource(activity);
        mPermissions = new ArrayList<>();
    }

    Request(@NonNull Fragment fragment) {
        mRequestTime = System.currentTimeMillis();
        mHandler = new CallbackHandler(this);
        mSource = new FragmentSource(fragment);
        mPermissions = new ArrayList<>();
    }

    void addPermissions(@NonNull String... permissions) {
        mPermissions.addAll(Arrays.asList(permissions));
    }

    void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }


    void setCallback(OnPermissionsResultCallback callback) {
        mResultCallback = callback;
    }

    void setOnGrantedCallback(OnPermissionsGrantedCallback callback) {
        mGrantedCallback = callback;
    }

    void setOnDeniedCallback(OnPermissionsDeniedCallback callback) {
        mDeniedCallback = callback;
    }

    void setRationale(CharSequence rationale) {
        mRationaleResId = 0;
        mRationale = rationale;
    }

    void setRationale(@StringRes int resId) {
        mRationaleResId = resId;
        mRationale = null;
    }

    void start() {
        RequestPlugins.setOnRequestPermissionsCallback(this);

        final String[] deniedPermissions = getDeniedPermissions();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (deniedPermissions == null) {
                onPermissionsGranted(mRequestCode);
            } else {
                final CharSequence rationale = getRationale();
                if (rationale != null) {
                    showSettingDialog(rationale, () -> onPermissionsDenied(mRequestCode, deniedPermissions));
                } else {
                    onPermissionsDenied(mRequestCode, deniedPermissions);
                }
            }
        } else {
            if (deniedPermissions != null) {
                Intent intent = new Intent(mSource.getContext(), PermissionActivity.class);
                intent.putExtra(PermissionActivity.KEY_INPUT_REQUEST_TYPE, TYPE_REQUEST_PERMISSION);
                intent.putExtra(PermissionActivity.KEY_INPUT_PERMISSIONS_CODE, mRequestCode);
                intent.putExtra(PermissionActivity.KEY_INPUT_PERMISSIONS, deniedPermissions);
                mSource.startActivity(intent);
            } else {
                onPermissionsGranted(mRequestCode);
            }
        }
    }

    void clear() {
        mResultCallback = null;
        mGrantedCallback = null;
        mDeniedCallback = null;
    }

    long getStartTime() {
        return mRequestTime;
    }

    private CharSequence getRationale() {
        final CharSequence rationale;
        if (mRationaleResId != 0) {
            rationale = mSource.getContext().getText(mRationaleResId);
        } else if (mRationale != null) {
            rationale = mRationale;
        } else {
            rationale = null;
        }
        return rationale;
    }

    private String[] getDeniedPermissions() {
        final String[] permissions;
        int size = mPermissions == null ? 0 : mPermissions.size();
        if (size > 0) {
            permissions = mPermissions.toArray(new String[size]);
        } else {
            permissions = null;
        }
        return getDeniedPermissions(permissions);
    }

    private String[] getDeniedPermissions(String[] permissions) {
        if (permissions != null) {
            ArrayList<String> deniedPermissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(mSource.getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permission);
                }
            }
            int size = deniedPermissionList.size();
            if (size > 0) {
                return deniedPermissionList.toArray(new String[size]);
            }
        }
        return null;
    }

    private void showSettingDialog(@NonNull CharSequence rationale, @NonNull CancelAction cancel) {
        if (mRationaleDialog != null) {
            mRationaleDialog.dismiss();
        }
        mRationaleDialog = new AlertDialog.Builder(mSource.getFragmentManager())
                .setTitle(R.string.dialog_title)
                .setMessage(mSource.getContext().getString(R.string.tip_permission_denied, rationale))
                .setMessageTextAlignment(View.TEXT_ALIGNMENT_TEXT_START)
                .setPositiveButton(R.string.goto_setting, (dialog, which) -> {
                    Intent intent = new Intent(mSource.getContext(), PermissionActivity.class);
                    intent.putExtra(PermissionActivity.KEY_INPUT_REQUEST_TYPE, TYPE_REQUEST_SETTING);
                    mSource.startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> cancel.onCancel())
                .show();
        mRationaleDialog.show();
    }

    private void onPermissionsGranted(int requestCode) {
        Message message = mHandler.obtainMessage(CallbackHandler.MSG_GRANTED);
        message.arg1 = requestCode;
        message.sendToTarget();
    }

    private void onPermissionsDenied(int requestCode, String[] deniedPermissions) {
        Message message = mHandler.obtainMessage(CallbackHandler.MSG_DENIED, deniedPermissions);
        message.arg1 = requestCode;
        message.sendToTarget();
    }

    private void dispatchGrantedEvent(int requestCode) {
        try {
            if (mResultCallback != null) {
                mResultCallback.onPermissionsGranted(requestCode);
            }

            if (mGrantedCallback != null) {
                mGrantedCallback.onPermissionsGranted(requestCode);
            }
        } catch (Exception ignore) {
        }

        if (RequestPlugins.sResultCallback != null) {
            RequestPlugins.sResultCallback.onPermissionsGranted(requestCode);
        }
    }

    private void dispatchDeniedEvent(int requestCode, String[] deniedPermissions) {
        try {
            if (mResultCallback != null) {
                mResultCallback.onPermissionsDenied(requestCode, deniedPermissions);
            }

            if (mDeniedCallback != null) {
                mDeniedCallback.onPermissionsDenied(requestCode, deniedPermissions);
            }
        } catch (Exception ignore) {
        }

        if (RequestPlugins.sResultCallback != null) {
            RequestPlugins.sResultCallback.onPermissionsDenied(requestCode, deniedPermissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mSource == null) {
            return;
        }
        String[] deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions != null) {
            final CharSequence rationale = getRationale();
            if (rationale != null) {
                showSettingDialog(rationale, () -> onPermissionsDenied(requestCode, deniedPermissions));
            } else {
                onPermissionsDenied(requestCode, deniedPermissions);
            }
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final String[] deniedPermissions = getDeniedPermissions();
        if (deniedPermissions == null) {
            onPermissionsGranted(mRequestCode);
        } else {
            onPermissionsDenied(mRequestCode, deniedPermissions);
        }
    }

    private interface CancelAction {
        void onCancel();
    }
}