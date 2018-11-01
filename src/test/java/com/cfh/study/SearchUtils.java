package com.cfh.study;

import org.apache.lucene.search.*;

import java.io.IOException;

/**
 * @Author: cfh
 * @Date: 2018/9/18 19:03
 * @Description: 查询的通用工具类
 */
public class SearchUtils {

    public static TopDocs getScoreDocsByPerPageAndSortField(IndexSearcher searcher, Query query, int first, int max, Sort sort) {
        try {
            if (query == null) {
                System.out.println(" Query is null return null ");
                return null;
            }
            TopFieldCollector collector = null;
            if (sort != null) {
                collector = TopFieldCollector.create(sort, first + max, false, false, false);
            } else {
                SortField[] sortField = new SortField[1];
                sortField[0] = new SortField("createTime", SortField.Type.STRING, true);
                Sort defaultSort = new Sort(sortField);
                collector = TopFieldCollector.create(defaultSort, first + max, false, false, false);
            }
            searcher.search(query, collector);
            return collector.topDocs(first, max);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }
}
