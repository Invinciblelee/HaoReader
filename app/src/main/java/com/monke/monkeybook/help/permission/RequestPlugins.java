package com.monke.monkeybook.help.permission;

/**
 * create on 2019/2/13
 *
 * by 咸鱼
 */
final class RequestPlugins {

    static volatile OnRequestPermissionsResultCallback sRequestCallback;

    static volatile OnPermissionsResultCallback sResultCallback;

    static void setOnRequestPermissionsCallback(OnRequestPermissionsResultCallback callback) {
        sRequestCallback = callback;
    }

    static void setOnPermissionsResultCallback(OnPermissionsResultCallback  callback){
        sResultCallback = callback;
    }


}
