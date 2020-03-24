package com.notejava.demo;

import lombok.Data;

@Data
public class Components {
    private String material;
    private String name;
    private String retailPrice;
    private String stock;
    private String beijingPrice;
    private String beijingInventory;
    private String shanghaiPrice;
    private String shanghaiInventory;
    private String foshanPrice;
    private String foshanInventory;
    private String chengduPrice;
    private String chengduInventory;
    private String shengyangPrice;
    private String shengyangInventory;

    @Override
    public String toString() {
        return "" +
                "编号='" + material + '\'' +
                "名称='" + name + '\'' +
                ", 零售价='" + retailPrice + '\'' +
                ", 经销商净值='" + stock + '\'' +
                ", 北京库存='" + beijingPrice + '\'' +
                ", 北京可用库存='" + beijingInventory + '\'' +
                ", 上海可用='" + shanghaiPrice + '\'' +
                ", 上海可用库存='" + shanghaiInventory + '\'' +
                ", 佛山可用='" + foshanPrice + '\'' +
                ", 佛山可用库存='" + foshanInventory + '\'' +
                ", 成都可用='" + chengduPrice + '\'' +
                ", 成都可用库存='" + chengduInventory + '\'' +
                ", 沈阳可用='" + shengyangPrice + '\'' +
                ", 沈阳可用库存='" + shengyangInventory + '\'';
    }
}
