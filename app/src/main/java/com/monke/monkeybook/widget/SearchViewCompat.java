package com.monke.monkeybook.widget;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import com.monke.monkeybook.R;

public class SearchViewCompat {

    public static void useCustomIcon(SearchView searchView, String hint) {
        AppCompatImageView close = searchView.findViewById(R.id.search_close_btn);
        close.setImageResource(R.drawable.ic_close_black_24dp);
        close.setColorFilter(searchView.getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);

        AppCompatImageView search = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        search.setImageResource(R.drawable.ic_search_black_24dp_new);
        search.setColorFilter(searchView.getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);

        SearchView.SearchAutoComplete textView = searchView.findViewById(R.id.search_src_text);
        final int textSize = (int) (textView.getTextSize() * 1.25);
        Drawable mSearchHintIcon = searchView.getResources().getDrawable(R.drawable.ic_search_black_24dp_new);
        mSearchHintIcon.setBounds(0, 0, textSize, textSize);
        mSearchHintIcon.mutate();
        mSearchHintIcon.setColorFilter(searchView.getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
        final SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.setSpan(new ImageSpan(mSearchHintIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(hint);
        textView.setHint(ssb);
    }

}
