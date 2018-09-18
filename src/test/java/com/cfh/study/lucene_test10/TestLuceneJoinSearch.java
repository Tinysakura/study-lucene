package com.cfh.study.lucene_test10;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/18 09:14
 * @Description: 测试Lucene的join查询
 */
public class TestLuceneJoinSearch {
    final String indexDir = "/Users/chenfeihao/Desktop/lucene/index7";
    final String idField = "id";
    final String toField = "productId";

    //@Test
    public void createIndex() throws Exception{
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter w = new IndexWriter(dir, config);

        // 0
        Document doc = new Document();
        doc.add(new TextField("description", "random text", Field.Store.YES));
        doc.add(new TextField("name", "name1", Field.Store.YES));
        doc.add(new TextField(idField, "1", Field.Store.YES));
        doc.add(new SortedDocValuesField(idField, new BytesRef("1")));

        w.addDocument(doc);

        // 1
        Document doc1 = new Document();
        doc1.add(new TextField("price", "10.0", Field.Store.YES));
        doc1.add(new TextField(idField, "2", Field.Store.YES));
        doc1.add(new SortedDocValuesField(idField, new BytesRef("2")));
        doc1.add(new TextField(toField, "1", Field.Store.YES));
        doc1.add(new SortedDocValuesField(toField, new BytesRef("1")));

        w.addDocument(doc1);

        // 2
        Document doc2 = new Document();
        doc2.add(new TextField("price", "20.0", Field.Store.YES));
        doc2.add(new TextField(idField, "3", Field.Store.YES));
        doc2.add(new SortedDocValuesField(idField, new BytesRef("3")));
        doc2.add(new TextField(toField, "1", Field.Store.YES));
        doc2.add(new SortedDocValuesField(toField, new BytesRef("1")));

        w.addDocument(doc2);

        // 3
        Document doc3 = new Document();
        doc3.add(new TextField("description", "more random text", Field.Store.YES));
        doc3.add(new TextField("name", "name2", Field.Store.YES));
        doc3.add(new TextField(idField, "4", Field.Store.YES));
        doc3.add(new SortedDocValuesField(idField, new BytesRef("4")));

        w.addDocument(doc3);


        // 4
        Document doc4 = new Document();
        doc4.add(new TextField("price", "10.0", Field.Store.YES));
        doc4.add(new TextField(idField, "5", Field.Store.YES));
        doc4.add(new SortedDocValuesField(idField, new BytesRef("5")));
        doc4.add(new TextField(toField, "4", Field.Store.YES));
        doc4.add(new SortedDocValuesField(toField, new BytesRef("4")));
        w.addDocument(doc4);

        // 5
        Document doc5 = new Document();
        doc5.add(new TextField("price", "20.0", Field.Store.YES));
        doc5.add(new TextField(idField, "6", Field.Store.YES));
        doc5.add(new SortedDocValuesField(idField, new BytesRef("6")));
        doc5.add(new TextField(toField, "4", Field.Store.YES));
        doc5.add(new SortedDocValuesField(toField, new BytesRef("4")));
        w.addDocument(doc5);

        //6
        Document doc6 = new Document();
        doc6.add(new TextField(toField, "4", Field.Store.YES));
        doc6.add(new SortedDocValuesField(toField, new BytesRef("4")));
        w.addDocument(doc6);
        w.commit();
        w.close();
        dir.close();
    }

    /**
     * 测试join查询
     * @throws Exception
     */
    @Test
    public void testJoinSearch() throws Exception{
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        //使用JoinQuery进行连接查询
        //根据打印结果可以看出连接查询先根据Term的条件查询到所有符合的文档，然后提取文档中我们指定的fromField(这里是idField),
        //根据fromField的值寻找匹配的toField的值，根据toField的值返回匹配的文档
        //以第一个查询为例，根据Term我们去寻找name索引值为name2的文档，找到的文档的fromField值为4，则根据连接条件
        //toField值也为4,于是查询转为查询所有toField索引（这里指定为了productId）值为4的文档
        Query joinQuery = JoinUtil.createJoinQuery(idField, false, toField, new TermQuery(new Term("name", "name2")), searcher, ScoreMode.None);
        TopDocs docs = searcher.search(joinQuery, 10);
        System.out.println("查询到文档数:"+docs.totalHits);
        for (ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(idField+":"+doc.get(idField));
            System.out.println(toField+":"+doc.get(toField));
        }

        joinQuery = JoinUtil.createJoinQuery(idField, false, toField, new TermQuery(new Term("name", "name1")), searcher, ScoreMode.None);
        docs = searcher.search(joinQuery, 10);
        System.out.println("查询到的文档数："+docs.totalHits);
        for (ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(idField+":"+doc.get(idField));
            System.out.println(toField+":"+doc.get(toField));
        }

        // 根据商品连接查询offer,查询出的结果为所有id为4的offer
        joinQuery = JoinUtil.createJoinQuery(toField, false, idField, new TermQuery(new Term("id", "5")), searcher, ScoreMode.None);
        docs = searcher.search(joinQuery, 10);
        System.out.println("查询到的匹配数据："+docs.totalHits);
        for (ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(idField+":"+doc.get(idField));
            System.out.println(toField+":"+doc.get(toField));
        }

        reader.close();
        dir.close();
    }
}
