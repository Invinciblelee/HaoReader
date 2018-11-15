package com.monke.monkeybook.presenter.contract;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.RipeFile;

import java.util.List;

public interface FileSelectorContract {

    interface Presenter extends IPresenter {
        void init(AppCompatActivity activity);

        String getTitle();

        MediaType getMediaType();

        void sort(int orderIndex);

        void query(String query);

        void startLoad();
    }

    interface View extends IView {

        void showFabComplete();

        void showLoading();

        void hideLoading();

        void onLoadFinish(List<RipeFile> files);

        void showBigImage(android.view.View shareView, String url);

    }

    enum MediaType implements Parcelable {
        IMAGE, FIlE;

        MediaType() {
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<MediaType> CREATOR = new Creator<MediaType>() {
            @Override
            public MediaType createFromParcel(final Parcel source) {
                return MediaType.values()[source.readInt()];
            }

            @Override
            public MediaType[] newArray(final int size) {
                return new MediaType[size];
            }
        };
    }
}
