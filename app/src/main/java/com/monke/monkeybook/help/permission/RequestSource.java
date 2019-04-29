package com.monke.monkeybook.help.permission;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

public interface RequestSource {

    Context getContext();

    FragmentManager getFragmentManager();

    void startActivity(Intent intent);

}
