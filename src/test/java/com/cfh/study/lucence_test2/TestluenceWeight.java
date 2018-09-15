package com.cfh.study.lucence_test2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/15 19:24
 * @Description: 测试lucene的文档域权重的设置
 */
public class TestluenceWeight {
    String dirPath = "/Users/chenfeihao/Desktop/lucene/index3";
    private String ids[]={"1","2","3","4"};
    private String authors[]={"Jack","Marry","John","Json"};
    private String positions[]={"accounting","technician","salesperson","boss"};
    private String titles[]={"Java is a good language.","Java is a cross platform language","Java powerful","You should learn java"};
    private String contents[]={
            "If possible, use the same JRE major version at both index and search time.",
            "When upgrading to a different JRE major version, consider re-indexing. ",
            "Different JRE major versions may implement different versions of Unicode,",
            "For example: with Java 1.4, `LetterTokenizer` will split around the character U+02C6,"
    };

    //@Test
    public void createIndex() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(dirPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);

        IndexWriter writer = new IndexWriter(directory, iwConfig);

        for (int i = 0; i < ids.length; i++){
            Document doc = new Document();

            doc.add(new StringField("id",ids[i],Field.Store.YES));
            doc.add(new StringField("author",authors[i],Field.Store.YES));
            doc.add(new StringField("position",positions[i],Field.Store.YES));
            //在有间隔的字段上建立索引的话需要使用TextField而不是StringField，使用StringField不会进行分词
            //根据position对title索引加权
            TextField field = new TextField("title",titles[i],Field.Store.YES);
            if ("boss".equals(positions[i])){
                //setBoosts方法在6.6版本后已经被废除
                //field.setBoost(2);
            }
            doc.add(new TextField("title",titles[i],Field.Store.YES));
            doc.add(new TextField("content",contents[i],Field.Store.NO));

            writer.addDocument(doc);
        }

        writer.close();
    }

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
}
