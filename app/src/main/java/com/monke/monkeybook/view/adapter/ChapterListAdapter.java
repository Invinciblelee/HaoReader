//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.AppConfigHelper;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.annotation.BookType;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.List;

public class ChapterListAdapter extends BaseChapterListAdapter<ChapterBean> {

    private BookShelfBean mBook;
    private int mIndex = -1;

    private OnRefreshChapterListener refreshChapterListener;

    public ChapterListAdapter(Context context) {
        super(context);
    }

    public synchronized void upChapter(int position) {
        if (mBook != null && mBook.getChapterList().size() > position) {
            notifyItemChanged(position, 0);
        }
    }

    public synchronized void upChapterIndex(int index) {
        if (getItemCount() == 0) {
            return;
        }
        if (this.mIndex != index) {
            if (this.mIndex != -1) {
                notifyItemChanged(this.mIndex, 0);
            }
            this.mIndex = index;
            notifyItemChanged(index, 0);
        }
    }

    public synchronized void setBook(BookShelfBean book) {
        if (book != null) {
            boolean changed = mBook == null || !TextUtils.equals(mBook.getTag(), book.getTag());
            this.mBook = book;
            if (changed) {
                setDataList(mBook.getChapterList());
            }
        }
    }

    public int getIndex() {
        return mIndex;
    }

    public void setOnRefreshChapterListener(OnRefreshChapterListener listener) {
        refreshChapterListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        final int realPosition = holder.getLayoutPosition();
        final ChapterBean chapterBean = getItem(realPosition);
        if (payloads.size() > 0) {
            if (isCached(chapterBean)) {
                setBoldText(holder, true);
            }

            if (this.mIndex == chapterBean.getDurChapterIndex()) {
                setTextTint(holder, R.color.colorAccent);
                setRefreshVisible(holder, true);
            } else {
                setTextTint(holder, R.color.color_chapter_item);
                setRefreshVisible(holder, false);
            }
            return;
        }
        if (realPosition == (reverseLayout() ? 0 : getItemCount() - 1)) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        holder.tvName.setText(FormatWebText.trim(chapterBean.getDurChapterName()));

        if (chapterBean.getDurChapterIndex() == mIndex) {
            setTextTint(holder, R.color.colorAccent);
            setRefreshVisible(holder, true);
        } else {
            setTextTint(holder, R.color.color_chapter_item);
            setRefreshVisible(holder, false);
        }
        if (isCached(chapterBean)) {
            setBoldText(holder, true);
        } else {
            setBoldText(holder, false);
        }
        holder.llName.setOnClickListener(v -> callOnItemClickListener(chapterBean));

        if (holder.btnRefresh.isShown()) {
            holder.btnRefresh.setOnClickListener(v -> {
                if (refreshChapterListener != null) {
                    refreshChapterListener.onRefreshChapter(chapterBean);
                }
            });
        }
    }

    private boolean isCached(ChapterBean chapterBean) {
        if (chapterBean == null || mBook == null) {
            return false;
        }
        return TextUtils.equals(mBook.getTag(), BookShelfBean.LOCAL_TAG) || chapterBean.getHasCache(mBook.getBookInfoBean());
    }

    private void setBoldText(ThisViewHolder holder, boolean bold) {
        holder.tvName.setSelected(bold);
        holder.tvName.getPaint().setFakeBoldText(bold);
    }

    private void setTextTint(ThisViewHolder holder, @ColorRes int tint) {
        ColorStateList color = holder.tvName.getResources().getColorStateList(tint);
        holder.tvName.setTextColor(color);
    }

    private void setRefreshVisible(ThisViewHolder holder, boolean visible) {
        if (mBook != null && !TextUtils.equals(mBook.getBookInfoBean().getBookType(), BookType.AUDIO)) {
            return;
        }
        holder.btnRefresh.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean reverseLayout() {
        return AppConfigHelper.get().getBoolean("isChapterReverse", false);
    }

    public interface OnRefreshChapterListener {

        void onRefreshChapter(ChapterBean chapterBean);

    }
}
