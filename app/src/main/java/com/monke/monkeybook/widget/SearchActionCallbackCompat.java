package com.monke.monkeybook.widget;

import android.graphics.Rect;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.view.activity.SearchBookActivity;

public class SearchActionCallbackCompat {

    public SearchActionCallbackCompat(TextView textView) {
        if (!textView.isTextSelectable()) {
            textView.setTextIsSelectable(true);
        }

        ActionMode.Callback2 textSelectionActionModeCallback;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            textSelectionActionModeCallback = new ActionMode.Callback2() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menuInflater.inflate(R.menu.menu_text_selection_search, menu);
                    return true;//返回false则不会显示弹窗
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    menu.removeItem(android.R.id.shareText);
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    //根据item的ID处理点击事件
                    if (menuItem.getItemId() == R.id.action_search_book) {
                        String textContent = textView.getText().toString();
                        String selected = textContent.substring(textView.getSelectionStart(), textView.getSelectionEnd());
                        if(selected.length() > 12){
                            selected = selected.substring(0, 12);
                        }
                        actionMode.finish();
                        SearchBookActivity.startByKey(textView.getContext(), selected);
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {

                }

                @Override
                public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                    //可选  用于改变弹出菜单的位置
                    super.onGetContentRect(mode, view, outRect);
                }
            };

            textView.setCustomSelectionActionModeCallback(textSelectionActionModeCallback);
        }
    }
}
