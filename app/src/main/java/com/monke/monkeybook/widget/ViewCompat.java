package com.monke.monkeybook.widget;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.FrameLayout;

import com.monke.monkeybook.R;

import java.lang.reflect.Field;

public class ViewCompat {

    public static void useCustomIconForSearchView(SearchView searchView, String hint) {
        AppCompatImageView close = searchView.findViewById(R.id.search_close_btn);
        close.setImageResource(R.drawable.ic_close_black_24dp);
        close.setColorFilter(searchView.getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);

        AppCompatImageView search = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        search.setImageResource(R.drawable.ic_search_black_24dp_new);
        search.setColorFilter(searchView.getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);

        setQueryHintForSearchView(searchView, hint);
    }


    public static void setQueryHintForSearchView(SearchView searchView, String hintText) {
        SearchView.SearchAutoComplete textView = searchView.findViewById(R.id.search_src_text);
        textView.setTextColor(searchView.getResources().getColor(R.color.tv_text_default));
        final int textSize = (int) (textView.getTextSize() * 1.25);
        Drawable mSearchHintIcon = searchView.getResources().getDrawable(R.drawable.ic_search_black_24dp_new);
        mSearchHintIcon.setBounds(0, 0, textSize, textSize);
        DrawableCompat.setTint(mSearchHintIcon.mutate(), textView.getCurrentHintTextColor());
        final SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.setSpan(new ImageSpan(mSearchHintIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(hintText);
        textView.setHint(ssb);
    }

    public static void setNavigationMenuLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
        try {
            Field fieldByPressenter = navigationView.getClass().getDeclaredField("presenter");
            fieldByPressenter.setAccessible(true);
            NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) fieldByPressenter.get(navigationView);
            Field fieldByMenuView = menuPresenter.getClass().getDeclaredField("menuView");
            fieldByMenuView.setAccessible(true);
            final NavigationMenuView mMenuView = (NavigationMenuView) fieldByMenuView.get(menuPresenter);
            mMenuView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(View view) {
                    RecyclerView.ViewHolder viewHolder = mMenuView.getChildViewHolder(view);
                    if (viewHolder != null && "SeparatorViewHolder".equals(viewHolder.getClass().getSimpleName())) {
                        if (viewHolder.itemView instanceof FrameLayout) {
                            FrameLayout frameLayout = (FrameLayout) viewHolder.itemView;
                            View line = frameLayout.getChildAt(0);
                            line.setBackgroundColor(color);
                            line.getLayoutParams().height = height;
                            line.setLayoutParams(line.getLayoutParams());
                        }
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(View view) {

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
