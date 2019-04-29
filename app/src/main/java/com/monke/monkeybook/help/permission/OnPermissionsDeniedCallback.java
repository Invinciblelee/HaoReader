package com.monke.monkeybook.help.permission;

public interface OnPermissionsDeniedCallback {
    void onPermissionsDenied(int requestCode, String[] deniedPermissions);
}
