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
    String dirpath2 = "/Users/chenfeihao/Desktop/lucene/index";

    /**
     * 测试模糊查询（FuzzySearch）
     * @throws Exception
     */
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
     * 使用TermQuery对指定项进行搜索
     * @throws Exception
     */
    @Test
    public void readIndex() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(dirPath));

        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        String queryField = "title";//查询的索引

        String queryContent = "java";//查询的内容

        Term term = new Term(queryField,queryContent);//指定在哪个索引上查询哪些内容

        Query query = new TermQuery(term);//使用TermQuery对索引中包含的内容进行查询

        TopDocs docs = searcher.search(query, 10);//使用query对象查询前10条数据

        System.out.println("查询到了"+docs.totalHits+"条数据");

        for(ScoreDoc scoreDoc : docs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);//根据docId获取Document
            System.out.println(doc.get("title"));//使用document的get方法获取建立的索引中的内容
        }
    }

    /**
     * 测试查询表达式的使用
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

    /**
     * 解析表达式（QueryParser）的高级使用
     */
    @Test
    public void testAdvancedQueryParser() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(dirPath));

        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        String queryField = "content";//对content索引进行查询

        Analyzer analyzer = new StandardAnalyzer();

        QueryParser queryParser = new QueryParser(queryField,analyzer);//使用指定的分词器构建QueryParser

        /*
          测试不同的单条件queryExpression的查询效果
         */
        //String queryExpression = "if AND when";//AND表示查询的索引中必须同时包括AND前后连接的内容
        //String queryExpression = "when OR possible";//OR表示查询的索引中可以包含任意被OR连接的内容，OR默认可以省略
        //注意所有模糊查询都可以放在表达式开头否则将失去效果
        //String queryExpression = "upgrading?";//?表示模糊查询，？只能指代分词中的一个字符
        //String queryExpression = "upgrad*";//*表示模糊查询，*可以指代分词中的多个字符
        //String queryExpression = "ugrding~";//~表示相似度查询，会查询与指代的分词相似的内容（是否相似由算法决定）
        //String queryExpression = "\"upgrading different\"~10";//距离查询，~10表示在查询的索引中upgrading与different之间最多可以有10个分词
        //String queryExpression = "id:[1 TO 3]";//闭区间的范围查询（id:指定了查询的索引为id）
        //String queryExpression = "id:{1 TO 4}";//开区间的范围查询
        //String queryExpression = "possible^2 when";//^给查询词设置了不同的权重，权重较大的查询词查询返回的结果排序更加靠前（默认都为1）

        /*
          下面测试多条件的queryExpression(AND|OR|NOT|+|-)
         */
        //String queryExpression = "\"upgrading?\" AND \"Different\"";//AND连接的多条件，索引必须全部满足
        //String queryExpression = "\"upgrading?\" OR \"Different\"";//OR连接的多条件，索引满足其中之一即可
        //String queryExpression = "\"different\" NOT \"upgrading~\"";//NOT连接的多条件，NOT后的条件一定不能被满足
        //String queryExpression = "+\"upgrading\" OR \"different\"";//+后的条件索引必须强制匹配
        //String queryExpression = "-\"upgrading\" AND \"different\"";//-后的条件索引必须强制不匹配

        /*
          下面测试queryExpression中的分组
         */
        //String queryExpression = "(\"upgrading?\" OR \"Different\") AND -\"versions\"";//()可以对多条件查询情况下的表达式进行分组以消除歧义
        //String queryExpression = "title:(+java -good) AND content:(upgrading)";//()可以在多字段查询时对字段对应的搜索条件进行分组

        /*
          下面是lucene中需要进行转义的一些特殊字符
          Escaping Special Characters
          Lucene支持转义查询中的特殊字符，以下是Lucene的特殊字符清单：
          + - && || ! ( ) { } [ ] ^ " ~ * ? : \
          转义特殊字符我们可以使用符号“\”放于字符之前。比如我们要搜索(1+1):2，我们可以使用如下语法：\(1\+1\)\:2
         */

        String queryExpression = null;

        Query query = queryParser.parse(queryExpression);

        TopDocs docs = searcher.search(query, 10);

        //打印在哪一篇文章中查询到的结果
        System.out.println("共查询到了"+docs.totalHits+"篇文章");
        for (ScoreDoc scoreDoc : docs.scoreDocs){
            System.out.println("id:"+searcher.doc(scoreDoc.doc).get("id"));
            System.out.println("title:"+searcher.doc(scoreDoc.doc).get("title"));
            System.out.println("========================================");
        }
    }
}
