package com.monke.monkeybook.view.fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.basemvplib.BaseFragment;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.AudioPlayInfo;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.presenter.AudioBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.AudioBookContract;
import com.monke.monkeybook.service.AudioBookPlayService;
import com.monke.monkeybook.utils.ToastUtils;
import com.monke.monkeybook.view.activity.AudioBookPlayActivity;
import com.monke.monkeybook.view.activity.BookDetailActivity;
import com.monke.monkeybook.view.adapter.AudioBookAdapter;
import com.monke.monkeybook.view.adapter.base.OnBookItemClickListenerTwo;
import com.monke.monkeybook.widget.AudioPlayingButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioBookFragment extends BaseFragment<AudioBookContract.Presenter> implements AudioBookContract.View, FragmentTrigger {
    @BindView(R.id.rv_bookshelf)
    RecyclerView rvBookshelf;
    @BindView(R.id.view_audio_running)
    AudioPlayingButton runningView;

    private AudioBookAdapter bookListAdapter;

    @Override
    protected AudioBookContract.Presenter initInjector() {
        return new AudioBookPresenterImpl();
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_audio_book, container, false);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this, view);

        rvBookshelf.setHasFixedSize(true);

        bookListAdapter = new AudioBookAdapter(getContext());
        rvBookshelf.setLayoutManager(new LinearLayoutManager(getContext()));

        rvBookshelf.setAdapter(bookListAdapter);
    }

    @Override
    protected void bindEvent() {
        bookListAdapter.setItemClickListener(new OnBookItemClickListenerTwo() {
            @Override
            public void onClick(View view, BookShelfBean bookShelf) {
                AudioBookPlayActivity.startThis((MBaseActivity) getActivity(), null, bookShelf);
            }

            @Override
            public void onLongClick(View view, BookShelfBean bookShelf) {
                BookDetailActivity.startThis((MBaseActivity) getActivity(), bookShelf);
            }
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.loadAudioBooks(false);

        if (AudioBookPlayService.running) {
            AudioBookPlayService.start(requireContext());
        }
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
    public void toast(String msg) {
        ToastUtils.toast(requireContext(), msg);
    }

    @Override
    public void clearBookShelf() {
        bookListAdapter.clear();
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

    @Override
    public void onRefresh() {
        mPresenter.loadAudioBooks(true);
    }

    @Override
    public void onRestore() {
        mPresenter.loadAudioBooks(false);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public void onReselected() {
        rvBookshelf.scrollToPosition(0);
    }

    @Override
    public void onPlayEvent(AudioPlayInfo info) {
        switch (info.getAction()) {
            case AudioBookPlayService.ACTION_ATTACH:
                runningView.setCoverImage(info.getBookInfoBean().getRealCoverUrl());
                runningView.setProgress(info.getProgress(), info.getDuration());
                if (info.isPause()) {
                    runningView.setPause();
                } else {
                    runningView.setResume();
                }
                runningView.show();
                break;
            case AudioBookPlayService.ACTION_START:
                runningView.setResume();
                break;
            case AudioBookPlayService.ACTION_PAUSE:
                runningView.setPause();
                break;
            case AudioBookPlayService.ACTION_RESUME:
                runningView.setResume();
                break;
            case AudioBookPlayService.ACTION_PROGRESS:
                runningView.setProgress(info.getProgress(), info.getDuration());
                break;
            case AudioBookPlayService.ACTION_STOP:
                runningView.hide();
                runningView.stop();
                break;
        }
    }
}
