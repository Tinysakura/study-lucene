package com.cfh.study.lucene_test9;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

/**
 * @Author: cfh
 * @Date: 2018/9/17 19:07
 * @Description: 测试Lucene的facet（方面查询）
 */
public class TestLuceneFacetSearch{
    final String indexDir = "/Users/chenfeihao/Desktop/lucene/index6";
    final String taxoDir = "/Users/chenfeihao/Desktop/lucene/taxo/taxo1";

    @Test
    public void buildIndex() throws Exception{
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
        //使用DirectoryTaxonomyWriter写入进行切面查询所需要的Taxonomy索引
        Directory taxioDirectory = FSDirectory.open(Paths.get(taxoDir));
        DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxioDirectory);

        FacetsConfig config = new FacetsConfig();

        Document doc = new Document();
        doc.add(new TextField("device", "手机", Field.Store.YES));
        doc.add(new TextField("name", "米1", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        doc.add(new FacetField("network", "移动4G"));
        //写入索引的同时写入taxo索引
        writer.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("device", "手机", Field.Store.YES));
        doc.add(new TextField("name", "米4", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        doc.add(new FacetField("network", "联通4G"));
        writer.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("device", "手机", Field.Store.YES));
        doc.add(new TextField("name", "荣耀6", Field.Store.YES));
        doc.add(new FacetField("brand", "华为"));
        doc.add(new FacetField("network", "移动4G"));
        writer.addDocument(config.build(taxoWriter, doc));

        doc = new Document();
        doc.add(new TextField("device", "电视", Field.Store.YES));
        doc.add(new TextField("name", "小米电视2", Field.Store.YES));
        doc.add(new FacetField("brand", "小米"));
        writer.addDocument(config.build(taxoWriter, doc));

        writer.close();
        taxoWriter.close();
    }

    /**
     * 对facet查询进行测试
     * @throws Exception
     */
    @Test
    public void testFacetSearch() throws Exception{
        Directory directory = FSDirectory
                .open(Paths.get(indexDir));
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        //同时还需要taxonomy reader
        Directory taxoDirectory = FSDirectory
                .open(Paths.get(taxoDir));
        TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDirectory);
        FacetsConfig config = new FacetsConfig();
        //相应的Collector是必不可少的
        FacetsCollector facetsCollector = new FacetsCollector();

        //按照手机这个维度查询
        System.out.println("---------手机----------");
        TermQuery query = new TermQuery(new Term("device", "手机"));
        TopDocs docs = FacetsCollector.search(searcher, query, 10, facetsCollector);
        printDocs(docs, searcher);
        System.out.println("----------facet-----------");
        Facets facets = new FastTaxonomyFacetCounts(taxoReader, config, facetsCollector);
        List<FacetResult> results = facets.getAllDims(10);
        //打印其他维度信息
        for (FacetResult tmp : results){
            System.out.println(tmp);
        }

        System.out.println("=======================");

        //2.drill down，品牌选小米
        System.out.println("-----小米手机-----");
        DrillDownQuery drillDownQuery = new DrillDownQuery(config, query);
        drillDownQuery.add("brand", "小米");
        FacetsCollector fc1 = new FacetsCollector();//要new新collector，否则会累加
        docs = FacetsCollector.search(searcher, drillDownQuery, 10, fc1);
        printDocs(docs, searcher);
        System.out.println("----------facet-----------");
        facets = new FastTaxonomyFacetCounts(taxoReader, config, fc1);
        results = facets.getAllDims(10);
        //获得小米手机的分布，总数2个，网络：移动4G 1个，联通4G 1个
        for (FacetResult tmp : results) {
            System.out.println(tmp);
        }
        System.out.println("=======================");

        //3.drill down，在brand这个facet选择了小米之后继续选择另一个方面network为移动4G
        System.out.println("-----移动4G小米手机-----");
        //可以看到使用的是同一个DrillDownQuery
        drillDownQuery.add("network", "移动4G");
        FacetsCollector fc2 = new FacetsCollector();
        docs = FacetsCollector.search(searcher, drillDownQuery, 10, fc2);
        printDocs(docs, searcher);
        System.out.println("----------facet-----------");
        facets = new FastTaxonomyFacetCounts(taxoReader, config, fc2);
        results = facets.getAllDims(10);
        for (FacetResult tmp : results) {
            System.out.println(tmp);
        }
        System.out.println("=======================");

        //使用sideWay查看其它平行维度的信息
        System.out.println("-----小米手机drill sideways-----");
        DrillSideways ds = new DrillSideways(searcher, config, taxoReader);
        DrillDownQuery drillDownQuery1 = new DrillDownQuery(config, query);
        drillDownQuery1.add("brand", "小米");
        DrillSideways.DrillSidewaysResult result = ds.search(drillDownQuery1, 10);
        docs = result.hits;
        printDocs(docs, searcher);
        System.out.println("----------facet-----------");
        results = result.facets.getAllDims(10);
        for (FacetResult tmp : results) {
            System.out.println(tmp);
        }
        System.out.println("=======================");

        indexReader.close();
        taxoReader.close();

    }

    public void printDocs(TopDocs docs, IndexSearcher searcher) throws Exception{
        for(ScoreDoc doc : docs.scoreDocs){
            Document document = searcher.doc(doc.doc);
            System.out.println("device:"+document.get("device"));
            System.out.println("name:"+document.get("name"));
            System.out.println();
        }
    }

}
