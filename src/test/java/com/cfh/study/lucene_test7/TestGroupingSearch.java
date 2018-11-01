package com.cfh.study.lucene_test7;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/17 11:05
 * @Description:
 */
public class TestGroupingSearch {
    //索引目录
    static String indexDir = "/Users/chenfeihao/Desktop/lucene/index5";
    static Analyzer analyzer = new StandardAnalyzer();
    //指定在哪个索引上进行分组
    static String groupField = "author";

    @Test
    public void mainTest() throws Exception {
        //createIndex();
//        Directory directory = FSDirectory.open(Paths.get(indexDir));
//        IndexReader reader = DirectoryReader.open(directory);
//        IndexSearcher searcher = new IndexSearcher(reader);
//        Query query = new TermQuery(new Term("content", "random"));
//        /**每个分组内部的排序规则*/
//        Sort groupSort = Sort.RELEVANCE;
        //groupBy(searcher, query, groupSort);
    }


    /**
     * 创建测试用的索引文档
     *
     * @throws IOException
     */
    public static void createIndex() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
        addDocuments(groupField, writer);
    }

    /**
     * 添加索引文档
     *
     * @param groupField
     * @param writer
     * @throws IOException
     */
    public static void addDocuments(String groupField, IndexWriter writer)
            throws IOException {
        // 0
        Document doc = new Document();
        addGroupField(doc, groupField, "author1");
        doc.add(new StringField("author", "author1", Field.Store.YES));
        doc.add(new TextField("content", "random text", Field.Store.YES));
        doc.add(new StringField("id", "1", Field.Store.YES));
        writer.addDocument(doc);

        // 1
        doc = new Document();
        addGroupField(doc, groupField, "author1");
        doc.add(new StringField("author", "author1", Field.Store.YES));
        doc.add(new TextField("content", "some more random text",
                Field.Store.YES));
        doc.add(new StringField("id", "2", Field.Store.YES));
        writer.addDocument(doc);

        // 2
        doc = new Document();
        addGroupField(doc, groupField, "author1");
        doc.add(new StringField("author", "author1", Field.Store.YES));
        doc.add(new TextField("content", "some more random textual data",
                Field.Store.YES));
        doc.add(new StringField("id", "3", Field.Store.YES));
        writer.addDocument(doc);

        // 3
        doc = new Document();
        addGroupField(doc, groupField, "author2");
        doc.add(new StringField("author", "author2", Field.Store.YES));
        doc.add(new TextField("content", "some random text", Field.Store.YES));
        doc.add(new StringField("id", "4", Field.Store.YES));
        writer.addDocument(doc);

        // 4
        doc = new Document();
        addGroupField(doc, groupField, "author3");
        doc.add(new StringField("author", "author3", Field.Store.YES));
        doc.add(new TextField("content", "some more random text",
                Field.Store.YES));
        doc.add(new StringField("id", "5", Field.Store.YES));
        writer.addDocument(doc);

        // 5
        doc = new Document();
        addGroupField(doc, groupField, "author3");
        doc.add(new StringField("author", "author3", Field.Store.YES));
        doc.add(new TextField("content", "random", Field.Store.YES));
        doc.add(new StringField("id", "6", Field.Store.YES));
        writer.addDocument(doc);

        // 6 -- no author field
        doc = new Document();
        doc.add(new StringField("author", "author4", Field.Store.YES));
        doc.add(new TextField("content",
                "random word stuck in alot of other text", Field.Store.YES));
        doc.add(new StringField("id", "6", Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        writer.close();
    }

    /**
     * 添加分组域
     *
     * @param doc        索引文档
     * @param groupField 需要分组的域名称
     * @param value      域值
     */
    private static void addGroupField(Document doc, String groupField,
                                      String value) {
        //进行分组的域上建立的必须是SortedDocValuesField类型
        doc.add(new SortedDocValuesField(groupField, new BytesRef(value)));
    }

    /**
     * 测试lucene7环境下的分组查询
     */
    @Test
    public void lucene7GroupBy() throws Exception {
        GroupingSearch groupingSearch = new GroupingSearch(groupField);//指定要进行分组的索引
        groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));//指定分组排序规则
        groupingSearch.setFillSortFields(true);//是否填充SearchGroup的sortValues
        groupingSearch.setCachingInMB(4.0, true);
        groupingSearch.setAllGroups(true);
        //groupingSearch.setAllGroupHeads(true);
        groupingSearch.setGroupDocsLimit(10);//限制分组个数

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("content", analyzer);
        String queryExpression = "some content";
        Query query = parser.parse(queryExpression);
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        //在content索引上对包含some与content分词的索引进行具体查询，结果按照author索引的内容进行分组
        TopGroups<BytesRef> result = groupingSearch.search(searcher, query, 0, 1000);

        //总命中数
        System.out.println("总命中数:" + result.totalHitCount);
        //分组数
        System.out.println("分组数:" + result.groups.length);
        //按照分组打印查询结果
        for (GroupDocs<BytesRef> groupDocs : result.groups) {
            if (groupDocs != null) {
                if (groupDocs.groupValue != null) {
                    System.out.println("分组:" + groupDocs.groupValue.utf8ToString());
                } else {
                    //由于建立索引时有一条数据没有在分组索引上建立SortedDocValued索引，因此这个分组的groupValue为null
                    System.out.println("分组:" + "unknow");
                }
                System.out.println("组内数据条数:" + groupDocs.totalHits);

                for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                    System.out.println("author:" + searcher.doc(scoreDoc.doc).get("author"));
                    System.out.println("content:" + searcher.doc(scoreDoc.doc).get("content"));
                    System.out.println();
                }

                System.out.println("=====================================");
            }
        }
    }

    /**
     * 进行分组查询
     * 分组查询的API lucene7.xY与lucene5.x不同
     * @param searcher
     * @param query
     * @param groupSort
     * @throws Exception
     */
//    public static void groupBy(IndexSearcher searcher, Query query, Sort groupSort) throws Exception {
//        /** 前N条中分组 */
//        int topNGroups = 10;
//        /** 分组起始偏移量 */
//        int groupOffset = 0;
//        /** 是否填充SearchGroup的sortValues */
//        boolean fillFields = true;
//        /** groupSort用于对组进行排序，docSort用于对组内记录进行排序，多数情况下两者是相同的，但也可不同 */
//        Sort docSort = groupSort;
//        /** 用于组内分页，起始偏移量 */
//        int docOffset = 0;
//        /** 每组返回多少条结果 */
//        int docsPerGroup = 2;
//        /** 是否需要计算总的分组数量 */
//        boolean requiredTotalGroupCount = true;
//        /** 是否需要缓存评分 */
//        boolean cacheScores = true;
//
//        TermFirstPassGroupingCollector c1 = new TermFirstPassGroupingCollector(
//                "author", groupSort, groupOffset + topNGroups);
//        //第一次查询缓存容量的大小：设置为16M
//        double maxCacheRAMMB = 16.0;
//        /** 将TermFirstPassGroupingCollector包装成CachingCollector，为第一次查询加缓存，避免重复评分
//         *  CachingCollector就是用来为结果收集器添加缓存功能的
//         */
//        CachingCollector cachedCollector = CachingCollector.create(c1,
//                cacheScores, maxCacheRAMMB);
//        // 开始第一次分组统计
//        searcher.search(query, cachedCollector);
//
//        /**第一次查询返回的结果集TopGroups中只有分组域值以及每组总的评分，至于每个分组里有几条，分别哪些索引文档，则需要进行第二次查询获取*/
//        Collection<SearchGroup<BytesRef>> topGroups = c1.getTopGroups(
//                groupOffset, fillFields);
//
//        if (topGroups == null) {
//            System.out.println("No groups matched ");
//            return;
//        }
//
//        Collector secondPassCollector = null;
//
//        // 是否获取每个分组内部每个索引的评分
//        boolean getScores = true;
//        // 是否计算最大评分
//        boolean getMaxScores = true;
//        // 如果需要对Lucene的score进行修正，则需要重载TermSecondPassGroupingCollector
//        TermSecondPassGroupingCollector c2 = new TermSecondPassGroupingCollector(
//                "author", topGroups, groupSort, docSort, docOffset
//                + docsPerGroup, getScores, getMaxScores, fillFields);
//
//        // 如果需要计算总的分组数量，则需要把TermSecondPassGroupingCollector包装成TermAllGroupsCollector
//        // TermAllGroupsCollector就是用来收集总分组数量的
//        TermAllGroupsCollector allGroupsCollector = null;
//        //若需要统计总的分组数量
//        if (requiredTotalGroupCount) {
//            allGroupsCollector = new TermAllGroupsCollector("author");
//            secondPassCollector = MultiCollector.wrap(c2, allGroupsCollector);
//        } else {
//            secondPassCollector = c2;
//        }
//
//        /**如果第一次查询已经加了缓存，则直接从缓存中取*/
//        if (cachedCollector.isCached()) {
//            // 第二次查询直接从缓存中取
//            cachedCollector.replay(secondPassCollector);
//        } else {
//            // 开始第二次分组查询
//            searcher.search(query, secondPassCollector);
//        }
//
//        /** 所有组的数量 */
//        int totalGroupCount = 0;
//        /** 所有满足条件的记录数 */
//        int totalHitCount = 0;
//        /** 所有组内的满足条件的记录数(通常该值与totalHitCount是一致的) */
//        int totalGroupedHitCount = -1;
//        if (requiredTotalGroupCount) {
//            totalGroupCount = allGroupsCollector.getGroupCount();
//        }
//        //打印总的分组数量
//        System.out.println("groupCount: " + totalGroupCount);
//
//        TopGroups<BytesRef> groupsResult = c2.getTopGroups(docOffset);
//        //这里打印的3项信息就是第一次查询的统计结果
//        totalHitCount = groupsResult.totalHitCount;
//        totalGroupedHitCount = groupsResult.totalGroupedHitCount;
//        System.out.println("groupsResult.totalHitCount:" + totalHitCount);
//        System.out.println("groupsResult.totalGroupedHitCount:"
//                + totalGroupedHitCount);
//        System.out.println("///////////////////////////////////////////////");
//        int groupIdx = 0;
//
//        //下面打印的是第二次查询的统计结果，如果你仅仅值需要第一次查询的统计结果信息，不需要每个分组内部的详细信息，则不需要进行第二次查询，请知晓
//        //迭代组
//        for (GroupDocs<BytesRef> groupDocs : groupsResult.groups) {
//            groupIdx++;
//            String groupVL = groupDocs.groupValue == null ? "分组域的域值为空" : new String(groupDocs.groupValue.bytes);
//            // 分组域的域值，groupIdx表示组的索引即第几组
//            System.out.println("group[" + groupIdx + "].groupFieldValue:" + groupVL);
//            // 当前分组内命中的总记录数
//            System.out
//                    .println("group[" + groupIdx + "].totalHits:" + groupDocs.totalHits);
//            int docIdx = 0;
//            // 迭代组内的记录
//            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
//                docIdx++;
//                // 打印分组内部每条记录的索引文档ID及其评分
//                System.out.println("group[" + groupIdx + "][" + docIdx + "]{docID:Score}:"
//                        + scoreDoc.doc + "/" + scoreDoc.score);
//                //根据docID可以获取到整个Document对象，通过doc.get(fieldName)可以获取某个存储域的域值
//                //注意searcher.doc根据docID返回的document对象中不包含docValuesField域的域值，只包含非docValuesField域的域值，请知晓
//                Document doc = searcher.doc(scoreDoc.doc);
//                System.out.println("group[" + groupIdx + "][" + docIdx + "]{docID:author}:"
//                        + doc.get("id") + ":" + doc.get("content"));
//            }
//            System.out.println("******************华丽且拉轰的分割线***********************");
//        }
//    }
}