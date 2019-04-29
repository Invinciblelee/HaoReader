package com.monke.monkeybook.help.permission;

import android.os.Handler;

import java.util.Collections;
import java.util.Stack;

/**
 * create on 2019/2/13
 *
 * by 咸鱼
 */
class RequestManager implements OnPermissionsResultCallback {

    private static RequestManager sManager;

    private Stack<Request> mRequests;
    private Request mRequest;

    private Handler mHandler = new Handler();

    private Runnable mRequestStarter = new Runnable() {
        @Override
        public void run() {
            if (mRequest != null) {
                mRequest.start();
            }
        }
    };

    private RequestManager() {
        RequestPlugins.setOnPermissionsResultCallback(this);
    }

    static RequestManager get() {
        if (sManager == null) {
            synchronized (RequestManager.class) {
                if (sManager == null) {
                    sManager = new RequestManager();
                }
            }
        }
        return sManager;
    }

    void pushRequest(Request request) {
        if (request == null) return;

        if (mRequests == null) {
            mRequests = new Stack<>();
        }

        int index = mRequests.indexOf(request);
        if (index >= 0) {
            int to = mRequests.size() - 1;
            if (index != to) {
                Collections.swap(mRequests, index, to);
            }
        } else {
            mRequests.push(request);
        }

        if (mRequest == null || isTimeOut()) {
            mRequest = mRequests.pop();
            mHandler.post(mRequestStarter);
        }
    }

    private void startNextRequest() {
        if (mRequests == null) return;

        if (mRequest != null) {
            mRequest.clear();
            mRequest = null;
        }

        if (!mRequests.empty()) {
            mRequest = mRequests.pop();
            mHandler.post(mRequestStarter);
        }
    }

    private boolean isTimeOut() {
        return mRequest != null && System.currentTimeMillis() - mRequest.getStartTime() > 5 * 1000L;
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        startNextRequest();
    }

    @Override
    public void onPermissionsDenied(int requestCode, String[] deniedPermissions) {
        startNextRequest();
    }
}
