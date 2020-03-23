package com.notejava.demo;

import lombok.Data;

@Data
public class Components {
    private String material;
    private String retailPrice;
    private String stock;
    //private String beijingPrice;
    private String beijingInventory;
    //private String shanghaiPrice;
    private String shanghaiInventory;
    //private String foshanPrice;
    private String foshanInventory;
    //private String chengduPrice;
    private String chengduInventory;
    //private String shengyangPrice;
    private String shengyangInventory;

    @Override
    public String toString() {
        return "零件{" +
                "编号='" + material + '\'' +
                ", 零售价='" + retailPrice + '\'' +
                ", 经销商净值='" + stock + '\'' +
                //", 北京价格='" + beijingPrice + '\'' +
                ", 北京库存='" + beijingInventory + '\'' +
                //", 上海价格='" + shanghaiPrice + '\'' +
                ", 上海库存='" + shanghaiInventory + '\'' +
                //", 佛山价格='" + foshanPrice + '\'' +
                ", 佛山库存='" + foshanInventory + '\'' +
                //", 成都价格='" + chengduPrice + '\'' +
                ", 成都库存='" + chengduInventory + '\'' +
                //", 沈阳价格='" + shengyangPrice + '\'' +
                ", 沈阳库存='" + shengyangInventory + '\'' +
                '}';
    }
}
