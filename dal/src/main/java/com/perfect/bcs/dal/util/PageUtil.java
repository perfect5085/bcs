package com.perfect.bcs.dal.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuhong.common.shared.api.Pager;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
public class PageUtil {

    public static <T> Pager<T> convert(Page<T> page) {
        Pager<T> resultPage = new Pager();
        resultPage.setPageIndex((int) page.getCurrent());
        resultPage.setPageSize((int) page.getSize());
        resultPage.setList(page.getRecords());
        resultPage.setTotalCount(page.getTotal());
        return resultPage;
    }

    public static <S, T> Pager<T> copySimple(Pager<S> pager) {
        Pager<T> resultPager = new Pager();
        resultPager.setPageIndex(pager.getPageIndex());
        resultPager.setPageSize(pager.getPageSize());
        resultPager.setTotalCount(pager.getTotalCount());
        return resultPager;
    }
}
