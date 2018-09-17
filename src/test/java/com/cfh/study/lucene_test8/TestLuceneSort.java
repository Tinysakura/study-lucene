package com.cfh.study.lucene_test8;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/17 15:45
 * @Description: 测试Lucene自定义排序功能
 */
public class TestLuceneSort {
    String indexDir = "/Users/chenfeihao/Desktop/lucene/index3";

    @Test
    public void testCustomSort() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        String queryField = "id";//对content索引进行查询

        Analyzer analyzer = new StandardAnalyzer();

        QueryParser queryParser = new QueryParser(queryField,analyzer);

        String queryExpression = "[1 TO 4]";

        Query query = queryParser.parse(queryExpression);

        //自定义排序规则
        //注意用来排序的索引必须是SortedDocValuesField类型或者是NumericDocValuesField类型，否则会抛异常
        Sort sort = new Sort(new SortField("id",SortField.Type.STRING,false));

        //设置doDocScores为true而doMaxScore为false说明当多个查询条件都符合时取多条件中score最大的而不是score之和
        TopDocs docs = searcher.search(query, 10, sort, true, false);
        System.out.println("共查询到了"+docs.totalHits+"篇文章");
        for (ScoreDoc scoreDoc : docs.scoreDocs){
            System.out.println("id:"+searcher.doc(scoreDoc.doc).get("id"));
            System.out.println("title:"+searcher.doc(scoreDoc.doc).get("title"));
            System.out.println("========================================");
        }
    }
}
