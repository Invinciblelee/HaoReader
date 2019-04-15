package com.monke.monkeybook.view.adapter.base;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.hwangjr.rxbus.RxBus;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseBookListAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final List<BookShelfBean> books;
    private Integer group;
    private int bookshelfPx;
    private Context context;
    private OnBookItemClickListenerTwo itemClickListener;


    private MyItemTouchHelpCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new MyItemTouchHelpCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int position) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(books, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            int start = srcPosition;
            int end = targetPosition;
            if (start > end) {
                start = targetPosition;
                end = srcPosition;
            }
            notifyItemRangeChanged(start, end - start + 1);
            return true;
        }

        @Override
        public void onRelease() {
            RxBus.get().post(RxBusTag.SAVE_BOOK_DATA, group);
        }
    };

    public MyItemTouchHelpCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public void setItemClickListener(OnBookItemClickListenerTwo itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    protected void onClick(View view, BookShelfBean bookShelf) {
        if (itemClickListener != null) {
            itemClickListener.onClick(view, bookShelf);
        }
    }

    protected void onLongClick(View view, BookShelfBean bookShelf) {
        if (itemClickListener != null) {
            itemClickListener.onLongClick(view, bookShelf);
        }
    }

    protected final int getBookshelfPx() {
        return bookshelfPx;
    }

    public final void setBookshelfPx(int bookshelfPx) {
        if (this.bookshelfPx != bookshelfPx) {
            this.bookshelfPx = bookshelfPx;
            if (!books.isEmpty()) {
                BookshelfHelp.order(books, bookshelfPx);
                notifyDataSetChanged();
            }
        }
    }

    protected final Context getContext() {
        return context;
    }

    public final List<BookShelfBean> getBooks() {
        return books;
    }

    public final BookShelfBean getItem(int position) {
        return books.isEmpty() ? null : books.get(position);
    }

    public BaseBookListAdapter(Context context, int group, int bookshelfPx) {
        this.context = context;
        this.group = group;
        this.bookshelfPx = bookshelfPx;
        books = new ArrayList<>();
    }

    public synchronized boolean updateBook(BookShelfBean bookShelf, boolean sort) {
        if (bookShelf != null) {
            for (int i = 0, size = books.size(); i < size; i++) {
                if (TextUtils.equals(books.get(i).getNoteUrl(), bookShelf.getNoteUrl())) {
                    books.set(i, bookShelf);
                    if (sort) {
                        BookshelfHelp.order(books, bookshelfPx);
                        notifyDataSetChanged();
                    } else {
                        notifyItemChanged(i, 0);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void addBook(BookShelfBean bookShelf) {
        if (bookShelf != null && !updateBook(bookShelf, true)) {
            books.add(bookShelf);
            BookshelfHelp.order(books, bookshelfPx);
            notifyDataSetChanged();
        }
    }

    public synchronized void removeBook(BookShelfBean bookShelf) {
        if (bookShelf == null || books.isEmpty()) {
            return;
        }

        int index = -1;
        for (int i = 0, size = books.size(); i < size; i++) {
            if (TextUtils.equals(books.get(i).getNoteUrl(), bookShelf.getNoteUrl())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            books.remove(index);
            notifyItemRemoved(index);
        }

    }

    public synchronized void sort() {
        if (!books.isEmpty()) {
            BookshelfHelp.order(books, bookshelfPx);
            notifyDataSetChanged();
        }
    }

    public synchronized void replaceAll(List<BookShelfBean> newDataS) {
        books.clear();
        if (null != newDataS && newDataS.size() > 0) {
            BookshelfHelp.order(newDataS, bookshelfPx);
            books.addAll(newDataS);
        }
        notifyDataSetChanged();
    }

    public synchronized void clear() {
        books.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
