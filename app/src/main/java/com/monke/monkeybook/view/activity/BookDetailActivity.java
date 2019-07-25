//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.AppActivityManager;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.help.BitIntentDataManager;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.presenter.BookDetailPresenterImpl;
import com.monke.monkeybook.presenter.contract.BookDetailContract;
import com.monke.monkeybook.utils.StringUtils;
import com.monke.monkeybook.view.fragment.dialog.ChangeSourceDialog;
import com.monke.monkeybook.widget.RotateLoading;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.monke.basemvplib.NetworkUtil.isNetworkAvailable;
import static com.monke.monkeybook.presenter.BookDetailPresenterImpl.FROM_BOOKSHELF;

public class BookDetailActivity extends MBaseActivity<BookDetailContract.Presenter> implements BookDetailContract.View {
    @BindView(R.id.ifl_content)
    FrameLayout flContent;
    @BindView(R.id.card_content)
    CardView cardView;
    @BindView(R.id.iv_blur_cover)
    ImageView ivBlurCover;
    @BindView(R.id.cover_card)
    View cardCover;
    @BindView(R.id.iv_cover)
    ImageView ivCover;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_author)
    TextView tvAuthor;
    @BindView(R.id.tv_update_switch)
    TextView tvUpdateSw;
    @BindView(R.id.ll_origin)
    View llOrigin;
    @BindView(R.id.tv_origin)
    TextView tvOrigin;
    @BindView(R.id.tv_change_origin)
    TextView tvChangeOrigin;
    @BindView(R.id.ll_book_recent)
    View llBookRecent;
    @BindView(R.id.tv_chapter)
    TextView tvChapter;
    @BindView(R.id.tv_chapter_last)
    TextView tvLastChapter;
    @BindView(R.id.tv_intro)
    TextView tvIntro;
    @BindView(R.id.ll_loading)
    View llLoading;
    @BindView(R.id.tv_read)
    TextView tvRead;
    @BindView(R.id.tv_remove_shelf)
    TextView tvRemoveShelf;
    @BindView(R.id.rl_loading)
    RotateLoading progressBar;
    @BindView(R.id.tv_loading_msg)
    TextView tvLoadingMsg;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.rg_book_group)
    RadioGroup rgGroup;

    private Animation animHideLoading;
    private Animation animShowInfo;

    public static void startThis(MBaseActivity activity, BookShelfBean bookShelf) {
        Intent intent = new Intent(activity, BookDetailActivity.class);
        intent.putExtra("openFrom", BookDetailPresenterImpl.FROM_BOOKSHELF);
        String key = String.valueOf(System.currentTimeMillis());
        intent.putExtra("data_key", key);
        BitIntentDataManager.getInstance().putData(key, bookShelf.copy());
        activity.startActivityByAnim(intent, R.anim.anim_alpha_in, 0);
    }

    public static void startThis(MBaseActivity activity, SearchBookBean searchBook) {
        Intent intent = new Intent(activity, BookDetailActivity.class);
        intent.putExtra("openFrom", BookDetailPresenterImpl.FROM_SEARCH);
        intent.putExtra("data", searchBook);
        activity.startActivityByAnim(intent, R.anim.anim_alpha_in, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected BookDetailContract.Presenter initInjector() {
        return new BookDetailPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPresenter.getBookShelf() != null) {
            String key = String.valueOf(System.currentTimeMillis());
            getIntent().putExtra("data_key", key);
            BitIntentDataManager.getInstance().putData(key, mPresenter.getBookShelf());
        }
    }

    @Override
    public void initImmersionBar() {
        mImmersionBar.transparentStatusBar();

        mImmersionBar.navigationBarColor(R.color.colorNavigationBar);

        if (canNavigationBarLightFont()) {
            mImmersionBar.navigationBarDarkIcon(false);
        }

        mImmersionBar.statusBarDarkFont(false);

        mImmersionBar.init();
    }

    @Override
    protected void initData() {
        mPresenter.initData(getIntent());
        animShowInfo = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        animHideLoading = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animHideLoading.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void changeUpdateSwitch(boolean off) {
        if (off) {
            tvUpdateSw.setText(R.string.update_on);
        } else {
            tvUpdateSw.setText(R.string.update_off);
        }
    }

    @Override
    public void getBookShelfError(boolean refresh) {
        llLoading.setVisibility(View.VISIBLE);
        tvLoadingMsg.setText("加载失败，点击重试");
        progressBar.hide();
        llLoading.setEnabled(true);
        llLoading.setOnClickListener(v -> {
            showLoading(true);
            mPresenter.loadBookShelfInfo(refresh);
        });
    }

    @Override
    protected void firstRequest() {
        if (mPresenter.getOpenFrom() == BookDetailPresenterImpl.FROM_SEARCH) {
            //网络请求
            mPresenter.loadBookShelfInfo(false);
        }
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);

        final int screenHeight = getResources().getDisplayMetrics().heightPixels;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cardView.getLayoutParams();
        params.height = (int) (screenHeight * 0.7f);
        cardView.setLayoutParams(params);

        if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
            if (mPresenter.getBookShelf() == null) return;
            updateView(true);
        } else {
            if (mPresenter.getSearchBook() == null) return;
            showCoverImage(mPresenter.getSearchBook().getCoverUrl());
            llBookRecent.setVisibility(View.GONE);
            tvName.setText(mPresenter.getSearchBook().getName());
            tvAuthor.setText(mPresenter.getSearchBook().getAuthor());
            if (TextUtils.isEmpty(tvAuthor.getText())) {
                tvAuthor.setText(R.string.author_unknown);
            }
            if (mPresenter.getSearchBook().getOrigin() != null && mPresenter.getSearchBook().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText(mPresenter.getSearchBook().getOrigin());
            } else {
                tvOrigin.setVisibility(View.INVISIBLE);
            }
            String lastChapterName = mPresenter.getSearchBook().getDisplayLastChapter();
            if (TextUtils.isEmpty(lastChapterName)) {
                tvLastChapter.setText(getString(R.string.book_search_last, getString(R.string.text_placeholder)));
            } else {
                tvLastChapter.setText(lastChapterName);
            }
            tvIntro.setVisibility(View.INVISIBLE);
            llBookRecent.setVisibility(View.GONE);
            showHideViews(mPresenter.getSearchBook().getBookType(), false);
            showLoading(true);
        }

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_dialog_in);
        cardView.post(() -> cardView.startAnimation(animation));
    }

    @Override
    public void updateView(boolean stopLoading) {
        if (null != mPresenter.getBookShelf()) {
            String bookType = mPresenter.getBookShelf().getBookInfoBean().getBookType();

            showCoverImage(mPresenter.getBookShelf().getBookInfoBean().getRealCoverUrl());
            showHideViews(bookType, mPresenter.getBookShelf().isLocalBook());
            changeUpdateSwitch(mPresenter.getBookShelf().getUpdateOff());

            if (mPresenter.inBookShelf()) {
                if (rgGroup.getVisibility() == View.VISIBLE) {
                    int index = mPresenter.getBookShelf().getGroup();
                    if (index < 0 || index >= rgGroup.getChildCount()) {
                        rgGroup.setVisibility(View.GONE);
                    } else {
                        ((RadioButton) rgGroup.getChildAt(index)).setChecked(true);
                    }
                }
                llBookRecent.setVisibility(View.VISIBLE);
                String durChapterName = mPresenter.getBookShelf().getDisplayDurChapterName();
                if (!TextUtils.isEmpty(durChapterName)) {
                    tvChapter.setText(durChapterName);
                } else {
                    tvChapter.setText(getString(TextUtils.equals(bookType, BookType.AUDIO) ?
                            R.string.play_dur_progress : R.string.read_dur_progress, getString(R.string.text_placeholder)));
                }
                tvRemoveShelf.setText(R.string.remove_shelf);
            } else {
                llBookRecent.setVisibility(View.GONE);
                tvRemoveShelf.setText(R.string.add_shelf);
            }

            if (TextUtils.isEmpty(tvName.getText())) {
                tvName.setText(mPresenter.getBookShelf().getBookInfoBean().getName());
            }
            tvAuthor.setText(mPresenter.getBookShelf().getBookInfoBean().getAuthor());
            if (TextUtils.isEmpty(tvAuthor.getText())) {
                tvAuthor.setText(R.string.author_unknown);
            }

            String lastChapterName = mPresenter.getBookShelf().getDisplayLastChapterName();
            if (TextUtils.isEmpty(lastChapterName) && mPresenter.getSearchBook() != null) {
                lastChapterName = mPresenter.getSearchBook().getLastChapter();
            }
            if (!TextUtils.isEmpty(lastChapterName)) {
                tvLastChapter.setText(lastChapterName);
            } else {
                tvLastChapter.setText(getString(R.string.book_search_last, getString(R.string.text_placeholder)));
            }

            String introduce = mPresenter.getBookShelf().getBookInfoBean().getIntroduce();
            tvIntro.setText(StringUtils.isBlank(introduce) ? null : introduce);

            if (tvIntro.getVisibility() != View.VISIBLE) {
                tvIntro.setVisibility(View.VISIBLE);
                tvIntro.startAnimation(animShowInfo);
            }
        }
        if (stopLoading) {
            showLoading(false);
        }
    }

    @Override
    protected void bindEvent() {
        flContent.setOnClickListener(v -> finish());

        rgGroup.setOnCheckedChangeListener((group, checkedId) -> {
            View checkView = rgGroup.findViewById(checkedId);
            if (!checkView.isPressed()) {
                return;
            }
            int idx = rgGroup.indexOfChild(checkView);
            mPresenter.getBookShelf().setGroup(idx);
            if (mPresenter.inBookShelf()) {
                mPresenter.addToBookShelf();
            }
        });

        tvChangeOrigin.setOnClickListener(view -> changeSource());

        tvRead.setOnClickListener(v -> startForBookType(mPresenter.getBookShelf().getBookInfoBean().getBookType()));

        tvRemoveShelf.setOnClickListener(v -> {
            if (!mPresenter.inBookShelf()) {
                mPresenter.addToBookShelf();
            } else {
                mPresenter.removeFromBookShelf();
            }
        });

        ivRefresh.setOnClickListener(view -> {
            if (llLoading.isShown()) {
                return;
            }
            AnimationSet animationSet = new AnimationSet(true);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            animationSet.addAnimation(rotateAnimation);
            ivRefresh.startAnimation(animationSet);
            showLoading(true);
            mPresenter.loadBookShelfInfo(true);
        });

        ivCover.setOnClickListener(view -> {
            if (mPresenter.getOpenFrom() == FROM_BOOKSHELF) {
                BookInfoActivity.startThis(this, mPresenter.getBookShelf().copy(), cardCover);
            }
        });

        tvUpdateSw.setOnClickListener(v -> mPresenter.switchUpdate(!mPresenter.getBookShelf().getUpdateOff()));

        tvAuthor.setOnClickListener(view -> {
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, tvAuthor.getText().toString());
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, tvAuthor.getText().toString());
            }
            finish();
        });

        tvName.setOnClickListener(view -> {
            if (!AppActivityManager.getInstance().isExist(SearchBookActivity.class)) {
                SearchBookActivity.startByKey(this, tvName.getText().toString());
            } else {
                RxBus.get().post(RxBusTag.SEARCH_BOOK, tvName.getText().toString());
            }
            finish();
        });
    }

    private void showHideViews(String bookType, boolean local) {
        if (local) {
            tvUpdateSw.setVisibility(View.GONE);
            rgGroup.setVisibility(View.GONE);
            ivRefresh.setVisibility(View.GONE);
            tvChangeOrigin.setVisibility(View.GONE);
            tvOrigin.setText(getString(R.string.local));
        } else {
            tvUpdateSw.setVisibility(View.VISIBLE);
            rgGroup.setVisibility(View.VISIBLE);
            ivRefresh.setVisibility(View.VISIBLE);
            tvChangeOrigin.setVisibility(View.VISIBLE);
            if (mPresenter.getBookShelf().getBookInfoBean().getOrigin() != null && mPresenter.getBookShelf().getBookInfoBean().getOrigin().length() > 0) {
                tvOrigin.setVisibility(View.VISIBLE);
                tvOrigin.setText(mPresenter.getBookShelf().getBookInfoBean().getOrigin());
            } else {
                tvOrigin.setVisibility(View.INVISIBLE);
            }
        }
        if (TextUtils.equals(bookType, BookType.AUDIO)) {
            tvRead.setText(R.string.start_listen);
            rgGroup.setVisibility(View.GONE);
        }
    }

    private void startForBookType(String bookType) {
        if (TextUtils.equals(bookType, BookType.TEXT)) {
            //进入阅读
            ReadBookActivity.startThis(BookDetailActivity.this, mPresenter.getBookShelf());
            finish();
            overridePendingTransition(R.anim.anim_right_in, R.anim.anim_alpha_out);
        } else if (TextUtils.equals(bookType, BookType.AUDIO)) {
            AudioBookPlayActivity.startThis(BookDetailActivity.this, cardCover, mPresenter.getBookShelf());
            finish();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            llLoading.setVisibility(View.VISIBLE);
            tvLoadingMsg.setText(R.string.data_loading);
            progressBar.show();
        } else {
            if (llLoading.getVisibility() == View.GONE) {
                return;
            }
            llLoading.startAnimation(animHideLoading);
        }
        llLoading.setOnClickListener(null);
        llLoading.setEnabled(false);
    }

    private void showCoverImage(String image) {
        Glide.with(this).load(image)
                .apply(new RequestOptions().dontAnimate().centerCrop()
                        .placeholder(R.drawable.img_cover_default)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(R.drawable.img_cover_default)).into(ivCover);

        Glide.with(this).load(image)
                .apply(new RequestOptions()
                        .dontAnimate()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.img_cover_gs)
                        .error(R.drawable.img_cover_gs))
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                .into(ivBlurCover);
    }

    private void changeSource() {
        if (!isNetworkAvailable()) {
            toast("网络不可用，无法换源");
            return;
        }
        ChangeSourceDialog.show(getSupportFragmentManager(), mPresenter.getBookShelf().getBookInfoBean(), false,
                searchBookBean -> {
                    tvOrigin.setText(searchBookBean.getOrigin());
                    if (!TextUtils.isEmpty(searchBookBean.getLastChapter())) {
                        tvLastChapter.setText(searchBookBean.getLastChapter());
                    } else {
                        tvLastChapter.setText(getString(R.string.book_search_last, getString(R.string.text_placeholder)));
                    }
                    showLoading(true);
                    mPresenter.changeBookSource(searchBookBean);
                });
    }

    @Override
    public void finish() {
        finishByAnim(0, R.anim.anim_alpha_out);
    }
}