package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.RipeFile;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FileHelp;
import com.monke.monkeybook.presenter.contract.FileSelectorContract;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RipeFile> files;

    private Context context;
    private LayoutInflater inflater;

    private FileSelectorContract.MediaType mediaType;

    private RipeFile selectedFile = null;

    public FileSelectorAdapter(Context context, FileSelectorContract.MediaType mediaType) {
        this.context = context;
        this.mediaType = mediaType;
        this.inflater = LayoutInflater.from(context);

        files = new ArrayList<>();
    }

    public void setItems(List<RipeFile> files){
        if(files != null && !files.isEmpty()){
            this.files = files;
        }else {
            this.files.clear();
        }
        notifyDataSetChanged();
    }

    public void clear(){

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(mediaType == FileSelectorContract.MediaType.IMAGE){
            return new ImageViewHolder(inflater.inflate(R.layout.item_image_selector, viewGroup, false));
        }
        return new FileViewHolder(inflater.inflate(R.layout.item_file_selector, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder thisViewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if(!payloads.isEmpty()){
            if (viewHolder instanceof FileViewHolder) {
                FileViewHolder holder = (FileViewHolder) viewHolder;
                holder.mChecker.setChecked(false);
            }else {
                ImageViewHolder holder = (ImageViewHolder) viewHolder;
                holder.mChecker.setChecked(false);
            }
        }else {
            final RipeFile item = files.get(viewHolder.getLayoutPosition());
            if (viewHolder instanceof FileViewHolder) {
                FileViewHolder holder = (FileViewHolder) viewHolder;
                holder.mTvName.setText(item.getName());
                holder.mTvTag.setText(item.getSuffix());
                holder.mTvSize.setText(FileHelp.getFileSize(item.getSize()));
                holder.mTvDate.setText(StringUtils.dateConvert(item.getDate(), Constant.FORMAT_FILE_DATE));

                if(selectedFile != null) {
                    holder.mChecker.setChecked(selectedFile.equals(item));
                }

                final View.OnClickListener clickListener = v -> {
                    if(!Objects.equals(selectedFile, item)){
                        int index;
                        if(selectedFile != null && (index = files.indexOf(selectedFile)) >= 0) {
                            notifyItemChanged(index, index);
                        }

                        selectedFile = item;

                        if(!holder.mChecker.isChecked()) {
                            holder.mChecker.setChecked(true);
                        }

                        if(context instanceof FileSelectorContract.View){
                            ((FileSelectorContract.View) context).showFabComplete();
                        }
                    }
                };

                holder.itemView.setOnClickListener(clickListener);
            } else {
                ImageViewHolder holder = (ImageViewHolder) viewHolder;

                if(selectedFile != null) {
                    holder.mChecker.setChecked(selectedFile.equals(item));
                }

                Glide.with(context).load(item.getPath())
                        .apply(new RequestOptions().centerCrop())
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(holder.mImage);

                holder.mChecker.setOnClickListener(v -> {
                    if(!Objects.equals(selectedFile, item)){
                        int index;
                        if(selectedFile != null && (index = files.indexOf(selectedFile)) >= 0) {
                            notifyItemChanged(index, index);
                        }

                        selectedFile = item;

                        if(context instanceof FileSelectorContract.View){
                            ((FileSelectorContract.View) context).showFabComplete();
                        }
                    }
                });

                holder.itemView.setOnClickListener(v -> {
                    if(context instanceof FileSelectorContract.View){
                        ((FileSelectorContract.View) context).showBigImage(holder.mImage, item.getPath());
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public RipeFile getSelectedFile(){
        return selectedFile;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvName;
        private TextView mTvTag;
        private TextView mTvSize;
        private TextView mTvDate;
        private RadioButton mChecker;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);

            mChecker = itemView.findViewById(R.id.file_rb_checker);
            mTvName = itemView.findViewById(R.id.file_tv_name);
            mTvTag = itemView.findViewById(R.id.file_tv_tag);
            mTvSize = itemView.findViewById(R.id.file_tv_size);
            mTvDate = itemView.findViewById(R.id.file_tv_date);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder{

        private RadioButton mChecker;
        private ImageView mImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mChecker = itemView.findViewById(R.id.image_rb_checker);
            mImage = itemView.findViewById(R.id.image_album);
        }
    }
}
