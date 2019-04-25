package com.monke.monkeybook.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.DownloadInfo;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.view.adapter.DownloadAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.monke.monkeybook.service.DownloadService.addDownloadAction;
import static com.monke.monkeybook.service.DownloadService.finishDownloadAction;
import static com.monke.monkeybook.service.DownloadService.obtainDownloadListAction;
import static com.monke.monkeybook.service.DownloadService.progressDownloadAction;
import static com.monke.monkeybook.service.DownloadService.removeDownloadAction;

public class DownloadActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private DownloadAdapter adapter;

    public static void startThis(Context context) {
        context.startActivity(new Intent(context, DownloadActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_vew);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.download_offline);
        }
    }

    @Override
    protected void firstRequest() {
        RxBus.get().register(this);

        DownloadService.obtainDownloadList(this);
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                DownloadService.cancelDownload(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.BOOK_DOWNLOAD)})
    public void onDownloadEvent(DownloadInfo downloadInfo) {
        String action = downloadInfo.getAction();
        switch (action) {
            case addDownloadAction:
                adapter.addData(downloadInfo.getDownloadBookBean());
                break;
            case removeDownloadAction:
                adapter.removeData(downloadInfo.getDownloadBookBean());
                break;
            case progressDownloadAction:
                adapter.upData(downloadInfo.getDownloadBookBean());
                break;
            case finishDownloadAction:
                adapter.upDataS(null);
                break;
            case obtainDownloadListAction:
                adapter.upDataS(downloadInfo.getDownloadBookBeans());
                break;

        }
    }

}
