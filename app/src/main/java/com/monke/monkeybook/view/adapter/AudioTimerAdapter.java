package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.monke.monkeybook.R;

import java.util.List;

public class AudioTimerAdapter extends RecyclerView.Adapter<AudioTimerAdapter.ThisViewHolder> {

    private final LayoutInflater inflater;

    private final String[] titles;
    private final int[] values;

    private int index;

    private OnItemSelectListener listener;

    public AudioTimerAdapter(Context context, OnItemSelectListener listener) {
        this.listener = listener;
        inflater = LayoutInflater.from(context);

        titles = context.getResources().getStringArray(R.array.audio_timer_s);
        values = context.getResources().getIntArray(R.array.audio_timer_value);
    }

    public void upIndexByValue(int value) {
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (value == values[i]) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            if (this.index != -1) {
                notifyItemChanged(this.index, false);
            }
            this.index = index;
            notifyItemChanged(index, true);
        }
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThisViewHolder(inflater.inflate(R.layout.item_audio_timer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            boolean check = (boolean) payloads.get(0);
            holder.checker.setChecked(check);
            return;
        }

        holder.tvTitle.setText(titles[position]);
        holder.checker.setChecked(position == index);
        holder.itemView.setOnClickListener(v -> {
            if (!holder.checker.isChecked()) {
                int value = values[position];
                upIndexByValue(value);
                if (listener != null) {
                    listener.onSelected(value);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        CheckBox checker;

        private ThisViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            checker = itemView.findViewById(R.id.checker_timer);
        }
    }

    public interface OnItemSelectListener {
        void onSelected(int timerMinute);
    }
}
