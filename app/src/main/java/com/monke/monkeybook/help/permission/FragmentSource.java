package com.monke.monkeybook.help.permission;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;

final class FragmentSource implements RequestSource {

    private final WeakReference<Fragment> mFragRef;

    FragmentSource(Fragment fragment) {
        mFragRef = new WeakReference<>(fragment);
    }

    @Override
    public Context getContext() {
        return mFragRef.get().getContext();
    }

    @Override
    public FragmentManager getFragmentManager() {
        return mFragRef.get().getChildFragmentManager();
    }

    @Override
    public void startActivity(Intent intent) {
        mFragRef.get().startActivity(intent);
    }
}
