package com.notejava.demo.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.notejava.demo.ExcelUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Bilibili {

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        System.out.println("工作目录:" + userDir);

        /*Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入视频网址");
            url = scanner.nextLine();
            if (!StringUtil.isBlank(url)) {
                break;
            }
        }
        scanner.close();*/

        String url = FileUtils.readFileToString(new File(userDir + File.separator + "url.txt"), "utf-8");
        if (StringUtil.isBlank(url)) {
            System.out.println("url 为空");
            return;
        }

        System.out.println("================================开始抓取====================================");

        url = url.trim().replace("https", "http");
        String aid = getAid(url);
        if (StringUtil.isBlank(aid)) {
            System.out.println("获取 aid 失败");
            return;
        }

        int pageCount = getPageCount(aid);
        if (pageCount == 0) {
            System.out.println("评论页数为空");
            return;
        }

        System.out.println("评论页数为:" + pageCount);

        Random random = new Random();
        List<Comment> commentList = Lists.newArrayList();
        for (int i = 1; i <= pageCount; i++) {
            System.out.println("抓取第" + i + "页...");
            try {
                List<Comment> onePageComment = getOnePageComment(aid, i);
                commentList.addAll(onePageComment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int sleepSecond = random.nextInt(3);
            Thread.sleep(sleepSecond * 1000);
        }
        Map<String, String> header = new HashMap<>();
        header.put("msg", "");
        ExcelUtils.export(header, commentList, userDir + File.separator + "bilibiliComment.xlsx");

        System.out.println("================================结束抓取====================================");

        System.out.println("评论数据抓取成功，请查看 bilibiliComment.xlsx");
    }

    private static String getAid(String url) throws IOException {
        try {
            String html = Jsoup.connect(url).timeout(30000).ignoreContentType(true).execute().body();
            String startIndexStr = "\"aid\":";
            String endIndexStr = ",";
            int startIndex = html.indexOf(startIndexStr);
            int endIndex = html.indexOf(endIndexStr, startIndex);
            String aid = html.substring(startIndex + startIndexStr.length(), endIndex);
            return aid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getPageCount(String aid) throws IOException {
        String url = "http://api.bilibili.com/x/v2/reply?jsonp=jsonp&pn=1&type=1&oid=" + aid + "&sort=2&_=1588240827784";
        String body = Jsoup.connect(url).timeout(30000).ignoreContentType(true).execute().body();
        if (StringUtil.isBlank(body)) {
            System.out.println("getPageCount body 为空");
            return 0;
        }
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject == null) {
            System.out.println(" getPageCount jsonObject is null finish");
            return 0;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            System.out.println("getPageCount data is null finish");
            return 0;
        }
        JSONObject page = data.getJSONObject("page");
        Integer count = page.getInteger("count");
        int pageCount = (count / 20) + 1;
        return pageCount;
    }

    /**
     * 获取单页评论列表
     *
     * @param pageNo
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static List<Comment> getOnePageComment(String aid, int pageNo) throws IOException {
        String url = "http://api.bilibili.com/x/v2/reply?jsonp=jsonp&pn=" + pageNo + "&type=1&oid=" + aid + "&sort=2&_=1588240827784";
        String body = Jsoup.connect(url).timeout(30000).ignoreContentType(true).execute().body();
        if (StringUtil.isBlank(body)) {
            System.out.println("body 为空");
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject == null) {
            System.out.println("jsonObject is null finish");
            return Collections.emptyList();
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            System.out.println("data is null finish");
            return Collections.emptyList();
        }
        JSONArray replies = data.getJSONArray("replies");
        if (CollectionUtils.isEmpty(replies)) {
            System.out.println("replies is null finish");
            return Collections.emptyList();
        }
        List<Comment> commentList = Lists.newArrayList();
        for (Object reply : replies) {
            JSONObject jo = (JSONObject) reply;
            JSONObject content = jo.getJSONObject("content");
            String message = content.getString("message");
            System.out.println(message);
            Comment comment = new Comment();
            comment.setMsg(message);
            commentList.add(comment);
        }
        return commentList;
    }
}
