package com.cfh.study.lucence_test6;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author: cfh
 * @Date: 2018/9/17 10:18
 * @Description: 测试lucene的suggest功能
 */
public class TestLuceneSuggest {
    private static void lookup(AnalyzingInfixSuggester suggester, String name,
                               String region) throws IOException {
        HashSet<BytesRef> contexts = new HashSet<BytesRef>();
        //先根据region域进行suggest再根据name域进行suggest
        contexts.add(new BytesRef(region.getBytes("UTF8")));
        //num决定了返回几条数据，参数四表明是否所有TermQuery是否都需要满足，参数五表明是否需要高亮显示
        List<Lookup.LookupResult> results = suggester.lookup(name, contexts, 2, true, false);
        System.out.println("-- \"" + name + "\" (" + region + "):");
        for (Lookup.LookupResult result : results) {
            System.out.println(result.key);//result.key中存储的是根据用户输入内部算法进行匹配后返回的suggest内容
            //从载荷（payload）中反序列化出Product对象(实际生产中出于降低内存占用考虑一般不会在载荷中存储这么多内容)
            BytesRef bytesRef = result.payload;
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytesRef.bytes));
            Product product = null;
            try {
                product = (Product)is.readObject();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("product-Name:" + product.getName());
            System.out.println("product-regions:" + product.getRegions());
            System.out.println("product-image:" + product.getImage());
            System.out.println("product-numberSold:" + product.getNumberSold());
        }
        System.out.println();
    }

    @Test
    public void mainTest(){
        try {
            RAMDirectory indexDir = new RAMDirectory();
            StandardAnalyzer analyzer = new StandardAnalyzer();
            AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(indexDir, analyzer);

            //创建Product测试数据
            ArrayList<Product> products = new ArrayList<Product>();
            products.add(new Product("Electric Guitar",
                    "http://images.example/electric-guitar.jpg", new String[] {
                    "US", "CA" }, 100));
            products.add(new Product("Electric Train",
                    "http://images.example/train.jpg", new String[] { "US",
                    "CA" }, 100));
            products.add(new Product("Acoustic Guitar",
                    "http://images.example/acoustic-guitar.jpg", new String[] {
                    "US", "ZA" }, 80));
            products.add(new Product("Guarana Soda",
                    "http://images.example/soda.jpg",
                    new String[] { "ZA", "IE" }, 130));

            // 创建测试索引
            suggester.build(new ProductIterator(products.iterator()));

            // 开始搜索
            lookup(suggester, "Gu", "US");
            //lookup(suggester, "Gu", "ZA");
            //lookup(suggester, "Gui", "CA");
            //lookup(suggester, "Electric guit", "US");
        } catch (IOException e) {
            System.err.println("Error!");
        }
    }
}
