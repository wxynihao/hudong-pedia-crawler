package me.rainking.baike.crawler;

import lombok.extern.slf4j.Slf4j;
import me.rainking.baike.repository.BaikeRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @Description: 爬虫的页面处理逻辑
 * @Author: Rain
 * @Date: 2018/2/28 16:29
 */
@Slf4j
@Component("baikePageProcessor")
public class BaikePageProcessor implements PageProcessor {

    @Autowired
    private BaikeRepository baikeRepository;

    private Site site = Site.me()
            .setDomain("baike.com")
            .setRetryTimes(3)
            .setSleepTime(1000);

    /**
     * process the page, extract urls to fetch, extract the data and store
     *
     * @param page page
     */
    @Override
    public void process(Page page) {
        //定义如何抽取页面信息，并保存下来
        //分类，需处理
        String rawCategory = page.getHtml().xpath("//p[@id='openCatp']/html()").toString();
        page.putField("category", convertCategoryHtml(rawCategory));
        //摘要，需处理
        String rawSummary = page.getHtml().xpath("//div[@class='summary']/html()").toString();
        page.putField("summary", convertSummaryHtml(rawSummary));
        //标题
        String rawTitle = page.getHtml().xpath("//div[@class='content-h1']//h1/text()").toString();
        page.putField("title", rawTitle);
        // 内容
        String contentRaw = page.getHtml().xpath("//div[@id='content']/tidyText()").toString();
        page.putField("content", convertContent(contentRaw, rawTitle));
        //信息框
        String inforBoxRaw = page.getHtml().xpath("//div[@id='datamodule']/html()").toString();
        page.putField("inforBox", convertInforboxHtml(inforBoxRaw));

        //必须包含标题，否则跳过
        if (page.getResultItems().get("title") == null) {
            page.setSkip(true);
        }

        //从页面发现后续的url地址来抓取
        //分类详情页获取子分类的链接
        String pageUrl = page.getUrl().toString();
        if (pageUrl.contains("fenlei") && !pageUrl.contains("list")) {
            List<String> catalogList = page.getHtml()
                    .xpath("//div[@class='sort']//p[2]").links().all()
                    .stream().map(this::getShortUrl).collect(toList());
            page.addTargetRequests(catalogList);

            //分类全部词条页链接
            List<String> catalogItemList = catalogList.stream()
                    .map(url -> geneCataListUrlFromCataName(getNameFromUrl(url))).collect(toList());
            page.addTargetRequests(catalogItemList);
        }


        //分类全部词条页获取词条页的链接
        if (pageUrl.contains("list")) {
            List<String> itemList = page.getHtml()
                    .links()
                    .regex("http://www\\.baike\\.com/wiki/.*")
                    .all()
                    .stream()
                    //过滤掉已经下载入库的词条
                    .filter(this::notInDb)
                    .map(this::getShortUrl).collect(toList());

            page.addTargetRequests(itemList);
        }

    }

    /**
     * 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }

    private String makeQueryStringAllRegExp(String str) {
        if (str == null || "".equals(str.trim())) {
            return str;
        }

        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }


    /**
     * 解析tidyText处理后的词条内容
     *
     * @param content 词条内容
     * @param title   词条名称，分割内容时使用
     * @return map类型的内容
     */
    private Map<String, String> convertContent(String content, String title) {

        if (content == null) {
            return null;
        }

        Map<String, String> contents = new LinkedHashMap<>(16);

        String regex = "/" + title + " 编辑";
        String contentRemoveLink = content
                .replaceAll("<>", "")
                .replaceAll("<http.*>", "")
                .replaceAll("\n+", "\n")
//                .replaceAll("\n\\s+\n", "\n")
                ;
        String[] contentFrags = contentRemoveLink.split(makeQueryStringAllRegExp(regex));

        //内容部分仅有一段且无标题时
        if (contentFrags.length == 1) {
            contents.put("无标题", contentFrags[0]);
            return contents;
        }

        String pagraphTitle = "";


        for (int index = 0; index < contentFrags.length; index++) {
            if (index == 0) {
                pagraphTitle = contentFrags[0];
            } else {
                int posOfLastLine = contentFrags[index].lastIndexOf("\n");

                contents.put(pagraphTitle.replaceAll("\\.", "[dot]").trim(), contentFrags[index].substring(0, posOfLastLine).trim());

                if (index != contentFrags.length - 1) {
                    pagraphTitle = contentFrags[index].substring(posOfLastLine + "\n".length());
                }
            }
        }

        return contents;
    }

    /**
     * 解析 html 格式 的 summary 内容
     *
     * @param summaryHtml html 格式摘要
     * @return 纯文本规范格式摘要
     */
    private String convertSummaryHtml(String summaryHtml) {
        if (summaryHtml == null) {
            return null;
        }

        Document doc = Jsoup.parseBodyFragment(summaryHtml);
        Elements fields = doc.select("p");

        return fields.stream().map(Element::text).collect(Collectors.joining("\n"));

    }

    /**
     * 解析 html 格式 的 inforbox 内容
     *
     * @param inforboxHtml html 格式 的 inforbox
     * @return 解析后的map格式信息框内容
     */
    private Map<String, String> convertInforboxHtml(String inforboxHtml) {

        if (inforboxHtml == null) {
            return null;
        }

        Map<String, String> infors = new LinkedHashMap<>(16);
        Document doc = Jsoup.parseBodyFragment(inforboxHtml);
        Elements fields = doc.select("strong");
        Elements values = doc.select("span");

        for (int index = 0; index < fields.size(); index++) {
            String fieldRaw = fields.get(index).text();
            String field = fieldRaw.substring(0, fieldRaw.lastIndexOf("："));
            infors.put(field, values.get(index).text());
        }

        return infors;
    }

    /**
     * 解析html格式的 category 内容
     *
     * @param categoryHtml html格式的category
     * @return 处理过的category
     */
    private String convertCategoryHtml(String categoryHtml) {

        if (categoryHtml == null) {
            return "";
        }

        Document doc = Jsoup.parseBodyFragment(categoryHtml);
        Elements fields = doc.select("a");

        return fields.stream().map(Element::text).collect(Collectors.joining(" "));
    }

    /**
     * 从分类链接中获取分类(词条)名称
     *
     * @param url 分类链接
     * @return 分类名称
     */
    private String getNameFromUrl(String url) {

        String name = "";

        int pos = url.lastIndexOf("/");

        if (pos != -1) {
            name = url.substring(pos + 1);
        }

        return name;
    }

    /**
     * 根据分类名称，生成分类全部词条页面的链接
     *
     * @param cataName 分类名称
     * @return 分类全部词条页链接
     */
    private String geneCataListUrlFromCataName(String cataName) {
        return "http://fenlei.baike.com/" + cataName + "/list/";
    }

    /**
     * 将包含参数的链接中的参数删除，保留最简短的形式
     *
     * @param longUrl 包含参数的长链接
     * @return 简短形式链接
     */
    private String getShortUrl(String longUrl) {
        int pos = longUrl.lastIndexOf("?");

        String url = longUrl;
        if (pos != -1) {
            url = url.substring(0, pos);
        }

        return url;
    }

    /**
     * 判断数据库中是否不存在该词条
     *
     * @param url 包含标题信息的url
     * @return 是否不存在
     */
    private boolean notInDb(String url) {
        String title = "";
        try {
            title = URLDecoder.decode(getNameFromUrl(url), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        long countOfTitle = baikeRepository.countByTitle(title);

        boolean isNotInDb = false;

        if (countOfTitle == 0) {
            isNotInDb = true;
        } else if (countOfTitle == 1) {
            isNotInDb = false;
        } else if (countOfTitle > 1) {
            isNotInDb = false;
            log.warn("\n" + title + "在数据库中存在" + countOfTitle + "条记录。" + "\n");
        }

        return isNotInDb;
    }

}
