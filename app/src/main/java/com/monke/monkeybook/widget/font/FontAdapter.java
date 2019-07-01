package com.monke.monkeybook.widget.font;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FontAdapter extends Adapter<FontAdapter.MyViewHolder> {
    private final List<File> fileList = new ArrayList<>();
    private FontSelector.OnThisListener thisListener;
    private Context context;
    private String selectPath;

    FontAdapter(Context context, String selectPath, FontSelector.OnThisListener thisListener) {
        this.context = context;
        this.selectPath = selectPath;
        this.thisListener = thisListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (fileList.size() > 0) {
            final int realPosition = holder.getLayoutPosition();
            final File file = fileList.get(realPosition);
            if (fileNotExists(file, realPosition)) {
                return;
            }
            Typeface typeface = Typeface.createFromFile(file);
            holder.tvFont.setTypeface(typeface);
            holder.tvFont.setText(fileList.get(position).getName());
            if (TextUtils.equals(file.getAbsolutePath(), selectPath)) {
                holder.ivChecked.setChecked(true);
            } else {
                holder.ivChecked.setChecked(false);
            }
            holder.itemView.setOnClickListener(view -> {
                if (fileNotExists(file, realPosition)) {
                    ToastUtils.toast(context, "字体文件不存在");
                    return;
                }
                if (thisListener != null) {
                    thisListener.setFontPath(file.getAbsolutePath());
                }
            });
        } else {
            holder.ivChecked.setVisibility(View.GONE);
            holder.tvFont.setSingleLine(false);
            holder.tvFont.setText(R.string.fonts_folder);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size() == 0 ? 1 : fileList.size();
    }

    private boolean fileNotExists(File file, int position) {
        if (!file.exists()) {
            fileList.remove(file);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    public void upData(File[] files) {
        if (files != null) {
            fileList.clear();
            for (File file : files) {
                try {
                    Typeface.createFromFile(file);
                    fileList.add(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFont;
        RadioButton ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            tvFont = itemView.findViewById(R.id.tv_font);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }

}
