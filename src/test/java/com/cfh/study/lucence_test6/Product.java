package com.cfh.study.lucence_test6;

import java.io.Serializable;

/**
 * @Author: cfh
 * @Date: 2018/9/17 10:18
 * @Description: 用来测试suggest功能的pojo类
 */
public class Product implements Serializable {
    /** 产品名称 */
    private String name;
    /** 产品图片 */
    private String image;
    /** 产品销售地区 */
    private String[] regions;
    /** 产品销售量 */
    private int numberSold;

    public Product() {
    }

    public Product(String name, String image, String[] regions, int numberSold) {
        this.name = name;
        this.image = image;
        this.regions = regions;
        this.numberSold = numberSold;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String[] getRegions() {
        return regions;
    }

    public void setRegions(String[] regions) {
        this.regions = regions;
    }

    public int getNumberSold() {
        return numberSold;
    }

    public void setNumberSold(int numberSold) {
        this.numberSold = numberSold;
    }
}
