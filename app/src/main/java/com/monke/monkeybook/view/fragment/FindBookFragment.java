package com.monke.monkeybook.view.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;

public class FindBookFragment extends BaseFragment {
    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_find_book, container, false);
    }
}
