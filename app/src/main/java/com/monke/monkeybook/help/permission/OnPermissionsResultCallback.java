package com.monke.monkeybook.help.permission;

public interface OnPermissionsResultCallback {

    void onPermissionsGranted(int requestCode);

    void onPermissionsDenied(int requestCode, String[] deniedPermissions);

}