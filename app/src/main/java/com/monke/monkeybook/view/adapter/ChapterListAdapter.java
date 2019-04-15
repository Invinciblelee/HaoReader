//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;
import com.monke.monkeybook.widget.AppCompat;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public class ChapterListAdapter extends BaseChapterListAdapter<ChapterBean> {

    private BookShelfBean mBook;
    private int mIndex = -1;

    public ChapterListAdapter(Context context) {
        super(context);
    }

    public void upChapter(int position) {
        if (mBook != null && mBook.getChapterList().size() > position) {
            notifyItemChanged(position, 0);
        }
    }

    public void upChapterIndex(int index) {
        if (this.mIndex != index) {
            if (this.mIndex != -1) {
                notifyItemChanged(this.mIndex, 1);
            }
            this.mIndex = index;
            notifyItemChanged(index, 2);
        }
    }

    public void setBook(BookShelfBean book) {
        if (book != null) {
            this.mBook = book;

            if (getItemCount() == 0) {
                setDataList(mBook.getChapterList());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        final int realPosition = holder.getLayoutPosition();
        final ChapterBean chapterBean = getItem(realPosition);
        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
        if (payloads.size() > 0) {
            int type = (int) payloads.get(0);
            if (type == 0) {
                setBoldText(holder, true);
            } else if (type == 1) {
                setTextTint(holder, R.color.color_chapter_item);
            } else if (type == 2) {
                setTextTint(holder, R.color.colorAccent);
            }
            return;
        }
        if (chapterBean.getDurChapterIndex() == mIndex) {
            setTextTint(holder, R.color.colorAccent);
        } else {
            setTextTint(holder, R.color.color_chapter_item);
        }
        holder.tvName.setText(FormatWebText.trim(chapterBean.getDurChapterName()));
        if (TextUtils.equals(mBook.getTag(), BookShelfBean.LOCAL_TAG) || chapterBean.getHasCache(mBook.getBookInfoBean())) {
            setBoldText(holder, true);
        } else {
            setBoldText(holder, false);
        }
        holder.llName.setOnClickListener(v -> callOnItemClickListener(chapterBean));
    }

    private void setBoldText(ThisViewHolder holder, boolean bold) {
        holder.tvName.setSelected(bold);
        holder.indicator.setSelected(bold);
        holder.tvName.getPaint().setFakeBoldText(bold);
    }

    private void setTextTint(ThisViewHolder holder, @ColorRes int tint) {
        ColorStateList color = holder.indicator.getResources().getColorStateList(tint);
        holder.tvName.setTextColor(color);
        AppCompat.setTintList(holder.indicator, color);
    }

}
