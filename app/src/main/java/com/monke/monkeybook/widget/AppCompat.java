package com.monke.monkeybook.widget;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ScreenUtils;

import java.lang.reflect.Field;

public class AppCompat {

    private AppCompat() {

    }

    public static void useCustomIconForSearchView(SearchView searchView, String hint, boolean showSearchHintIcon, boolean showBg) {
        final int normalColor = searchView.getResources().getColor(R.color.menu_color_default);
        AppCompatImageView search = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        search.setImageResource(R.drawable.ic_search_black_24dp);
        setTint(search, normalColor);

        SearchView.SearchAutoComplete searchText = searchView.findViewById(R.id.search_src_text);
        searchText.setTextSize(14f);
        searchText.setPaddingRelative(searchText.getPaddingLeft(), 0, 0, 0);

        final int textSize = (int) (searchText.getTextSize() * 1.18f);
        Drawable searchIcon = searchText.getResources().getDrawable(R.drawable.ic_search_black_24dp);
        searchIcon.setBounds(0, 0, textSize, textSize);
        setTint(searchIcon, normalColor);
        searchText.setCompoundDrawablesRelative(searchIcon, null, null, null);
        searchText.setCompoundDrawablePadding(ScreenUtils.dpToPx(5));
        searchText.setIncludeFontPadding(false);

        AppCompatImageView close = searchView.findViewById(R.id.search_close_btn);
        close.setImageResource(R.drawable.ic_close_black_24dp);
        setTint(close, normalColor);

        LinearLayout plate = searchView.findViewById(R.id.search_plate);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) plate.getLayoutParams();
        params.topMargin = ScreenUtils.dpToPx(7);
        params.bottomMargin = ScreenUtils.dpToPx(7);
        plate.setLayoutParams(params);

        plate.setPaddingRelative(ScreenUtils.dpToPx(6), 0, ScreenUtils.dpToPx(6), 0);

        if (showBg) {
            Drawable bag = searchView.getResources().getDrawable(R.drawable.bg_textfield_search);
            android.support.v4.view.ViewCompat.setBackground(plate, bag);
        } else {
            android.support.v4.view.ViewCompat.setBackground(plate, null);
        }

        setQueryHintForSearchText(searchText, hint, showSearchHintIcon);
    }

    public static void useCustomIconForSearchView(SearchView searchView, String hint) {
        useCustomIconForSearchView(searchView, hint, false, true);
    }

    private static ColorStateList createSearchPlateBagState(int activeColor, int normalColor) {
        int[] colors = new int[]{activeColor, activeColor, activeColor, normalColor, normalColor};
        int[][] states = new int[5][];
        states[0] = new int[]{android.R.attr.state_enabled, android.R.attr.state_focused};
        states[1] = new int[]{android.R.attr.state_enabled, android.R.attr.state_activated};
        states[2] = new int[]{android.R.attr.state_focused};
        states[3] = new int[]{android.R.attr.state_window_focused};
        states[4] = new int[]{};
        return new ColorStateList(states, colors);
    }

    public static void setQueryHintForSearchText(SearchView.SearchAutoComplete searchText, String hintText) {
        setQueryHintForSearchText(searchText, hintText, false);
    }


    public static void setQueryHintForSearchText(SearchView.SearchAutoComplete searchText, String hintText, boolean showIcon) {
        searchText.setTextColor(searchText.getResources().getColor(R.color.tv_text_default));
        if (showIcon) {
            final int textSize = (int) (searchText.getTextSize() * 1.25);
            Drawable mSearchHintIcon = searchText.getResources().getDrawable(R.drawable.ic_search_black_24dp);
            mSearchHintIcon.setBounds(0, 0, textSize, textSize);
            setTint(mSearchHintIcon, Color.GRAY);
            final SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
            ssb.setSpan(new ImageSpan(mSearchHintIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(hintText);
            searchText.setHint(ssb);
        } else {
            searchText.setHint(hintText);
        }
    }

    public static void setNavigationViewLineStyle(NavigationView navigationView, @ColorInt final int color, final int height) {
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

    public static void setTintList(Drawable drawable, ColorStateList tint, @NonNull PorterDuff.Mode tintMode) {
        if (drawable == null) return;
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, tintMode);
    }

    public static void setTintList(Drawable drawable, ColorStateList tint) {
        setTintList(drawable, tint, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setTintList(View view, ColorStateList tint) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            setTintList(drawable, tint);
        } else if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            for (Drawable drawable : drawables) {
                setTintList(drawable, tint);
            }
            drawables = ((TextView) view).getCompoundDrawablesRelative();
            setTintList(drawables[0], tint);
            setTintList(drawables[2], tint);
        }
    }

    public static void setTint(Drawable drawable, @ColorInt int tint, @NonNull PorterDuff.Mode tintMode) {
        if (drawable == null) return;
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrappedDrawable, tint);
        DrawableCompat.setTintMode(wrappedDrawable, tintMode);
    }

    public static void setTint(Drawable drawable, @ColorInt int tint) {
        setTint(drawable, tint, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setTint(View view, int color) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            setTint(drawable, color);
        } else if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            for (Drawable drawable : drawables) {
                setTint(drawable, color);
            }
            drawables = ((TextView) view).getCompoundDrawablesRelative();
            setTint(drawables[0], color);
            setTint(drawables[2], color);
        }
    }

    public static void setTint(MenuItem item, int color) {
        if (item != null && item.getIcon() != null) {
            setTint(item.getIcon(), color);
        }
    }

    public static void setToolbarNavIconTint(Toolbar toolbar, int color) {
        if (toolbar != null && toolbar.getNavigationIcon() != null) {
            setTint(toolbar.getNavigationIcon(), color);
        }
    }
}
