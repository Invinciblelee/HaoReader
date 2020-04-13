//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.utils.NumberUtil;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.List;

public class ChapterListAdapter extends BaseChapterListAdapter<ChapterBean> {

    private BookShelfBean mBook;
    private int mIndex = -1;

    private final Object lock = new Object();

    public ChapterListAdapter(Context context) {
        super(context);
    }

    public void upChapter(int position) {
        synchronized (lock) {
            if (mBook != null && mBook.getChapterList().size() > position) {
                notifyItemChanged(position, 0);
            }
        }
    }

    public void upChapterIndex(int index) {
        synchronized (lock) {
            if (this.mIndex != index) {
                if (this.mIndex != -1) {
                    notifyItemChanged(this.mIndex, 0);
                }
                this.mIndex = NumberUtil.makeInRange(0, getItemCount() - 1, index);
                notifyItemChanged(this.mIndex, 0);
            }
        }
    }

    public void setBook(BookShelfBean book) {
        synchronized (lock) {
            boolean changed = mBook == null
                    || !TextUtils.equals(mBook.getNoteUrl(), book.getNoteUrl())
                    || mBook.getChapterList().size() != book.getChapterList().size()
                    || BookShelfBean.LOCAL_TAG.equals(book.getTag());
            this.mBook = book;
            if (changed) {
                setDataList(mBook.getChapterList());
            }
        }
    }

    public int getIndex() {
        return mIndex;
    }

    public void restoreIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        final int realPosition = holder.getLayoutPosition();
        final ChapterBean chapterBean = getItem(realPosition);
        if (payloads.size() > 0) {
            if (isCached(chapterBean)) {
                setBoldText(holder, true);
            }

            if (this.mIndex == realPosition) {
                setTextTint(holder, R.color.colorAccent);
            } else {
                setTextTint(holder, R.color.color_chapter_item);
            }
            return;
        }

        holder.tvName.setText(chapterBean.getDisplayDurChapterName());

        if (isCached(chapterBean)) {
            setBoldText(holder, true);
        } else {
            setBoldText(holder, false);
        }

        if (realPosition == mIndex) {
            setTextTint(holder, R.color.colorAccent);
        } else {
            setTextTint(holder, R.color.color_chapter_item);
        }

        holder.llName.setOnClickListener(v -> {
            upChapterIndex(realPosition);
            callOnItemClickListener(chapterBean);
        });
    }

    private boolean isCached(ChapterBean chapterBean) {
        if (chapterBean == null || mBook == null) {
            return false;
        }
        return mBook.isLocalBook() || chapterBean.getHasCache(mBook.getBookInfoBean());
    }

    private void setBoldText(ThisViewHolder holder, boolean bold) {
        holder.tvName.setSelected(bold);
        holder.tvName.getPaint().setFakeBoldText(bold);
    }

    private void setTextTint(ThisViewHolder holder, @ColorRes int tint) {
        ColorStateList color = holder.tvName.getResources().getColorStateList(tint);
        holder.tvName.setTextColor(color);
    }
}
