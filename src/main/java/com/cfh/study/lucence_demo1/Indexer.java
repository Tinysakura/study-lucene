package com.cfh.study.lucence_demo1;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/15 15:31
 * @Description: 创建索引文件
 */
public class Indexer {
    //写索引实例
    private IndexWriter writer;

    /**
     * 在配置文件中创建IndexWriter实例
     *
     * @param indexDir
     */
    public Indexer(String indexDir) throws Exception {
        //得到索引所在的目录
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        //创建标准分词器
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);

        //实例化IndexWriter
        writer = new IndexWriter(directory, iwConfig);
    }

    //关闭写索引
    public void close() throws Exception {
        writer.close();
    }

    /**
     * 获取文档
     *
     * @param f
     * @return
     * @throws Exception
     */
    Document getDocument(File f) throws Exception {
        Document doc = new Document();
        //设置内容索引
        doc.add(new TextField("contents", new FileReader(f)));
        //Field.Store.YES:将文件名加到索引文件中，为no说明不需要加入
        doc.add(new TextField("fileName", f.getName(), Field.Store.YES));
        //将文件的完整路径加到索引中
        doc.add(new TextField("fullPath", f.getCanonicalPath(), Field.Store.YES));

        return doc;
    }

    /**
     * 为文件写索引
     *
     * @param file
     * @throws Exception
     */
    void indexFile(File file) throws Exception {
        //获取文档
        Document document = getDocument(file);

        //开始将文档写入索引文件
        writer.addDocument(document);
    }

    //为一个目录下的所有文件写索引
    public int index(String dataDir) throws Exception {
        File[] files = new File(dataDir).listFiles();

        for (File f : files) {
            indexFile(f);
        }

        //返回写入索引的文件数
        return writer.numDocs();
    }

    //在主方法中测试一下
    public static void main(String[] args) {
        //指定索引文件的路径
        String indexDir = "/Users/chenfeihao/Desktop/lucence/index";
        //指定被索引文件的路径
        String dataDir = "/Users/chenfeihao/Desktop/lucence/data";

        long startTime = System.currentTimeMillis();
        int indexNum;
        Indexer indexer = null;

        try {
            indexer = new Indexer(indexDir);
            indexNum = indexer.index(dataDir);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                indexer.close();//关闭资源
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("建立索引花费了" + (endTime - startTime) + "ms");
    }
}
