package com.monke.monkeybook.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.R;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.view.activity.SettingActivity;

/**
 * Created by GKF on 2017/12/16.
 * 设置
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingActivity settingActivity;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("CONFIG");
        addPreferencesFromResource(R.xml.pref_settings);
        mContext = this.getActivity();
        settingActivity = (SettingActivity) this.getActivity();

        bindPreferenceSummaryToValue(findPreference(mContext.getString(R.string.pk_bookshelf_px)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View rootView = getView();
        if (rootView != null) {
            ListView listView = rootView.findViewById(android.R.id.list);
            if (listView != null) {
                listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                listView.setVerticalScrollBarEnabled(false);
                listView.setDivider(null);
                listView.setDividerHeight(0);
            }
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (Preference preference, Object value) -> {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            // For all other preferences, set the summary to the value's
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                preference.getContext().getSharedPreferences("CONFIG", Context.MODE_PRIVATE).getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pk_ImmersionStatusBar))) {
            settingActivity.initImmersionBar();
            RxBus.get().post(RxBusTag.IMMERSION_CHANGE, true);
        } else if (key.equals(getString(R.string.pk_bookshelf_px))) {
            String value = sharedPreferences.getString(key, "0");
            if (value != null) {
                Integer bookPx = Integer.parseInt(value);
                RxBus.get().post(RxBusTag.UPDATE_BOOK_PX, bookPx);
            }
        } else if (key.equals(getResources().getString(R.string.pk_chapter_disk_cache))) {
            DbHelper.getInstance().getDaoSession().getChapterBeanDao().detachAll();
        } else if (key.equals(getString(R.string.pk_show_all_find))) {
            RxBus.get().post(RxBusTag.FIND_LIST_CHANGE, true);
        }
    }
}
