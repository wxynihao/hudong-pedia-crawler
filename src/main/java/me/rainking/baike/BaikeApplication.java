package me.rainking.baike;

import me.rainking.baike.crawler.BaikeMongodbPipeline;
import me.rainking.baike.crawler.BaikePageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import us.codecraft.webmagic.Spider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @Description: 主程序
 * @Author: Rain
 * @Date: 2018/2/28 16:29
 */
@SpringBootApplication
public class BaikeApplication implements CommandLineRunner {

    /**
     * 需要爬取词条的根分类
     */
    @Value("${nameOfRootCategory}")
    private String nameOfRootCategory;

    /**
     * 必须使用@Autowired注解注入的方式，否则将导致其他部分的DAO接口调用为null
     */
    private final BaikeMongodbPipeline baikeMongodbPipeline;

    /**
     *
     */
    private final BaikePageProcessor baikePageProcessor;

    @Autowired
    public BaikeApplication(BaikeMongodbPipeline baikeMongodbPipeline, BaikePageProcessor baikePageProcessor) {
        this.baikeMongodbPipeline = baikeMongodbPipeline;
        this.baikePageProcessor = baikePageProcessor;
    }

    public static void main(String[] args) {
        SpringApplication.run(BaikeApplication.class, args);
    }

    /**
     * 该方法随程序启动而运行
     * 添加该方法是为了使用java -jar命令运行爬虫
     * 该方法在测试时也会运行，进行单元测试前需注释掉方法体
     *
     * @param strings 运行参数
     * @throws UnsupportedEncodingException url编码时的编码错误
     */
    @Override
    public void run(String... strings) throws UnsupportedEncodingException {

        String category = URLEncoder.encode(nameOfRootCategory, "utf-8");

        Spider.create(baikePageProcessor)
                //将爬取的数据存入mongodb
                .addPipeline(baikeMongodbPipeline)
                //种子链接为根分类首页与根分类全部词条页
                .addUrl("http://fenlei.baike.com/" + category, "http://fenlei.baike.com/" + category + "/list/")
                //开启5个线程抓取
                .thread(5)
                //启动爬虫
                .run();
    }
}
