package com.cfh.study.lucence_demo1;

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
import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/15 15:57
 * @Description: 测试索引的搜索功能
 */
public class Searcher {

    public static void search(String indexDir,String query) throws Exception{

        //获取索引文件的路径
        Directory dir = FSDirectory.open(Paths.get(indexDir));

        //通过dir获取路径下的所有文件
        IndexReader reader = DirectoryReader.open(dir);

        //建立索引查询器
        IndexSearcher searcher = new IndexSearcher(reader);

        //实例化分析器
        Analyzer analyzer = new StandardAnalyzer();

        /*
          建立查询解析器
          第一个参数是要查询的字段，第二个参数是分析器Analyzer
         */
        QueryParser parser = new QueryParser("contents", analyzer);

        //根据传入的查询语句查找
        Query query1 = parser.parse(query);
        //第一个参数是Query对象，第二个参数为查询的行数
        TopDocs hits = searcher.search(query1, 10);

        //对得到的结果进行遍历
        for (ScoreDoc scoreDoc : hits.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);
            //打印搜索到相关结果的文件的fullPath
            System.out.println((doc).get("fullPath"));
        }

        reader.close();
    }

    public static void main(String[] args) {
        String indexDir = "/Users/chenfeihao/Desktop/lucence/index";

        String q = "Jean-Philippe Barrette-LaPierre";

        try {
            search(indexDir,q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
