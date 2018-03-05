package me.rainking.baike;

import me.rainking.baike.crawler.BaikeMongodbPipeline;
import me.rainking.baike.crawler.BaikePageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.PageModelPipeline;

import java.net.URLEncoder;


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
    @Autowired
    private BaikeMongodbPipeline baikeMongodbPipeline;

    public static void main(String[] args) {
        SpringApplication.run(BaikeApplication.class, args);
    }

    /**
     * 该方法随程序启动而运行
     * 添加该方法是为了使用java -jar命令运行爬虫
     * 该方法在测试时也会运行，进行单元测试前需注释掉方法体
     *
     * @param strings
     * @throws Exception
     */
    @Override
    public void run(String... strings) throws Exception {

//        String category = URLEncoder.encode(nameOfRootCategory, "utf-8");
//
//        Spider.create(new BaikePageProcessor())
//                //将爬取的数据存入mngodb
//                .addPipeline(baikeMongodbPipeline)
//                //种子链接为军事分类首页与军事全部词条页
//                .addUrl("http://fenlei.baike.com/" + category, "http://fenlei.baike.com/" + category + "/list/")
//                //开启5个线程抓取
//                .thread(5)
//                //启动爬虫
//                .run();
    }
}
