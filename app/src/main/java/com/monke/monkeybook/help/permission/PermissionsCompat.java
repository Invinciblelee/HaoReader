package com.monke.monkeybook.help.permission;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * create on 2019/2/13
 *
 * by 咸鱼
 */
public class PermissionsCompat  {

    private Request mRequest;

    private PermissionsCompat() {
    }

    public void request(){
        RequestManager.get().pushRequest(mRequest);
    }

    public static class Builder {
       private Request mRequest;

        public Builder(@NonNull AppCompatActivity activity) {
            mRequest = new Request(activity);
        }

        public Builder(@NonNull Fragment fragment) {
            mRequest = new Request(fragment);
        }

        public Builder addPermissions(@NonNull String... permissions) {
            mRequest.addPermissions(permissions);
            return this;
        }

        public Builder requestCode(int requestCode) {
            mRequest.setRequestCode(requestCode);
            return this;
        }


        public Builder callback(OnPermissionsResultCallback callback) {
            mRequest.setCallback(callback);
            return this;
        }

        public Builder onGranted(OnPermissionsGrantedCallback callback) {
            mRequest.setOnGrantedCallback(callback);
            return this;
        }

        public Builder onDenied(OnPermissionsDeniedCallback callback) {
            mRequest.setOnDeniedCallback(callback);
            return this;
        }

        public Builder rationale(CharSequence rationale) {
           mRequest.setRationale(rationale);
            return this;
        }

        public Builder rationale(@StringRes int resId) {
            mRequest.setRationale(resId);
            return this;
        }

        public PermissionsCompat build() {
            PermissionsCompat compat = new PermissionsCompat();
            compat.mRequest = mRequest;
            return compat;
        }

        public PermissionsCompat request() {
            PermissionsCompat compat = build();
            compat.mRequest = mRequest;
            compat.request();
            return compat;
        }
    }

}
