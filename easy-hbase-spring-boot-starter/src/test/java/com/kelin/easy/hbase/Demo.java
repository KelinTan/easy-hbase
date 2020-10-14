// Copyright 2020 Kelin Inc. All rights reserved.

package com.kelin.easy.hbase;

/**
 * @author Kelin Tan
 */
public class Demo {
    public Demo() {
    }

    public Demo(String rowkey, Integer id, String name) {
        this.rowkey = rowkey;
        this.id = id;
        this.name = name;
    }

    private String rowkey;
    private Integer id;
    private String name;

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
