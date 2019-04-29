package com.monke.monkeybook.help.permission;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnRequestPermissionsResultCallback {

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);


    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
}
