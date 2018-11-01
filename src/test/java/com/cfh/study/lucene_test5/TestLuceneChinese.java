package com.cfh.study.lucene_test5;

import com.cfh.study.SearchUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.StringReader;
import java.nio.file.Paths;

/**
 * @Author: cfh
 * @Date: 2018/9/16 20:24
 * @Description: 测试lucene的中文分词已经查询高亮
 */
public class TestLuceneChinese {
    final String indexDir = "/Users/chenfeihao/Desktop/lucene/index4";

    //准备的中文测试数据
    final String[] ids = {"1", "2", "3"};
    final String[] names = {"非自然死亡（unnatural）", "为了N", "白夜行"};
    final String[] desc = {
            "《非自然死亡》（日语名：アンナチュラル）是日本TBS电视台播出的医学悬疑剧，由冢原亚由子、竹村谦太郎、村尾嘉昭执导，野木亚纪子编剧，石原里美主演，洼田正孝、井浦新等共演，于2018年1月12日在日本开播。 [1] \n" +
                    "该剧讲述了在“非自然死亡原因研究所”任职的法医三澄美琴与同事们一起探查非正常死亡者的真正死因，从而改变现实世界的故事 [2]  。",
            "《为了N》是改编自凑佳苗的同名小说的一部推理纯爱电视剧。由日本TBS电视台于2014年10月17日出品并首播。冢原あゆ子、山本刚义执导，奥寺佐渡子编剧，荣仓奈奈、洼田正孝、贺来贤人、小出惠介、三浦友和等主演。\n" +
                    "该剧登场人物有一个共同点，那便是他们名字的首字母都是“N”。故事刻画了这些N们如何相遇、相爱、并犯下罪行的故事 [1]  。",
            "《白夜行》是日本2006年TBS电视台播出的电视剧，根据东野圭吾同名推理小说《白夜行》改编，由森下佳子编剧，山田孝之、绫濑遥主演。 [1-2] \n" +
                    "该剧讲述了一对有着悲惨命运的少年少女，14年以来以相当残酷、孤独、单纯的灵魂相爱着却无法相守的故事。"
    };

    /**
     * 测试以追加的方式创建索引
     */
    @Test
    public void createNewIndex() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //使用中文分词器而不是英文分词器StandardsAnalyser
        SmartChineseAnalyzer chineseAnalyzer = new SmartChineseAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(chineseAnalyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(directory, config);

        for (int i = 0; i < ids.length - 1; i++) {
            Document doc = new Document();
            doc.add(new StringField("id", ids[i], Field.Store.YES));
            doc.add(new StringField("name", names[i], Field.Store.YES));
            doc.add(new TextField("desc", desc[i], Field.Store.YES));

            writer.addDocument(doc);
        }

        writer.close();
    }

    /**
     * 在指定的目录下创建索引
     */
    @Test
    public void createIndex() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));

        //使用中文分词器而不是英文分词器StandardsAnalyser
        SmartChineseAnalyzer chineseAnalyzer = new SmartChineseAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(chineseAnalyzer);

        IndexWriter writer = new IndexWriter(directory, config);

        for (int i = 0; i < ids.length; i++) {
            Document doc = new Document();
            doc.add(new StringField("id", ids[i], Field.Store.YES));
            doc.add(new StringField("name", names[i], Field.Store.YES));
            doc.add(new TextField("desc", desc[i], Field.Store.YES));

            writer.addDocument(doc);
        }

        writer.close();
    }

    /**
     * 测试MatchAllDocQuery是否对中文分词有效
     */
    @Test
    public void testMatchAllDocQuery() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new SmartChineseAnalyzer();

        Query query = new MatchAllDocsQuery();
        TopDocs docs = SearchUtils.getScoreDocsByPerPageAndSortField(searcher, query, 0, 5, null);

        System.out.println("查询到了" + docs.totalHits + "条记录");
        for (ScoreDoc doc : docs.scoreDocs) {
            System.out.println("id:" + searcher.doc(doc.doc).get("id"));
            System.out.println("name:" + searcher.doc(doc.doc).get("name"));
        }

        reader.close();
    }

    /**
     * 测试查询表达式是否对中文分词有效
     */
    @Test
    public void testChineseSearch() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new SmartChineseAnalyzer();
        QueryParser queryParser = new QueryParser("desc", analyzer);

        //Query query = new FuzzyQuery(new Term("desc", "死"));
//        PhraseQuery.Builder builder = new PhraseQuery.Builder();
//        builder.add(new Term("desc", "电视台"));
//        builder.add(new Term("desc", "医学"));
//        builder.setSlop(3);
//        Query query = builder.build();

        Query query = new PrefixQuery(new Term("desc", "悲"));
        TopDocs docs = searcher.search(query, 10);

        System.out.println("查询到了" + docs.totalHits + "条记录");
        for (ScoreDoc doc : docs.scoreDocs) {
            System.out.println("id:" + searcher.doc(doc.doc).get("id"));
            System.out.println("name:" + searcher.doc(doc.doc).get("name"));
        }

        reader.close();
    }

    /**
     * 测试高亮
     *
     * @throws Exception
     */
    @Test
    public void testHignLighter() throws Exception {
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new SmartChineseAnalyzer();
        QueryParser queryParser = new QueryParser("desc", analyzer);
        String queryExpression = "白夜";
        Query query = queryParser.parse(queryExpression);

        TopDocs docs = searcher.search(query, 10);

        QueryScorer queryScorer = new QueryScorer(query);//根据查询的匹配度返回摘要
        Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
        //指定高亮部分的显示样式
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
        Highlighter highlighter = new Highlighter(formatter, queryScorer);
        highlighter.setTextFragmenter(fragmenter);

        System.out.println("查询到了" + docs.totalHits + "条记录");
        for (ScoreDoc doc : docs.scoreDocs) {
            System.out.println("id:" + searcher.doc(doc.doc).get("id"));
            System.out.println("name:" + searcher.doc(doc.doc).get("name"));
            String desc = searcher.doc(doc.doc).get("desc");
            System.out.println("desc:" + desc);
            //显示bestMatch(摘要)
            TokenStream tokenStream = analyzer.tokenStream("desc", new StringReader(desc));
            System.out.println("best_match:" + highlighter.getBestFragment(tokenStream, desc));
        }

        reader.close();
    }
}
