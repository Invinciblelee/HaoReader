package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.base.BaseBookListAdapter;
import com.monke.monkeybook.widget.RotateLoading;

import java.util.Locale;

public class AudioBookAdapter extends BaseBookListAdapter<AudioBookAdapter.MyViewHolder> {

    private final LayoutInflater mInflater;

    public AudioBookAdapter(Context context) {
        super(context, 4, 0);
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.item_audio_book, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        BookShelfBean item = getItem(holder.getLayoutPosition());

        Glide.with(getContext()).load(item.getBookInfoBean().getRealCoverUrl())
                .apply(new RequestOptions().dontAnimate()
                        .centerCrop().placeholder(R.drawable.img_cover_default)
                        .error(R.drawable.img_cover_default))
                .into(holder.ivCover);

        holder.tvName.setText(getBookName(item.getBookInfoBean().getName(), item.getNewChapters()));

        if (!TextUtils.isEmpty(item.getBookInfoBean().getAuthor())) {
            holder.tvAuthor.setText(item.getBookInfoBean().getAuthor());
        } else {
            holder.tvAuthor.setText(R.string.author_unknown);
        }

        String durChapterName = item.getDisplayDurChapterName();
        if (TextUtils.isEmpty(durChapterName)) {
            holder.tvRead.setText(getContext().getString(R.string.play_dur_progress, getContext().getString(R.string.text_placeholder)));
        } else {
            final String durProgress;
            if (item.getChapterListSize() != 0) {
                durProgress = String.format(Locale.getDefault(), "%d/%d | %s", item.getDurChapter() + 1, item.getChapterListSize(), durChapterName);
            } else {
                durProgress = getContext().getString(R.string.play_dur_progress, durChapterName);
            }
            holder.tvRead.setText(durProgress);
        }

        if (item.isFlag()) {
            holder.rotateLoading.show();
        } else {
            holder.rotateLoading.hide();
        }

        if (item.getHasUpdate() && !item.isLocalBook()) {
            holder.tvHasNew.setVisibility(View.VISIBLE);
        } else {
            holder.tvHasNew.setVisibility(View.INVISIBLE);
        }

        holder.content.setOnClickListener(v -> callOnItemClick(v, item));

        holder.content.setOnLongClickListener(v -> {
            callOnItemLongClick(v, item);
            return true;
        });

    }

    private SpannableStringBuilder getBookName(String name, int newChapters) {
        SpannableStringBuilder sbs = new SpannableStringBuilder(name);
        if (newChapters == 0) {
            return sbs;
        }
        SpannableString chaptersSpan = new SpannableString(String.format(Locale.getDefault(), "(新增%d章)", newChapters));
        chaptersSpan.setSpan(new RelativeSizeSpan(0.75f), 0, chaptersSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        chaptersSpan.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.colorTextSecondary)), 0, chaptersSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sbs.append(chaptersSpan);
        return sbs;
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvName;
        TextView tvAuthor;
        TextView tvRead;
        RotateLoading rotateLoading;
        TextView tvHasNew;
        View content;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvRead = itemView.findViewById(R.id.tv_read);
            rotateLoading = itemView.findViewById(R.id.rl_loading);
            tvHasNew = itemView.findViewById(R.id.tv_has_new);
            content = itemView.findViewById(R.id.content_card);
        }
    }

}
