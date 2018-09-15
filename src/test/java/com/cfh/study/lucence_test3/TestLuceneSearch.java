package com.cfh.study.lucence_test3;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/15 21:59
 * @Description: 测试Lucene的模糊查询
 */
public class TestLuceneSearch {
    String dirPath = "/Users/chenfeihao/Desktop/lucene/index3";

    @Test
    public void fuzzySearch() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(dirPath));

        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        String queryField = "title";//查询的索引

        String queryContent = "java";//查询的内容


        Term term = new Term(queryField,queryContent);//指定在哪个索引上查询哪些内容

        /*
          FuzzyQuery支持模糊查询，第三个参数为允许的错词(错词包括漏词和错词)
         */
        Query query = new FuzzyQuery(term, 1);//使用TermQuery对索引中包含的内容进行查询

        TopDocs docs = searcher.search(query, 10);//使用query对象查询前10条数据

        System.out.println("查询到了"+docs.totalHits+"条数据");

        for(ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);//根据docId获取Document
            System.out.println(doc.get("title"));//使用document的get方法获取建立的索引中的内容
        }
    }

    /**
     * 测试解析表达式的使用
     */
    @Test
    public void testQueryParser() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(dirPath));

        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        String queryField = "title";//查询的索引
        String queryContent = "good cross";

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser(queryField, analyzer);
        //使用QueryParser进行Query的构建默认会对查询内容进行分词
        Query query = queryParser.parse(queryContent);

        TopDocs docs = searcher.search(query, 10);//使用query对象查询前10条数据

        System.out.println("查询到了"+docs.totalHits+"条数据");

        for(ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);//根据docId获取Document
            System.out.println(doc.get("title"));//使用document的get方法获取建立的索引中的内容
        }
    }
}
