//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterBean;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.view.adapter.base.BaseChapterListAdapter;
import com.monke.monkeybook.widget.AppCompat;

import java.util.List;

import androidx.annotation.NonNull;

public class ChapterListAdapter extends BaseChapterListAdapter<ChapterBean> {

    private BookShelfBean mBook;
    private int mIndex;

    public ChapterListAdapter(Context context) {
        super(context);
    }

    public void upChapter(int position) {
        if (mBook.getChapterListSize() > position) {
            notifyItemChanged(position, 0);
        }
    }

    public void upChapterIndex(int index) {
        this.mIndex = index;
        notifyDataSetChanged();
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

        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
        if (payloads.size() > 0) {
            holder.tvName.setSelected(true);
            holder.indicator.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
            return;
        }
        final ChapterBean chapterBean = getItem(realPosition);
        if (chapterBean.getDurChapterIndex() == mIndex) {
            int color = holder.indicator.getResources().getColor(R.color.colorAccent);
            holder.tvName.setTextColor(color);
            AppCompat.setTint(holder.indicator, color);
        } else {
            ColorStateList colors = holder.indicator.getResources().getColorStateList(R.color.color_chapter_item);
            holder.tvName.setTextColor(colors);
            AppCompat.setTintList(holder.indicator, colors);
        }
        holder.tvName.setText(FormatWebText.trim(chapterBean.getDurChapterName()));
        if (TextUtils.equals(mBook.getTag(), BookShelfBean.LOCAL_TAG) || chapterBean.getHasCache(mBook.getBookInfoBean())) {
            holder.tvName.setSelected(true);
            holder.indicator.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
        } else {
            holder.tvName.setSelected(false);
            holder.indicator.setSelected(false);
            holder.tvName.getPaint().setFakeBoldText(false);
        }
        holder.llName.setOnClickListener(v -> {
            callOnItemClickListener(chapterBean);
        });
    }

}
