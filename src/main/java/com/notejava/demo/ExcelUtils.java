package com.notejava.demo;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lyle 2019/5/13 14:14.
 */
public class ExcelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    private ExcelUtils() {
    }

    /**
     * @param header   excel 的第一行
     * @param dataList 数据集
     * @param fileName 文件名
     * @description 导出 excel，默认只有一个 sheet
     **/
    public static <T> void export(Map<String, String> header, List<T> dataList, String fileName) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("sheet1");
        Row headerRow = sheet.createRow(0);
        List<String> values = new ArrayList<>(header.values());
        for (int i = 0; i < values.size(); i++) {
            headerRow.createCell(i).setCellValue(values.get(i));
        }

        List<String> keys = new ArrayList<>(header.keySet());
        int dataStart = 1;
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(dataStart++);
            T t = dataList.get(i);
            BeanWrapperImpl beanWrapper = new BeanWrapperImpl(t);
            for (int k = 0; k < keys.size(); k++) {
                Object propertyValue = beanWrapper.getPropertyValue(keys.get(k));
                row.createCell(k).setCellValue(propertyValue == null ? "" : propertyValue.toString());
            }
        }
        workbook.write(new FileOutputStream(new File(fileName)));
        workbook.close();
    }
}
