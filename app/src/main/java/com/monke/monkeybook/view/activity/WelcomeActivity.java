//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.widget.ImageView;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.presenter.ReadBookPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends MBaseActivity {

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_welcome);

        getWindow().getDecorView().postDelayed(() -> {
            if (preferences.getBoolean(getString(R.string.pk_default_read), false)) {
                startReadActivity();
            } else {
                startBookshelfActivity();
            }
            finish();
        }, 1200L);
    }

    private void startBookshelfActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void startReadActivity() {
        Intent intent = new Intent(this, ReadBookActivity.class);
        intent.putExtra("openFrom", ReadBookPresenterImpl.OPEN_FROM_APP);
        startActivity(intent);
    }

    @Override
    protected void initData() {

    }

}
