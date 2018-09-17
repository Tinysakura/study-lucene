package com.cfh.study.lucene_test1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/15 18:32
 * @Description: 测试lucene的crud
 */
public class TestLuceneCURD {
    String indexDir = "/Users/chenfeihao/Desktop/lucence/index2";

    // 下面是测试用到的数据
    private String ids[] = { "1", "2", "3" };
    private String citys[] = { "qingdao", "nanjing", "shanghai" };
    private String descs[] = { "Qingdao is a beautiful city.", "Nanjing is a city of culture.",
            "Shanghai is a bustling city." };

    IndexWriter getWriter(){
        try {
            //得到索引所在的目录
            Directory directory = FSDirectory.open(Paths.get(indexDir));
            //创建标准分词器
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
            return new IndexWriter(directory,iwConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 测试索引的写入
     */
    //@Test
    public void TestWriteIndex() throws Exception{
        IndexWriter writer = getWriter();
        for (int i = 0; i < ids.length; i++) {
            //创建文档对象，文档是索引和搜索的单位。
            Document doc = new Document();
            doc.add(new StringField("id", ids[i], Field.Store.YES));
            doc.add(new StringField("city", citys[i], Field.Store.YES));
            doc.add(new TextField("desc", descs[i], Field.Store.NO));
            // 添加文档
            writer.addDocument(doc);
        }
        writer.close();
    }

    /**
     * 测试写了几个文档
     *
     * @throws Exception
     */
    @Test
    public void testIndexWriter() throws Exception {
        IndexWriter writer = getWriter();
        System.out.println("写入了" + writer.numDocs() + "个文档");
        writer.close();
    }

    /**
     * 测试读取了几个文档
     *
     * @throws Exception
     */
    @Test
    public void testIndexReader() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(directory);
        System.out.println("最大文档数：" + reader.maxDoc());
        System.out.println("实际文档数：" + reader.numDocs());
        reader.close();
    }

    /**
     * 测试删除 在合并前
     *
     * @throws Exception
     */
    @Test
    public void testDeleteBeforeMerge() throws Exception {
        IndexWriter writer = getWriter();
        System.out.println("删除前：" + writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.commit();
        System.out.println("writer.maxDoc()：" + writer.maxDoc());
        System.out.println("writer.numDocs()：" + writer.numDocs());
        writer.close();
    }

    /**
     * 测试删除 在合并后
     *
     * @throws Exception
     */
    @Test
    public void testDeleteAfterMerge() throws Exception {
        IndexWriter writer = getWriter();
        System.out.println("删除前：" + writer.numDocs());
        writer.deleteDocuments(new Term("id", "1"));
        writer.forceMergeDeletes(); // 强制删除
        writer.commit();
        System.out.println("writer.maxDoc()：" + writer.maxDoc());
        System.out.println("writer.numDocs()：" + writer.numDocs());
        writer.close();
    }

    /**
     * 测试更新
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new StringField("id", "1", Field.Store.YES));
        doc.add(new StringField("city", "beijing", Field.Store.YES));
        doc.add(new TextField("desc", "beijing is a city.", Field.Store.NO));
        writer.updateDocument(new Term("id", "1"), doc);
        writer.close();
    }
}
