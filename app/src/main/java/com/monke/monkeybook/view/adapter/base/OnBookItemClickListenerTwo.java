package com.monke.monkeybook.view.adapter.base;

import android.view.View;

import com.monke.monkeybook.bean.BookShelfBean;

public interface OnBookItemClickListenerTwo {
    void onClick(View view, BookShelfBean bookShelf);

    void onLongClick(View view, BookShelfBean bookShelf);
}
