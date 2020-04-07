package com.notejava.demo;

import org.apache.commons.compress.utils.Lists;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Bmw {

    public static Map<String, String> cookiesMap = new HashMap<>();

    public static void test() throws IOException, InterruptedException {
        String userDir = System.getProperty("user.dir");
        System.out.println("工作目录:" + userDir);
        String excelPath = userDir + File.separator + "bmw.xlsx";
        System.out.println(excelPath);
        File file = new File(excelPath);
        if (!file.exists()) {
            System.out.println("Excel文件不存在");
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        Workbook workbook = null;
        try {
            System.out.println("读取文件中...");
            workbook = new XSSFWorkbook(fileInputStream);
        } catch (Exception ex) {
            workbook = new HSSFWorkbook(fileInputStream);
        }
        Sheet sheet = workbook.getSheetAt(0);
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        List<Components> componentsList = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        System.out.println("开始...");
        for (int i = 1; i < physicalNumberOfRows; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(0);
            cell.setCellType(CellType.STRING);
            String material = cell.getStringCellValue();

            if (!StringUtil.isBlank(material)) {
                executorService.submit(() -> {
                    try {
                        Components components = getComponents(material);
                        if (components != null) {
                            System.out.println(components.toString());
                            componentsList.add(components);
                        }
                    } catch (Exception e) {
                        System.out.println(material);
                        e.printStackTrace();
                    }
                });
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        Map headerMapper = new LinkedHashMap(11);
        headerMapper.put("material", "编号");
        headerMapper.put("name", "名称");
        headerMapper.put("retailPrice", "零售价");
        headerMapper.put("stock", "经销商净值");
        headerMapper.put("beijingPrice", "北京库存");
        headerMapper.put("beijingInventory", "北京可用库存");
        headerMapper.put("shanghaiPrice", "上海库存");
        headerMapper.put("shanghaiInventory", "上海可用库存");
        headerMapper.put("foshanPrice", "佛山库存");
        headerMapper.put("foshanInventory", "佛山可用库存");
        headerMapper.put("chengduPrice", "成都库存");
        headerMapper.put("chengduInventory", "成都可用库存");
        headerMapper.put("shengyangPrice", "沈阳库存");
        headerMapper.put("shengyangInventory", "沈阳可用库存");

        fileInputStream.close();

        ExcelUtils.export(headerMapper, componentsList, userDir + File.separator + "bmw_" + System.currentTimeMillis() + ".xlsx");
    }

    private static Components getComponents(String material) throws IOException {
        Connection conn1 = Jsoup.connect("https://www.dwp.bmw-brilliance.cn/sap(bD16aCZjPTEwMA==)/bc/bsp/bmw/gis_tp_par_dev/spp05_stock_check.htm");
        conn1.timeout(30000);
        conn1.data("material", material);
        conn1.data("onInputProcessing", "atp_all");
        conn1.data("req_date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        conn1.data("x", "-7");
        conn1.data("y", "5");
        conn1.cookies(getCookiesMap());
        Connection.Response response = conn1.execute();
        String body = response.body();
        //System.out.println(body);
        Document document = Jsoup.parse(body);

        Components components = new Components();
        components.setMaterial(material);

        Elements resultWhite = document.getElementsByClass("ResultWhite");
        if (CollectionUtils.isEmpty(resultWhite)) {
            System.out.println("编号" + material + "查询不到结果");
            return null;
        }

        //获取tr
        Element trElement = resultWhite.parents().first();
        //System.out.println(trElement);

        Elements children = trElement.children();

        Element nameElement = children.get(1);
        String name = getName(nameElement);
        components.setName(name);

        Element element = children.get(3);
        String retailPrice = element.html();
        components.setRetailPrice(retailPrice);

        try {
            Element beijing = children.get(7);
            components.setBeijingPrice(getPrice(beijing));
            components.setBeijingInventory(getInventory(beijing));
        } catch (Exception e) {
            //System.out.println(body);
            throw e;
        }

        Element shanghai = children.get(11);
        //.out.println(shanghai);
        components.setShanghaiPrice(getPrice(shanghai));
        components.setShanghaiInventory(getInventory(shanghai));

        Element foshan = children.get(15);
        //System.out.println(foshan);
        components.setFoshanPrice(getPrice(foshan));
        components.setFoshanInventory(getInventory(foshan));

        Element chengdu = children.get(19);
        //System.out.println(chengdu);
        components.setChengduPrice(getPrice(chengdu));
        components.setChengduInventory(getInventory(chengdu));

        Element shengyang = children.get(23);
        //System.out.println(shengyang);
        components.setShengyangPrice(getPrice(shengyang));
        components.setShengyangInventory(getInventory(shengyang));

        String stock = getStock(material);
        components.setStock(stock);

        //System.out.println(components);
        return components;
    }

    private static String getName(Element element) {
        String html = element.html();
        if (StringUtil.isBlank(html)) {
            return null;
        }
        String str = "<br>";
        int index = html.indexOf(str);
        if (index < 0) {
            return null;
        }
        String name = html.substring(index + str.length());
        return name;
    }

    private static String getPrice(Element element) {
        String price = element.getElementsByTag("i").first().html();
        //System.out.println(price);
        return price;
    }

    private static String getStock(String material) throws IOException {
        Connection conn1 = Jsoup.connect("https://www.dwp.bmw-brilliance.cn/sap(bD16aCZjPTEwMA==)/bc/bsp/bmw/gis_tp_par_dev/spp05_material_details.htm?if_material=0000000" + material);
        conn1.timeout(30000);
        conn1.cookies(getCookiesMap());
        Document document = conn1.execute().parse();
        Elements formul2 = document.getElementsByClass("formul2");
        if (CollectionUtils.isEmpty(formul2)) {
            return "";
        }
        Element element = formul2.get(11);
        if (element == null) {
            return "";
        }
        String html = element.html();
        int endIndexStr = html.indexOf("&nbsp");
        if (endIndexStr > 0) {
            return html.substring(0, endIndexStr).trim();
        }
        return null;
    }

    private static String getInventory(Element element) {
        String str = element.toString();
        int startIndex = str.indexOf("over reorderpoint:");
        if (startIndex < 0) {
            return "";
        }
        int endIndex = str.indexOf("'", startIndex);
        String inventory = str.substring(startIndex + "over reorderpoint:".length(), endIndex);
        //System.out.println(inventory);
        return inventory.trim();
    }

    private static Map<String, String> getCookiesMap() throws IOException {
        if (cookiesMap.isEmpty()) {
            Connection connect = Jsoup.connect("https://www.dwp.bmw-brilliance.cn/wit");
            connect.header("Authorization", "Basic NDYxMzZwYXI6NVRHQk5KSTk=");
            Connection.Response response = connect.execute();
            Map<String, String> cookies = response.cookies();
            //System.out.println(cookies);
            cookiesMap = cookies;
        }
        return cookiesMap;
    }
}
