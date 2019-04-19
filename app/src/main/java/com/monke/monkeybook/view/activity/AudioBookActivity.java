package com.monke.monkeybook.view.activity;

import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.presenter.AudioBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.AudioBookContract;
import com.monke.monkeybook.service.AudioBookPlayService;
import com.monke.monkeybook.view.adapter.BookShelfListAdapter;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.widget.refreshview.SwipeRefreshLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioBookActivity extends MBaseActivity<AudioBookContract.Presenter> implements AudioBookContract.View, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;
    @BindView(R.id.iv_image_cover)
    ImageView ivCover;

    private BookShelfListAdapter bookListAdapter;

    @Override
    protected AudioBookContract.Presenter initInjector() {
        return new AudioBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_audio_book);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        setupActionBar();

        rvBookshelf.setHasFixedSize(true);

        int bookPx = mPresenter.getBookshelfPx();
        bookListAdapter = new BookShelfListAdapter(getContext(), 4, bookPx);
        rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));

        if (bookPx == 2) {
            MyItemTouchHelpCallback itemTouchHelpCallback = new MyItemTouchHelpCallback();
            itemTouchHelpCallback.setDragEnable(true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelpCallback);
            itemTouchHelper.attachToRecyclerView(rvBookshelf);
            itemTouchHelpCallback.setOnItemTouchCallbackListener(bookListAdapter.getItemTouchCallbackListener());
        }

        rvBookshelf.setAdapter(bookListAdapter);
        refreshLayout.setOnRefreshListener(this);

        showCoverImage("dfa");
    }

    @Override
    protected void bindEvent() {
        ivCover.setOnClickListener(v -> AudioBookPlayActivity.startThis(AudioBookActivity.this, v));


        bookListAdapter.setItemClickListener(new OnBookItemClickListenerTwo() {
            @Override
            public void onClick(View view, BookShelfBean bookShelf) {
                AudioBookPlayService.start(AudioBookActivity.this, bookShelf);
            }

            @Override
            public void onLongClick(View view, BookShelfBean bookShelf) {
                BookDetailActivity.startThis(AudioBookActivity.this, bookShelf);
            }
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.loadAudioBooks(false);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.audio_book);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mPresenter.loadAudioBooks(true);
    }

    @Override
    public void showAudioBooks(List<BookShelfBean> bookShelfBeans) {
        boolean isEmptyBefore = bookListAdapter.getItemCount() == 0;

        bookListAdapter.replaceAll(bookShelfBeans);

        if (!isEmptyBefore) {
            rvBookshelf.scrollToPosition(0);
        } else {
            startLayoutAnimationIfNeed();
        }
    }

    @Override
    public void addBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.addBook(bookShelfBean);
    }

    @Override
    public void removeBookShelf(BookShelfBean bookShelfBean) {
        bookListAdapter.removeBook(bookShelfBean);
    }

    @Override
    public void updateBook(BookShelfBean bookShelfBean, boolean b) {
        bookListAdapter.updateBook(bookShelfBean, b);
    }

    @Override
    public void sortBookShelf() {
        bookListAdapter.sort();
    }

    @Override
    public void refreshFinish() {
        refreshLayout.stopRefreshing();
    }

    private void startLayoutAnimationIfNeed() {
        if (mPresenter.getNeedAnim()) {
            if (rvBookshelf.getLayoutAnimation() == null) {
                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.anim_bookshelf_layout);
                rvBookshelf.setLayoutAnimation(animation);
            } else {
                rvBookshelf.startLayoutAnimation();
            }
        }
    }

    private void showCoverImage(String image) {
        Glide.with(this).load(R.drawable.img_cover_default)
                .apply(new RequestOptions().dontAnimate().centerCrop()
                        .transforms(new CenterCrop(), new CircleCrop())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.shape_bg_circle_image)
                        .error(R.drawable.shape_bg_circle_image)).into(ivCover);
    }
}
