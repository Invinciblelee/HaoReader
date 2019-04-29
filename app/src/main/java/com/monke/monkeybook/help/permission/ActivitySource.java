package com.monke.monkeybook.help.permission;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;

final class ActivitySource implements RequestSource{

    private final WeakReference<AppCompatActivity> mActRef;

    ActivitySource(AppCompatActivity activity) {
        mActRef = new WeakReference<>(activity);
    }

    @Override
    public Context getContext() {
        return mActRef.get();
    }

    @Override
    public FragmentManager getFragmentManager() {
        return mActRef.get().getSupportFragmentManager();
    }

    @Override
    public void startActivity(Intent intent) {
        mActRef.get().startActivity(intent);
    }

}
