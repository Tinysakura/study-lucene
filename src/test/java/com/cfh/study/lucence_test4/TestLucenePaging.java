package com.cfh.study.lucence_test4;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/16 13:53
 * @Description： 测试lucene分页
 */
public class TestLucenePaging {
    String indexDir = "/Users/chenfeihao/Desktop/lucene/index3";
    Directory directory;
    IndexReader reader;

    IndexSearcher searcher;
    Analyzer analyzer;
    QueryParser queryParser;

    @Before
    public void init() throws Exception{
        directory = FSDirectory.open(Paths.get(indexDir));
        reader = DirectoryReader.open(directory);

        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer();
        queryParser = new QueryParser("desc", analyzer);
    }

    /**
     * 一次性查询所有结果，将结果缓存在内存中，根据分页信息从内存中取对应页的document
     */
    @Test
    public void testPaginInMemoringCache(){

    }

    /**
     * 获取上一页最后一个文档
     * @param pageNum
     * @param pageSize
     * @param query
     * @return
     * @throws Exception
     */
    public ScoreDoc getLastDoc(int pageNum, int pageSize, Query query) throws Exception{
        if(pageNum == 1){
            return null;
        }

        int num = pageSize * (pageNum - 1);//计算上一页文档的数量
        TopDocs tds = searcher.search(query, num);

        return tds.scoreDocs[num-1];
    }

    /**
     * 使用IndexSearcher的searchAfter方法进行分页查询
     * @param pageNum
     * @param pageSize
     * @param query
     * @throws Exception
     */
    public void testAfterSearcher(int pageNum, int pageSize, Query query) throws Exception{
        System.out.println("testAfterSearcher");

        ScoreDoc lastDoc = getLastDoc(pageNum, pageSize, query);
        TopDocs tds = searcher.searchAfter(lastDoc, query, pageSize);//查询lastDoc后的pageSize条数据

        for (ScoreDoc scoreDoc : tds.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc.get("id"));
        }
    }

    @Test
    public void mainTest() throws Exception{
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("content", analyzer);

        String queryExpression = "use when Different Java";

        Query query = queryParser.parse(queryExpression);

        testAfterSearcher(2, 2, query);
    }

}
