package com.monke.monkeybook.widget.page;

import com.monke.monkeybook.bean.ChapterBean;

import java.util.List;

public interface OnPageChangeListener {
    /**
     * 作用：章节切换的时候进行回调
     *
     * @param chapterIndex:切换章节的序号
     */
    void onChapterChange(int chapterIndex);

    /**
     * 作用：章节目录加载完成时候回调
     *
     * @param chapters：返回章节目录
     */
    void onCategoryFinish(List<ChapterBean> chapters);

    /**
     * 作用：章节页码数量改变之后的回调。==> 字体大小的调整，或者是否关闭虚拟按钮功能都会改变页面的数量。
     */
    void onPageCountChange(int count);

    /**
     * 作用：当页面改变的时候回调
     */
    void onPageChange(int chapterIndex, int pageIndex, int pageSize);
}