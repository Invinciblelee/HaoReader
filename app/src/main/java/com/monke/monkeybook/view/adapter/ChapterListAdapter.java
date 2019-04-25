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
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;

import java.util.List;

public class ChapterListAdapter extends BaseChapterListAdapter<ChapterBean> {

    private BookShelfBean mBook;
    private int mIndex = -1;


    public ChapterListAdapter(Context context) {
        super(context);
    }

    public synchronized void upChapter(int position) {
        if (mBook != null && mBook.getChapterList().size() > position) {
            notifyItemChanged(position, 0);
        }
    }

    public synchronized void upChapterIndex(int index) {
        if(getItemCount() == 0){
            return;
        }
        if (this.mIndex != index ) {
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
            } else {
                setTextTint(holder, R.color.color_chapter_item);
            }
            return;
        }
        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }

        holder.tvName.setText(FormatWebText.trim(chapterBean.getDurChapterName()));

        if (chapterBean.getDurChapterIndex() == mIndex) {
            setTextTint(holder, R.color.colorAccent);
        } else {
            setTextTint(holder, R.color.color_chapter_item);
        }
        if (isCached(chapterBean)) {
            setBoldText(holder, true);
        } else {
            setBoldText(holder, false);
        }
        holder.llName.setOnClickListener(v -> callOnItemClickListener(chapterBean));
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

}
