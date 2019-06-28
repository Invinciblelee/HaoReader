package com.monke.monkeybook.view.adapter.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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

    private final Object lock = new Object();


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

    protected void callOnItemClick(View view, BookShelfBean bookShelf) {
        if (itemClickListener != null) {
            itemClickListener.onClick(view, bookShelf);
        }
    }

    protected void callOnItemLongClick(View view, BookShelfBean bookShelf) {
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
                BookshelfHelp.sortBook(books, bookshelfPx);
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
        return books.get(position);
    }

    public BaseBookListAdapter(Context context, int group, int bookshelfPx) {
        this.context = context;
        this.group = group;
        this.bookshelfPx = bookshelfPx;
        books = new ArrayList<>();
    }

    public boolean updateBook(BookShelfBean bookShelf, boolean sort) {
        synchronized (lock) {
            if (bookShelf != null) {
                for (int i = 0, size = books.size(); i < size; i++) {
                    if (TextUtils.equals(books.get(i).getNoteUrl(), bookShelf.getNoteUrl())) {
                        books.set(i, bookShelf);
                        if (sort) {
                            BookshelfHelp.sortBook(books, bookshelfPx);
                            notifyDataSetChanged();
                        } else {
                            notifyItemChanged(i, 0);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addBook(BookShelfBean bookShelf) {
        synchronized (lock) {
            if (bookShelf != null && !updateBook(bookShelf, true)) {
                books.add(bookShelf);
                BookshelfHelp.sortBook(books, bookshelfPx);
                notifyDataSetChanged();
            }
        }
    }

    public void removeBook(BookShelfBean bookShelf) {
        synchronized (lock) {
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
    }

    public void sort() {
        synchronized (lock) {
            if (!books.isEmpty()) {
                BookshelfHelp.sortBook(books, bookshelfPx);
                notifyDataSetChanged();
            }
        }
    }

    public void replaceAll(List<BookShelfBean> newDataS) {
        synchronized (lock) {
            books.clear();
            if (null != newDataS && newDataS.size() > 0) {
                BookshelfHelp.sortBook(newDataS, bookshelfPx);
                books.addAll(newDataS);
            }
            notifyDataSetChanged();
        }
    }

    public void clear() {
        synchronized (lock) {
            books.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
